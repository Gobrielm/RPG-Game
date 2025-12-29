package com.mainApp.rpg.dungeonExploration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.mainApp.rpg.R
import com.mainApp.rpg.databinding.ActivityDungeonExplorationBinding
import com.mainApp.rpg.databinding.DungeonCharacterUiBinding
import com.mainApp.rpg.dungeon.Dungeon
import com.mainApp.rpg.dungeon.DungeonRooms
import com.mainApp.rpg.dungeon.InfiniteDungeon
import com.mainApp.rpg.enemy.EnemyCharacter
import com.mainApp.rpg.homepage.HomePage
import com.mainApp.rpg.homepage.inventory.PlayerInventory
import com.mainApp.rpg.attack.Attack
import com.mainApp.rpg.playerCharacter.Equipment
import com.mainApp.rpg.playerCharacter.PlayerCharacter
import com.mainApp.rpg.playerCharacter.StatusEffect
import com.mainApp.rpg.requests.getUserName
import com.mainApp.rpg.requests.getUserToken
import com.mainApp.rpg.requests.makePutRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Random

class DungeonExplorationActivity: AppCompatActivity() {


    private lateinit var binding: ActivityDungeonExplorationBinding
    private val viewModel: DungeonExplorationViewModel by viewModels()
    private var currentRoom: Int = 0
    private var finished: Boolean = false
    private var pauseInputs: Boolean = false
    private var leaveButtonClicked: Boolean = false
    private var attackedChosen: Attack? = null // For storing friendly attacks
    private var choosingTarget: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDungeonExplorationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Do nothing
                }
            }
        )

        val character1ID = intent.getIntExtra("CHARACTER1", -1)
        val character2ID = intent.getIntExtra("CHARACTER2", -1)
        val character3ID = intent.getIntExtra("CHARACTER3", -1)
        currentRoom = intent.getIntExtra("STARTING_DEPTH", 0)

        if (character1ID == -1) {
            Log.e("DungeonExplorationActivity", "No Valid Character Chosen")
            return
        }

        finished = false

        viewModel.addSelectedCharacter(PlayerInventory.getCharacter(character1ID)!!)

        if (character2ID != character1ID && character2ID != -1) {
            binding.characterUi.characterUi2Container.visibility = VISIBLE
            val character = PlayerInventory.getCharacter(character2ID)!!
            viewModel.addSelectedCharacter(character)
        }
        if (character2ID != character3ID && character3ID != character1ID && character3ID != -1) {
            binding.characterUi.characterUi3Container.visibility = VISIBLE
            val character = PlayerInventory.getCharacter(character3ID)!!
            viewModel.addSelectedCharacter(character)
        }


        viewModel.setDungeon(InfiniteDungeon())
        updateStats()
        setAttacks()

        binding.backButton.setOnClickListener {
            clickGoBack()
        }

        binding.characterUi.mv1Button.setOnClickListener {
            chooseAttack(0)
        }
        binding.characterUi.mv2Button.setOnClickListener {
            chooseAttack(1)
        }
        binding.characterUi.mv3Button.setOnClickListener {
            chooseAttack(2)
        }
        binding.characterUi.mv4Button.setOnClickListener {
            chooseAttack(3)
        }
        binding.characterUi.skipButton.setOnClickListener {
            skipAttack()
        }

        val buttons = arrayOf(binding.characterUi.friendly1Button, binding.characterUi.friendly2Button, binding.characterUi.friendly3Button)
        for (i in 0 until buttons.size) {
            val button = buttons[i]
            button.setOnClickListener {
                chooseTarget(i)
            }
        }

        viewModel.readyForNextRoom.observe(this, Observer { ready ->
            if (!ready) return@Observer
            moveToNextRoom()
        })

        viewModel.partyDied.observe(this, Observer { hasDied ->
            if (!hasDied) return@Observer
            binding.characterUi.failText.visibility = VISIBLE
            binding.characterUi.blurRect.visibility = VISIBLE
            finished = true
            lifecycleScope.launch {
                delay(2000)
                moveToMainMenu()
            }
        })
    }

    fun getCurrentRoom(): Int {
        return currentRoom
    }

    fun tryToLeaveDungeon() {
        if (finished) return

        binding.characterUi.finishText.visibility = VISIBLE
        binding.characterUi.blurRect.visibility = VISIBLE
        finished = true
        lifecycleScope.launch {
            delay(2000)
            moveToEndingScreen()
        }
    }

    fun clickGoBack() {
        if (leaveButtonClicked) {
            moveToMainMenu()
        } else {
            lifecycleScope.launch {
                leaveButtonClicked = true

                binding.characterUi.failText.visibility = View.VISIBLE
                binding.characterUi.failText.text = "You Will Lose All Progress in the Current Dungeon. \nClick to Confirm and Leave the Dungeon."
                delay(1500)
                binding.characterUi.failText.visibility = View.GONE
                binding.characterUi.failText.text = "Fail"

                leaveButtonClicked = false
            }
        }
    }

    fun showLootUi(lootEarned: ArrayList<Int>, coinsEarned: Int) {
        val container = binding.lootEarnedUi.lootContainer
        container.visibility = VISIBLE
        container.maxRowWidthPx = (300 * resources.displayMetrics.density).toInt() // example

        val size = (90 * resources.displayMetrics.density).toInt()

        container.removeChildren()

        val btn = Button(this).apply {
            text = "$coinsEarned Coins"
            layoutParams = ViewGroup.LayoutParams(size, size)
            isAllCaps = false
            maxLines = 1
            ellipsize = null

            setAutoSizeTextTypeUniformWithConfiguration(
                6, 40, 2, TypedValue.COMPLEX_UNIT_SP
            )
        }
        container.addView(btn)

        for (i in 0 until lootEarned.size) {
            val btn = Button(this).apply {
                text = Equipment.getEquipment(lootEarned[i])?.name ?: ""
                layoutParams = ViewGroup.LayoutParams(size, size)
            }
            container.addView(btn)
        }
    }

    fun unShowLootUi() {
        binding.lootEarnedUi.lootContainer.visibility = View.GONE
    }

    fun updateStats() {
        if (viewModel.partyDied.value!!) return
        val character1 = viewModel.getCharacter(0)

        if (character1 != null) {
            if (character1 == viewModel.getSelectedCharacter()) {
                highlightCharacter(binding.characterUi.characterUi1)
            } else {
                resetHighlights(binding.characterUi.characterUi1)
            }

            if (!character1.isAlive()) {
                binding.characterUi.characterUi1Container.visibility = View.INVISIBLE
            } else {
                binding.characterUi.characterUi1Container.visibility = View.VISIBLE
            }

            binding.characterUi.characterUi1.nameTextView.text =
                character1.playerClass.mainClass.toString()
            updateProgressBars(
                character1,
                binding.characterUi.characterUi1
            )

            updateStatusEffectsOnMainGui(character1, binding.characterUi.characterUi1)
        }

        val character2 = viewModel.getCharacter(1)
        if (character2 != null) {
            if (character2 == viewModel.getSelectedCharacter()) {
                highlightCharacter(binding.characterUi.characterUi2)
            } else {
                resetHighlights(binding.characterUi.characterUi2)
            }

            if (!character2.isAlive()) {
                binding.characterUi.characterUi2Container.visibility = View.INVISIBLE
            } else {
                binding.characterUi.characterUi2Container.visibility = View.VISIBLE
            }

            binding.characterUi.characterUi2.nameTextView.text =
                character2.playerClass.mainClass.toString()
            updateProgressBars(
                character2,
                binding.characterUi.characterUi2
            )

            updateStatusEffectsOnMainGui(character2, binding.characterUi.characterUi2)
        }

        val character3 = viewModel.getCharacter(2)
        if (character3 != null) {
            if (character3 == viewModel.getSelectedCharacter()) {
                highlightCharacter(binding.characterUi.characterUi3)
            } else {
                resetHighlights(binding.characterUi.characterUi3)
            }

            if (!character3.isAlive()) {
                binding.characterUi.characterUi3Container.visibility = View.INVISIBLE
            } else {
                binding.characterUi.characterUi3Container.visibility = View.VISIBLE
            }

            binding.characterUi.characterUi3.nameTextView.text =
                character3.playerClass.mainClass.toString()
            updateProgressBars(
                character3,
                binding.characterUi.characterUi3
            )

            binding.characterUi.characterUi3

            updateStatusEffectsOnMainGui(character3, binding.characterUi.characterUi3)
        }
    }

    fun highlightCharacter(container: DungeonCharacterUiBinding) {
        container.nameTextView.setTextColor(0xFF22FF22.toInt())
    }

    fun resetHighlights(container: DungeonCharacterUiBinding) {
        container.nameTextView.setTextColor(0xFF000000.toInt())
    }

    fun updateStatusEffectsOnMainGui(character: PlayerCharacter, container: DungeonCharacterUiBinding) {
        val statusEffects = character.getStatusEffects()

        val statusEffectImages = arrayOf(container.statusEffect1, container.statusEffect2, container.statusEffect3)
        for (i in 0 until 3) {
            if (i < statusEffects.size) {
                val imgID = StatusEffect.getImgFromID(statusEffects[i].id)
                if (imgID != null) statusEffectImages[i].setImageResource(imgID)
                statusEffectImages[i].visibility = View.VISIBLE
            } else {
                statusEffectImages[i].visibility = View.INVISIBLE
            }
        }
    }

    fun updateProgressBars(character: PlayerCharacter, container: DungeonCharacterUiBinding) {
        container.healthProgressbar.max = character.baseStats.getHealth()
        container.healthProgressbar.progress = character.currentStats.getHealth()

        container.manaProgressbar.max = character.baseStats.getMana()
        container.manaProgressbar.progress = character.currentStats.getMana()

        container.staminaProgressbar.max = character.baseStats.getStamina()
        container.staminaProgressbar.progress = character.currentStats.getStamina()

        if (character.getShieldHitPoints() > 0) {
            container.healthProgressbar.max += character.getShieldHitPoints()
            container.healthProgressbar.secondaryProgress = character.getShieldHitPoints() + character.currentStats.getHealth()
        } else {
            container.healthProgressbar.secondaryProgress = 0
        }
    }

    fun setAttacks() {
        val character = viewModel.getSelectedCharacter()!!
        binding.characterUi.mv1Button.text = (character.getAttack(0)?.name ?: "None")
        binding.characterUi.mv2Button.text = (character.getAttack(1)?.name ?: "None")
        binding.characterUi.mv3Button.text = (character.getAttack(2)?.name ?: "None")
        binding.characterUi.mv4Button.text = (character.getAttack(3)?.name ?: "None")
    }

    fun chooseAttack(mvInd: Int) {
        if (finished || pauseInputs) return
        val playerCharacter = viewModel.getSelectedCharacter()!!
        val attack = playerCharacter.attacks[mvInd]
        if (attack == null) return

        if (!playerCharacter.canChooseAttack(attack)) {
            lifecycleScope.launch {
                binding.characterUi.centeredText.text = "Need "
                delay(1000)

            }
            // TODO: Show err msg
            return
        }

        // TODO: If it is a friendly attack then choose target here
        if (attack.friendlyAttack) {
            attackedChosen = attack
            allowChoosingTarget()
        } else {
            disallowChoosingTarget()
            viewModel.setPlayerAttack(attack, -1)
        }
    }

    fun allowChoosingTarget() {
        choosingTarget = true
        binding.characterUi.centeredText.text = "Pick a Target"
    }

    fun disallowChoosingTarget() {
        choosingTarget = false
        binding.characterUi.centeredText.text = ""
    }

    fun chooseTarget(ind: Int) {
        if (!choosingTarget) return

        if (viewModel.getCharacter(ind) == null) return
        disallowChoosingTarget()

        if (attackedChosen == null) return

        viewModel.setPlayerAttack(attackedChosen, ind)
    }

    fun skipAttack() {
        if (finished || pauseInputs) return
        viewModel.setPlayerAttack(null, -1)
    }

    fun blurIn() {
        binding.characterUi.blurRect.apply {
            alpha = 0f
            visibility = VISIBLE
            animate()
                .alpha(1f)
                .setDuration(300) // ms
                .start()
        }
    }

    fun blurOut() {
        binding.characterUi.blurRect.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                binding.characterUi.blurRect.visibility = View.GONE
            }
            .start()
    }

    fun hideCharacterUI() {
        binding.characterUiContainer.visibility = View.INVISIBLE
        binding.backButton.visibility = View.INVISIBLE
    }

    fun showCharacterUI() {
        binding.characterUiContainer.visibility = View.VISIBLE
        binding.backButton.visibility = View.VISIBLE
    }

    fun showAttackIndicatorOnCharacter(ind: Int) {
        val container = when (ind) {
            0 -> binding.characterUi.characterUi1
            1 -> binding.characterUi.characterUi2
            else -> binding.characterUi.characterUi3
        }


        lifecycleScope.launch {
            delay(300)

            container.damageIndicator.apply {
                alpha = 0f
                visibility = VISIBLE
                animate()
                    .alpha(1f)
                    .setDuration(300) // ms
                    .start()
            }

            delay(500)
            container.damageIndicator.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    container.damageIndicator.visibility = View.GONE
                }
                .start()
        }
    }

    fun startBlockingInputs() {
        pauseInputs = true
    }

    fun stopBlockingInputs() {
        pauseInputs = false
    }

    fun moveToNextRoom() {
        if (finished) return
        pauseInputs = true
        blurIn()
        viewModel.resetReadyForNextRoom()
        lifecycleScope.launch {
            delay(1000)
            blurOut()
            val roomType = viewModel.getDungeon()!!.getRoom(++currentRoom)!!
            val navController = findNavController(R.id.nav_host_fragment_character_ui)

            showCharacterUI()
            when (roomType) {
                DungeonRooms.BOSS ->
                    navController.navigate(R.id.regular_room, bundleOf("boss" to true))

                DungeonRooms.TREASURE -> {
                    hideCharacterUI()
                    navController.navigate(R.id.treasure_room)
                }
                DungeonRooms.REST -> {
                    hideCharacterUI()
                    navController.navigate(R.id.rest_room)
                }
                else -> {
                    navController.navigate(R.id.regular_room, bundleOf("boss" to false))
                }
            }
            pauseInputs = false
            updateStats()
            setAttacks()
        }
    }

    fun moveToEndingScreen() {
        unShowLootUi()
        hideCharacterUI()
        binding.backButton.visibility = View.GONE

        lifecycleScope.launch {

            val token = getUserToken()
            if (token == null) return@launch

            val username = getUserName()
            if (username == null) Log.e("Dungeon Exploration Activity", "No Username Found")

            val json = JSONObject()
            json.put("deepestRoom", currentRoom)
            json.put("username", username!!)

            val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

            makePutRequest(
                "https://bikinggamebackend.vercel.app/api/leaderboard/",
                token,
                body
            )
        }

        val navController = findNavController(R.id.nav_host_fragment_character_ui)
        navController.navigate(R.id.finish_screen)
    }

    fun moveToMainMenu() {
        val intent = Intent(this, HomePage::class.java)
        startActivity(intent)
    }
}

