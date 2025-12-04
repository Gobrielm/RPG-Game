package com.example.bikinggame.dungeonExploration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import com.example.bikinggame.playerCharacter.PlayerCharacter
import com.example.bikinggame.requests.getUserJson
import com.example.bikinggame.requests.getUserToken
import com.example.bikinggame.requests.makePutRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlin.getValue

class DungeonExplorationActivity: AppCompatActivity() {

    private lateinit var binding: ActivityDungeonExplorationBinding

    private val viewModel: DungeonExplorationViewModel by viewModels()
    private var currentRoom: Int = 0
    private var stopped: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDungeonExplorationBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            viewModel.addSelectedCharacter(PlayerInventory.getCharacter(character2ID)!!)
        }
        if (character2ID != character3ID && character3ID != character1ID && character3ID != -1) {
            binding.characterUi.miniCharacter2Container.visibility = VISIBLE
            viewModel.addSelectedCharacter(PlayerInventory.getCharacter(character3ID)!!)
        }


        viewModel.setDungeon(InfiniteDungeon())
        updateStats()
        setAttacks(viewModel.getSelectedCharacter()!!)


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

        viewModel.readyForNextRoom.observe(this, Observer {
            viewModel.resetSelectedCharacter()
            moveToNextRoom()
        })

        viewModel.partyDied.observe(this, Observer { hasDied ->
            if (!hasDied) return@Observer
            binding.characterUi.failText.visibility = VISIBLE
            binding.characterUi.blurRect.visibility = VISIBLE
            stopped = true
            lifecycleScope.launch {
                delay(2000)
                moveToMainMenu()
            }
        })

        viewModel.partyDone.observe(this, Observer {
            binding.characterUi.finishText.visibility = VISIBLE
            binding.characterUi.blurRect.visibility = VISIBLE
            stopped = true
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

        val btn = Button(this).apply {
            text = "Coins: $coinsEarned"
            layoutParams = ViewGroup.LayoutParams(size, size)
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
            binding.characterUi.healthProgressbar,
            binding.characterUi.manaProgressbar,
            binding.characterUi.staminaProgressbar
        )

        val nextCharacter = viewModel.getNextCharacter()

        if (nextCharacter == null) return

        updateProgressBars(nextCharacter,
            binding.characterUi.miniCharacterUi1.healthProgressbar,
            binding.characterUi.miniCharacterUi1.manaProgressbar,
            binding.characterUi.miniCharacterUi1.staminaProgressbar
        )

        val nextNextCharacter = viewModel.getNextNextCharacter()

        if (nextNextCharacter == null) return

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

    fun setAttacks(character: PlayerCharacter) {
        binding.characterUi.mv1Button.text = (character.getAttack(0)?.name ?: "None")
        binding.characterUi.mv2Button.text = (character.getAttack(1)?.name ?: "None")
        binding.characterUi.mv3Button.text = (character.getAttack(2)?.name ?: "None")
        binding.characterUi.mv4Button.text = (character.getAttack(3)?.name ?: "None")
    }

    fun chooseAttack(mvInd: Int) {
        if (stopped) return
        val playerCharacter = viewModel.getSelectedCharacter()!!
        val attack = playerCharacter.attacks[mvInd]
        if (attack == null) return

        viewModel.setPlayerAttack(attack)
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

    fun moveToNextRoom() {
        if (stopped) return
        blurIn()
        lifecycleScope.launch {
            delay(1000)
            blurOut()
            val roomType = viewModel.getDungeon()!!.getRoom(++currentRoom)!!
            val navController = findNavController(R.id.nav_host_fragment_character_ui)

            when (roomType) {
                DungeonRooms.BOSS ->
                    navController.navigate(R.id.boss_room)

                DungeonRooms.TREASURE ->
                    navController.navigate(R.id.treasure_room)

                DungeonRooms.REST ->
                    navController.navigate(R.id.rest_room)

                else ->
                    navController.navigate(R.id.regular_room)
            }
        }
    }

    fun moveToEndingScreen() {
        unShowLootUi()
        binding.characterUi.mainCharacterUI.visibility = View.GONE

        binding.characterUi.finishText.visibility = View.GONE
        binding.characterUi.blurRect.visibility = View.GONE


        lifecycleScope.launch {

            val token = getUserToken()
            if (token == null) return@launch

            val json = JSONObject()
            json.put("deepestRoom", currentRoom)

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
    private val mutablePlayerAttack = MutableLiveData(Attack(0, "Temp", 0, 0, 0, 0))
    private val mutableReadyForNextRoom = MutableLiveData<Boolean>()
    private val mutablePartyDied = MutableLiveData<Boolean>(false)
    private val mutablePartyDone = MutableLiveData<Boolean>()
    private val lootEarned = MutableLiveData(arrayListOf(0, 0)) // XP first, then gold

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

    fun setPlayerAttack(attack: Attack) {
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
        lootEarned.value!![0] += exp
    }

    fun addCoinsEarned(coins: Int) {
        lootEarned.value!![1] += coins
    }

    fun addLootEarned(lootToAdd: ArrayList<Int>) {
        lootToAdd.forEach { lootID ->
            lootEarned.value!!.add(lootID)
        }
    }

    fun getLootEarned(): ArrayList<Int> {
        return lootEarned.value!!
    }

    fun getSelectedCharacter(): PlayerCharacter? {
        return mutableCharacters.value!![currentCharacterInd]
    }

    fun getNextCharacter(): PlayerCharacter? {
        val next = mutableCharacters.value!![(currentCharacterInd + 1) % mutableCharacters.value!!.size]
        if (next == getSelectedCharacter()) return null
        return next
    }

    fun getNextNextCharacter(): PlayerCharacter? {
        val next = mutableCharacters.value!![(currentCharacterInd + 2) % mutableCharacters.value!!.size]
        if (next == getSelectedCharacter()) return null
        return next
    }

    fun cycleSelectedCharacter() {
        _currentCharacterInd.value = (currentCharacterInd + 1) % mutableCharacters.value!!.size
    }

    fun removeCurrentMember() {
        mutableCharacters.value!!.removeAt(currentCharacterInd)
        if (mutableCharacters.value!!.isNotEmpty()) {
            _currentCharacterInd.value = currentCharacterInd % mutableCharacters.value!!.size
        } else {
            setPartyHasDied()
        }
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
