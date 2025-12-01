package com.example.bikinggame.dungeonExploration

import android.os.Bundle
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.example.bikinggame.R
import com.example.bikinggame.databinding.ActivityDungeonExplorationBinding
import com.example.bikinggame.dungeon.Dungeon
import com.example.bikinggame.dungeon.FiniteDungeon
import com.example.bikinggame.dungeon.DungeonRooms
import com.example.bikinggame.enemy.EnemyCharacter
import com.example.bikinggame.homepage.inventory.PlayerInventory
import com.example.bikinggame.playerCharacter.Attack
import com.example.bikinggame.playerCharacter.PlayerCharacter
import kotlin.getValue

class DungeonExplorationActivity: AppCompatActivity() {

    private lateinit var binding: ActivityDungeonExplorationBinding

    private val viewModel: DungeonExplorationViewModel by viewModels()
    private var currentRoom: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDungeonExplorationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val characterID = intent.getIntExtra("CHARACTER", 0)
        val dungeonID = intent.getIntExtra("DUNGEON", 0)

        viewModel.setSelectedCharacter(PlayerInventory.getCharacter(characterID)!!)
        viewModel.setDungeon(Dungeon.getDungeon(dungeonID)!!)
        viewModel.setEnemy(viewModel.getDungeon()!!.rollRandomEnemy())
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
            moveToNextRoom()
        })

        viewModel.partyDied.observe(this, Observer {
            binding.characterUi.failText.visibility = VISIBLE
            binding.characterUi.blurRect.visibility = VISIBLE
        })

    }

    fun showLootUi(lootEarned: ArrayList<Int>) {
        val container = binding.lootEarnedUi.lootContainer
        container.visibility = VISIBLE
        container.maxRowWidthPx = (300 * resources.displayMetrics.density).toInt() // example

        val size = (60 * resources.displayMetrics.density).toInt()

        for (i in 0 until lootEarned.size) {
            val btn = Button(this).apply {
                text = i.toString()
                layoutParams = ViewGroup.LayoutParams(size, size)
            }
            container.addView(btn)
        }
    }

    fun updateStats() {
        val character = viewModel.getSelectedCharacter()!!
        binding.characterUi.healthProgressbar.progress = (character.currentStats.getHealth().toDouble() / character.baseStats.getHealth() * 100.0).toInt()
        binding.characterUi.manaProgressbar.progress = (character.currentStats.getMana().toDouble() / character.baseStats.getMana() * 100.0).toInt()
        binding.characterUi.staminaProgressbar.progress = (character.currentStats.getStamina().toDouble() / character.baseStats.getStamina() * 100.0).toInt()
    }

    fun setAttacks(character: PlayerCharacter) {
        binding.characterUi.mv1Button.text = (character.getAttack(0)?.name ?: "None")
        binding.characterUi.mv2Button.text = (character.getAttack(1)?.name ?: "None")
        binding.characterUi.mv3Button.text = (character.getAttack(2)?.name ?: "None")
        binding.characterUi.mv4Button.text = (character.getAttack(3)?.name ?: "None")
    }

    fun chooseAttack(mvInd: Int) {
        val playerCharacter = viewModel.getSelectedCharacter()!!
        val attack = playerCharacter.attacks[mvInd]
        if (attack == null) return

        viewModel.setPlayerAttack(attack)
    }

    fun moveToNextRoom() {
        val roomType = viewModel.getDungeon()!!.getRoom(++currentRoom)!!
        val navController = findNavController(R.id.dungeon_navigation)

        when (roomType) {
            DungeonRooms.BOSS ->
                navController.navigate(R.id.regular_room)
            DungeonRooms.TREASURE ->
                navController.navigate(R.id.regular_room)
            DungeonRooms.REST ->
                navController.navigate(R.id.regular_room)
            DungeonRooms.REGULAR ->
                navController.navigate(R.id.regular_room)
        }
    }
}

class DungeonExplorationViewModel : ViewModel() {
    private val mutableSelectedCharacter = MutableLiveData<PlayerCharacter>()
    private val mutableEnemy = MutableLiveData<EnemyCharacter>()
    private val mutableDungeon = MutableLiveData<FiniteDungeon>()
    private val mutablePlayerAttack = MutableLiveData<Attack>()
    private val mutableReadyForNextRoom = MutableLiveData<Boolean>()
    private val mutablePartyDied = MutableLiveData<Boolean>()

    val attack: LiveData<Attack> get() = mutablePlayerAttack
    val readyForNextRoom: LiveData<Boolean> get() = mutableReadyForNextRoom
    val partyDied: LiveData<Boolean> get() = mutablePartyDied

    fun setSelectedCharacter(character: PlayerCharacter) {
        mutableSelectedCharacter.value = character
    }

    fun setEnemy(enemy: EnemyCharacter) {
        mutableEnemy.value = enemy
    }

    fun setDungeon(finiteDungeon: FiniteDungeon) {
        mutableDungeon.value = finiteDungeon
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

    fun getSelectedCharacter(): PlayerCharacter? {
        return mutableSelectedCharacter.value
    }

    fun getEnemy(): EnemyCharacter? {
        return mutableEnemy.value
    }

    fun getDungeon(): FiniteDungeon? {
        return mutableDungeon.value
    }

    fun getPlayerAttack(): Attack? {
        return mutablePlayerAttack.value
    }
}
