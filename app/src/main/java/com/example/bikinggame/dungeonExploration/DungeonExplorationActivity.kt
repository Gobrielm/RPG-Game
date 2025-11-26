package com.example.bikinggame.dungeonExploration

import android.os.Bundle
import android.util.Log
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.bikinggame.databinding.ActivityDungeonExplorationBinding
import com.example.bikinggame.dungeon.Dungeon
import com.example.bikinggame.enemy.EnemyCharacter
import com.example.bikinggame.homepage.inventory.PlayerInventory
import com.example.bikinggame.playerCharacter.Attack
import com.example.bikinggame.playerCharacter.PlayerCharacter
import org.json.JSONArray
import kotlin.getValue

class DungeonExplorationActivity: AppCompatActivity() {

    private lateinit var binding: ActivityDungeonExplorationBinding

    private val viewModel: DungeonExplorationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDungeonExplorationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val characterID = intent.getIntExtra("CHARACTER", 0)
        val dungeonID = intent.getIntExtra("DUNGEON", 0)

        viewModel.setSelectedCharacter(PlayerInventory.getCharacter(characterID)!!)
        viewModel.setDungeon(Dungeon.getDungeon(dungeonID)!!)
        viewModel.setEnemy(viewModel.getDungeon()!!.rollRandomEnemy(1))
        setStats(viewModel.getSelectedCharacter()!!)


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

        })

        viewModel.partyDied.observe(this, Observer {
            binding.characterUi.failText.visibility = VISIBLE
            binding.characterUi.blurRect.visibility = VISIBLE
        })

    }

    fun setStats(character: PlayerCharacter) {
        binding.characterUi.healthProgressbar.progress = (character.currentStats.getHealth().toDouble() / character.baseStats.getHealth() * 100).toInt()
        binding.characterUi.manaProgressbar.progress = (character.currentStats.getMana().toDouble() / character.baseStats.getMana() * 100).toInt()
        binding.characterUi.staminaProgressbar.progress = (character.currentStats.getStamina().toDouble() / character.baseStats.getStamina() * 100).toInt()
    }

    fun chooseAttack(mvInd: Int) {
        val playerCharacter = viewModel.getSelectedCharacter()!!
        val attack = playerCharacter.attacks[mvInd]
        if (attack == null) return

        viewModel.setPlayerAttack(attack)
    }
}

class DungeonExplorationViewModel : ViewModel() {
    private val mutableSelectedCharacter = MutableLiveData<PlayerCharacter>()
    private val mutableEnemy = MutableLiveData<EnemyCharacter>()
    private val mutableDungeon = MutableLiveData<Dungeon>()
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

    fun getSelectedCharacter(): PlayerCharacter? {
        return mutableSelectedCharacter.value
    }

    fun getEnemy(): EnemyCharacter? {
        return mutableEnemy.value
    }

    fun getDungeon(): Dungeon? {
        return mutableDungeon.value
    }

    fun getPlayerAttack(): Attack? {
        return mutablePlayerAttack.value
    }
}