class DungeonExplorationViewModel : ViewModel() {
    private val _currentCharacterInd = MutableLiveData(0)
    private val currentCharacterInd get() = _currentCharacterInd.value!!
    private val _mutableCharacters = MutableLiveData(arrayOf<PlayerCharacter?>(null, null, null))
    private val mutableCharacters get() = _mutableCharacters.value!!
    private val _currentEnemyInd = MutableLiveData(0)
    private val currentEnemyInd get() = _currentEnemyInd.value!!
    private val _mutableEnemies = MutableLiveData(arrayOf<EnemyCharacter?>(null, null, null))
    private val mutableEnemies get() = _mutableEnemies.value!!
    private val mutableDungeon = MutableLiveData<Dungeon>()
    private val mutablePlayerAttack = MutableLiveData<Pair<Attack, Int>?>(null)
    private val mutableReadyForNextRoom = MutableLiveData(false)
    private val mutablePartyDied = MutableLiveData(false)
    private val mutablePartyDone = MutableLiveData(false)
    private val lootEarned = MutableLiveData(mutableMapOf(-2 to 0, -1 to 0)) // XP first, then gold

    val attack: LiveData<Pair<Attack, Int>?> get() = mutablePlayerAttack
    val readyForNextRoom: LiveData<Boolean> get() = mutableReadyForNextRoom
    val partyDied: LiveData<Boolean> get() = mutablePartyDied
    val partyDone: LiveData<Boolean> get() = mutablePartyDone

