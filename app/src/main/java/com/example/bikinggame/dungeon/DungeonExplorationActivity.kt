package com.example.bikinggame.dungeon

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.bikinggame.databinding.ActivityDungeonExplorationBinding
import com.example.bikinggame.playerCharacter.BasicStats
import com.example.bikinggame.playerCharacter.PlayerCharacter
import org.json.JSONArray

class DungeonExplorationActivity: AppCompatActivity() {

    private lateinit var binding: ActivityDungeonExplorationBinding
    private var character: PlayerCharacter? = null
    private var dungeon: Dungeon? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDungeonExplorationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val characterJsonString = intent.getStringExtra("CHARACTER")
        val dungeonJsonString = intent.getStringExtra("DUNGEON")
        if (characterJsonString != null && dungeonJsonString != null) {
            val characterJsonArray = JSONArray(characterJsonString)
            val dungeonJsonArray = JSONArray(dungeonJsonString)
            character = PlayerCharacter(characterJsonArray)
            dungeon = Dungeon(dungeonJsonArray)
            Log.d("AAAAAA", character.toString())
            setStats(character!!)
        }

    }

    fun setStats(character: PlayerCharacter) {
        binding.characterUi.healthProgressbar.progress = (character.currentStats.getHealth().toDouble() / character.baseStats.getHealth() * 100).toInt()
        binding.characterUi.manaProgressbar.progress = (character.currentStats.getMana().toDouble() / character.baseStats.getMana() * 100).toInt()
        binding.characterUi.staminaProgressbar.progress = (character.currentStats.getStamina().toDouble() / character.baseStats.getStamina() * 100).toInt()
    }
}