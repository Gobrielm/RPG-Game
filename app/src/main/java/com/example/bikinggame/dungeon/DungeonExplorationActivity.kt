package com.example.bikinggame.dungeon

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikinggame.characterCreation.ClassChoiceViewModel
import com.example.bikinggame.databinding.ActivityDungeonExplorationBinding
import com.example.bikinggame.enemy.EnemyCharacter
import com.example.bikinggame.playerCharacter.BasicStats
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

        val characterJsonString = intent.getStringExtra("CHARACTER")
        val dungeonJsonString = intent.getStringExtra("DUNGEON")
        if (characterJsonString != null && dungeonJsonString != null) {
            val characterJsonArray = JSONArray(characterJsonString)
            val dungeonJsonArray = JSONArray(dungeonJsonString)
            viewModel.setSelectedCharacter(PlayerCharacter(characterJsonArray))
            viewModel.setDungeon(Dungeon(dungeonJsonArray))
            viewModel.setEnemy(viewModel.getDungeon()!!.rollRandomEnemy(1))
            setStats(viewModel.getSelectedCharacter()!!)
        }

    }

    fun setStats(character: PlayerCharacter) {
        binding.characterUi.healthProgressbar.progress = (character.currentStats.getHealth().toDouble() / character.baseStats.getHealth() * 100).toInt()
        binding.characterUi.manaProgressbar.progress = (character.currentStats.getMana().toDouble() / character.baseStats.getMana() * 100).toInt()
        binding.characterUi.staminaProgressbar.progress = (character.currentStats.getStamina().toDouble() / character.baseStats.getStamina() * 100).toInt()
    }
}

class DungeonExplorationViewModel : ViewModel() {
    private val mutableSelectedCharacter = MutableLiveData<PlayerCharacter>()
    private val mutableEnemy = MutableLiveData<EnemyCharacter>()
    private val mutableDungeon = MutableLiveData<Dungeon>()

    val selectedCharacter: LiveData<PlayerCharacter> get() = mutableSelectedCharacter
    val enemy: LiveData<EnemyCharacter> get() = mutableEnemy
    val dungeon: LiveData<Dungeon> get() = mutableDungeon

    fun setSelectedCharacter(character: PlayerCharacter) {
        mutableSelectedCharacter.value = character
    }

    fun setEnemy(enemy: EnemyCharacter) {
        mutableEnemy.value = enemy
    }

    fun setDungeon(dungeon: Dungeon) {
        mutableDungeon.value = dungeon
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
}