    fun addSelectedCharacter(character: PlayerCharacter) {
        for (i in 0 until 3) {
            if (mutableCharacters[i] == null) {
                _mutableCharacters.value!![i] = character
                break
            }
        }
    }

    fun addEnemy(enemy: EnemyCharacter, ind: Int) {
        mutableEnemies[ind] = enemy
    }

    fun setDungeon(dungeon: Dungeon) {
        mutableDungeon.value = dungeon
    }

    fun setPlayerAttack(attack: Attack?, target: Int) {
        mutablePlayerAttack.value = if (attack == null) null else Pair(attack, target)
    }

    fun setReadyForNextRoom() {
        mutableReadyForNextRoom.value = true
    }

    fun resetReadyForNextRoom() {
        mutableReadyForNextRoom.value = false
    }

    fun setPartyHasDied() {
        mutablePartyDied.value = true
    }

    fun setPartyIsDone() {
        mutablePartyDone.value = true
    }

    fun addExpEarned(exp: Int) {
        lootEarned.value!![-2] = lootEarned.value!![-2]!! + exp
    }

    fun addCoinsEarned(coins: Int) {
        lootEarned.value!![-1] = lootEarned.value!![-1]!! + coins
    }

    fun addLootEarned(lootToAdd: ArrayList<Int>) {
        lootToAdd.forEach { lootID ->
            if (!lootEarned.value!!.contains(lootID)) {
                lootEarned.value!![lootID] = 0
            }
            lootEarned.value!![lootID] = lootEarned.value!![lootID]!! + 1
        }
    }

