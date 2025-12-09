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
import androidx.appcompat.widget.AppCompatButton
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.bikinggame.R
import com.example.bikinggame.databinding.ActivityDungeonExplorationBinding
import com.example.bikinggame.dungeon.Dungeon
import com.example.bikinggame.dungeon.DungeonRooms
import com.example.bikinggame.dungeon.InfiniteDungeon
import com.example.bikinggame.enemy.EnemyCharacter
import com.example.bikinggame.homepage.HomePage
import com.example.bikinggame.homepage.inventory.PlayerInventory
import com.example.bikinggame.playerCharacter.Attack
import com.example.bikinggame.playerCharacter.Attack.AttackTypes
import com.example.bikinggame.playerCharacter.PlayerCharacter
import com.example.bikinggame.requests.getUserJson
import com.example.bikinggame.requests.getUserName
import com.example.bikinggame.requests.getUserToken
import com.example.bikinggame.requests.makePutRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.set
import kotlin.getValue

class DungeonExplorationActivity: AppCompatActivity() {

    private lateinit var binding: ActivityDungeonExplorationBinding

    private val viewModel: DungeonExplorationViewModel by viewModels()
    private var currentRoom: Int = 0
    private var finished: Boolean = false
    private var pauseInputs: Boolean = false

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
            binding.characterUi.miniCharacter1Container.visibility = VISIBLE
            val character = PlayerInventory.getCharacter(character2ID)!!
            viewModel.addSelectedCharacter(character)
        }
        if (character2ID != character3ID && character3ID != character1ID && character3ID != -1) {
            binding.characterUi.miniCharacter2Container.visibility = VISIBLE
            val character = PlayerInventory.getCharacter(character3ID)!!
            viewModel.addSelectedCharacter(character)
        }


        viewModel.setDungeon(InfiniteDungeon())
        updateStats()
        setAttacks()


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
            viewModel.resetSelectedCharacter()
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
        val character = viewModel.getSelectedCharacter()!!

        updateProgressBars(character,
            binding.characterUi.mainHealthProgressbar,
            binding.characterUi.mainManaProgressbar,
            binding.characterUi.mainStaminaProgressbar
        )

        val nextCharacter = viewModel.getNextCharacter()

        if (nextCharacter == null) return

        binding.characterUi.miniCharacterUi1.textBox.text = nextCharacter.playerClass.mainClass.toString()
        updateProgressBars(nextCharacter,
            binding.characterUi.miniCharacterUi1.healthProgressbar,
            binding.characterUi.miniCharacterUi1.manaProgressbar,
            binding.characterUi.miniCharacterUi1.staminaProgressbar
        )

        val nextNextCharacter = viewModel.getNextNextCharacter()

        if (nextNextCharacter == null) return

        binding.characterUi.miniCharacterUi2.textBox.text = nextNextCharacter.playerClass.mainClass.toString()
        updateProgressBars(nextNextCharacter,
            binding.characterUi.miniCharacterUi2.healthProgressbar,
            binding.characterUi.miniCharacterUi2.manaProgressbar,
            binding.characterUi.miniCharacterUi2.staminaProgressbar
        )
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

            when (roomType) {
                DungeonRooms.BOSS ->
                    navController.navigate(R.id.regular_room, bundleOf("boss" to true))

                DungeonRooms.TREASURE ->
                    navController.navigate(R.id.treasure_room)

                DungeonRooms.REST ->
                    navController.navigate(R.id.rest_room)

                else ->
                    navController.navigate(R.id.regular_room, bundleOf("boss" to false))
            }
            pauseInputs = false
            updateStats()
            setAttacks()
        }
    }

    fun moveToEndingScreen() {
        unShowLootUi()
        binding.characterUi.mainCharacterUI.visibility = View.GONE

        binding.characterUi.finishText.visibility = View.GONE
        binding.characterUi.blurRect.visibility = View.GONE
        binding.characterUi.miniCharacter1Container.visibility = View.GONE
        binding.characterUi.miniCharacter2Container.visibility = View.GONE
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
    private val mutablePartyDied = MutableLiveData<Boolean>(false)
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

    fun getSelectedCharacter(): PlayerCharacter? {
        return mutableCharacters.value!![currentCharacterInd]
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
        Log.d("AAA", currentCharacterInd.toString())
        _currentCharacterInd.value = (currentCharacterInd + 1) % getPartySize()
        Log.d("AAA", currentCharacterInd.toString())
    }

    fun removeCurrentMember() {
        mutableCharacters.value!!.removeAt(currentCharacterInd)
        if (mutableCharacters.value!!.isNotEmpty()) {
            _currentCharacterInd.value = currentCharacterInd % getPartySize()
        } else {
            setPartyHasDied()
        }
    }

    fun getPartySize(): Int {
        return mutableCharacters.value!!.size
    }

    fun resetSelectedCharacter() {
        _currentCharacterInd.value = 0
    }

    fun getEnemy(): EnemyCharacter? {
        return mutableEnemy.value
    }

    fun getDungeon(): Dungeon? {
        return mutableDungeon.value
    }

}
