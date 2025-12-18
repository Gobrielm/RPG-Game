package com.example.bikinggame.dungeonExploration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
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
import com.example.bikinggame.R
import com.example.bikinggame.databinding.ActivityDungeonExplorationBinding
import com.example.bikinggame.databinding.DungeonCharacterUiBinding
import com.example.bikinggame.dungeon.Dungeon
import com.example.bikinggame.dungeon.DungeonRooms
import com.example.bikinggame.dungeon.InfiniteDungeon
import com.example.bikinggame.enemy.EnemyCharacter
import com.example.bikinggame.homepage.HomePage
import com.example.bikinggame.homepage.inventory.PlayerInventory
import com.example.bikinggame.playerCharacter.Attack
import com.example.bikinggame.playerCharacter.Attack.AttackTypes
import com.example.bikinggame.playerCharacter.PlayerCharacter
import com.example.bikinggame.requests.getUserName
import com.example.bikinggame.requests.getUserToken
import com.example.bikinggame.requests.makePutRequest
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

        if (character1ID == -1) {
            Log.e("DungeonExplorationActivity", "No Valid Character Chosen")
            return
        }

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

        binding.characterUi.backButton.setOnClickListener {
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

        viewModel.readyForNextRoom.observe(this, Observer {
            viewModel.cycleSelectedCharacter()
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

        viewModel.partyDone.observe(this, Observer {
            binding.characterUi.finishText.visibility = VISIBLE
            binding.characterUi.blurRect.visibility = VISIBLE
            finished = true
            lifecycleScope.launch {
                delay(2000)
                moveToEndingScreen()
            }
        })
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

        val size = (60 * resources.displayMetrics.density).toInt()

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
                text = i.toString()
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
        val character1 = viewModel.getCharacter(0)!!

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

        binding.characterUi.characterUi1.nameTextView.text = character1.playerClass.mainClass.toString()
        updateProgressBars(character1,
            binding.characterUi.characterUi1.healthProgressbar,
            binding.characterUi.characterUi1.manaProgressbar,
            binding.characterUi.characterUi1.staminaProgressbar
        )

        updateStatusEffectsOnMainGui(character1, binding.characterUi.characterUi1)

        val character2 = viewModel.getCharacter(1)
        if (character2 == null) return

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

        binding.characterUi.characterUi2.nameTextView.text = character2.playerClass.mainClass.toString()
        updateProgressBars(character2,
            binding.characterUi.characterUi2.healthProgressbar,
            binding.characterUi.characterUi2.manaProgressbar,
            binding.characterUi.characterUi2.staminaProgressbar
        )

        updateStatusEffectsOnMainGui(character2, binding.characterUi.characterUi2)

        val character3 = viewModel.getCharacter(2)
        if (character3 == null) return

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

        binding.characterUi.characterUi3.nameTextView.text = character3.playerClass.mainClass.toString()
        updateProgressBars(character3,
            binding.characterUi.characterUi3.healthProgressbar,
            binding.characterUi.characterUi3.manaProgressbar,
            binding.characterUi.characterUi3.staminaProgressbar
        )

        binding.characterUi.characterUi3

        updateStatusEffectsOnMainGui(character3, binding.characterUi.characterUi3)
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
            if (i < statusEffects.size - 1) {
                // TODO: Set Img here
                statusEffectImages[i].visibility = View.VISIBLE
            } else {
                statusEffectImages[i].visibility = View.INVISIBLE
            }
        }
    }

    fun updateProgressBars(character: PlayerCharacter, healthProgressBar: ProgressBar, manaProgressBar: ProgressBar, staminaProgressBar: ProgressBar) {
        healthProgressBar.progress = (character.currentStats.getHealth().toDouble() / character.baseStats.getHealth() * 100.0).toInt()
        manaProgressBar.progress = (character.currentStats.getMana().toDouble() / character.baseStats.getMana() * 100.0).toInt()
        staminaProgressBar.progress = (character.currentStats.getStamina().toDouble() / character.baseStats.getStamina() * 100.0).toInt()
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
            // TODO: Show err msg
            return
        }

        viewModel.setPlayerAttack(attack)
    }

    fun skipAttack() {
        if (finished || pauseInputs) return
        viewModel.setPlayerAttack(null)
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
    }

    fun showCharacterUI() {
        binding.characterUiContainer.visibility = View.VISIBLE
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
    private val mutableCharacters = MutableLiveData(arrayListOf<PlayerCharacter>())
    private val mutableEnemy = MutableLiveData<EnemyCharacter>()
    private val mutableDungeon = MutableLiveData<Dungeon>()
    private val mutablePlayerAttack = MutableLiveData(Attack(0, "Temp", 0, 0, 0, AttackTypes.PHY))
    private val mutableReadyForNextRoom = MutableLiveData<Boolean>()
    private val mutablePartyDied = MutableLiveData(false)
    private val mutablePartyDone = MutableLiveData<Boolean>()
    private val lootEarned = MutableLiveData(mutableMapOf(-2 to 0, -1 to 0)) // XP first, then gold

    val attack: LiveData<Attack> get() = mutablePlayerAttack
    val readyForNextRoom: LiveData<Boolean> get() = mutableReadyForNextRoom
    val partyDied: LiveData<Boolean> get() = mutablePartyDied
    val partyDone: LiveData<Boolean> get() = mutablePartyDone

    fun addSelectedCharacter(character: PlayerCharacter) {
        mutableCharacters.value!!.add(character)
    }

    fun setEnemy(enemy: EnemyCharacter) {
        mutableEnemy.value = enemy
    }

    fun setDungeon(dungeon: Dungeon) {
        mutableDungeon.value = dungeon
    }

    fun setPlayerAttack(attack: Attack?) {
        mutablePlayerAttack.value = attack
    }

    fun setReadyForNextRoom() {
        mutableReadyForNextRoom.value = true
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
        val characters = mutableCharacters.value!!
        return characters.map { it.id }.toTypedArray()
    }

    fun getSelectedCharacter(): PlayerCharacter? {
        return mutableCharacters.value!![currentCharacterInd]
    }

    fun getCharacter(ind: Int): PlayerCharacter? {
        if (ind >= mutableCharacters.value!!.size) return null
        return mutableCharacters.value!![ind]
    }

    fun getNextCharacter(): PlayerCharacter? {
        val next = mutableCharacters.value!![(currentCharacterInd + 1) % getPartySize()]
        if (next == getSelectedCharacter()) return null
        return next
    }

    fun getNextNextCharacter(): PlayerCharacter? {
        val next = mutableCharacters.value!![(currentCharacterInd + 2) % getPartySize()]
        if (next == getSelectedCharacter()) return null
        return next
    }

    fun cycleSelectedCharacter() {
        if (partyDied.value!!) return
        _currentCharacterInd.value = (currentCharacterInd + 1) % getPartySize() // Force next character
        var i = 0
        while (!getCharacter(_currentCharacterInd.value!!)!!.isAlive()) { // Find first alive character
            _currentCharacterInd.value = (currentCharacterInd + 1) % getPartySize()
            if (i++ > 3) {
                setPartyHasDied()
                break
            } // whole party is dead, doesn't matter
        }

    }

    fun getRandomCharacter(): Pair<Int, PlayerCharacter> {
        val characters = mutableCharacters.value!!
        val ind = Random().nextInt(characters.size)
        return Pair(ind, characters[ind])
    }

    fun getPartySize(): Int {
        return mutableCharacters.value!!.size
    }

    fun getEnemy(): EnemyCharacter? {
        return mutableEnemy.value
    }

    fun getDungeon(): Dungeon? {
        return mutableDungeon.value
    }

}