    fun getLootEarned(): Map<Int, Int> {
        return lootEarned.value!!.toMap()
    }

    fun getCharacterIDs(): Array<Int> {
        val a = ArrayList<Int>()
        for (i in 0 until 3) {
            val character = mutableCharacters[i]
            if (character != null) a.add(character.id)
        }
        return a.toTypedArray()
    }

    fun getSelectedCharacter(): PlayerCharacter? {
        return mutableCharacters[currentCharacterInd]
    }

    fun getCharacter(ind: Int): PlayerCharacter? {
        if (ind >= 3 || ind < 0) return null
        return mutableCharacters[ind]
    }

    fun cycleSelectedCharacter() {
        if (partyDied.value!!) return
        removeDeadCharacters()
        _currentCharacterInd.value = (currentCharacterInd + 1) % 3 // Force next character
        var i = 0
        while (getCharacter(_currentCharacterInd.value!!) == null || !getCharacter(_currentCharacterInd.value!!)!!.isAlive()) { // Find first alive character
            _currentCharacterInd.value = (currentCharacterInd + 1) % 3
            if (i++ > 3) {
                setPartyHasDied()
                break
            } // whole party is dead break
        }
    }

    fun removeDeadCharacters() {
        for (i in 0 until 3) {
            if (mutableCharacters[i] != null && !mutableCharacters[i]!!.isAlive()) {
                mutableCharacters[i] = null
            }
        }
    }

    fun getRandomCharacter(): Pair<Int, PlayerCharacter> {
        val validCharacters = arrayListOf<Int>()
        for (i in 0 until 3) {
            if (mutableCharacters[i] != null) validCharacters.add(i)
        }

        val validCharacterInd = Random().nextInt(validCharacters.size)
        val characterInd = validCharacters[validCharacterInd]
        return Pair(characterInd, mutableCharacters[characterInd]!!)
    }

    fun getPartySize(): Int {
        var count = 0
        for (i in 0 until 3) {
            if (mutableCharacters[i] != null) count++
        }
        return count
    }

    fun getSelectedEnemy(): EnemyCharacter? {
        return mutableEnemies[currentEnemyInd]
    }

    fun getEnemy(ind: Int): EnemyCharacter? {
        if (ind >= 3 || ind < 0) return null
        return mutableEnemies[ind]
    }

    fun cycleSelectedEnemy() {
        if (mutableReadyForNextRoom.value!!) return
        removeDeadEnemies()
        _currentEnemyInd.value = (currentEnemyInd + 1) % 3 // Force next character
        var i = 0
        while (getEnemy(currentEnemyInd) == null || getEnemy(currentEnemyInd)!!.isDead()) { // Find first alive character
            _currentEnemyInd.value = (currentEnemyInd + 1) % 3
            if (i++ > 3) {
                mutableReadyForNextRoom.value = true
                break
            } // enemies dead, party is done w/room
        }
    }

    fun setSelectedEnemy(ind: Int) {
        _currentEnemyInd.value = ind
    }

    fun removeDeadEnemies() {
        for (i in 0 until 3) {
            if ((mutableEnemies[i]?.isDead() ?: false)) {
                mutableEnemies[i] = null
            }
        }
    }

    fun getEnemiesSize(): Int {
        var count = 0
        for (i in 0 until 3) {
            if (mutableEnemies[i] != null) count++
        }
        return count
    }

    fun getDungeon(): Dungeon? {
        return mutableDungeon.value
    }

}
