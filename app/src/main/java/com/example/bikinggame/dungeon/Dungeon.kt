package com.example.bikinggame.dungeon

import com.example.bikinggame.enemy.EnemyCharacter
import com.example.bikinggame.playerCharacter.BasicStats
import com.example.bikinggame.playerCharacter.CharacterStats
import org.json.JSONArray

class Dungeon {
    val id: Int

    constructor(pId: Int) {
        id = pId
    }

    constructor(jsonArray: JSONArray) {
        id = jsonArray.get(0) as Int
    }

    fun serialize(): JSONArray {
        val jsonArray = JSONArray()
        jsonArray.put(id)
        return jsonArray
    }

    /**
     *
     * @param difficulty Number 0..5 inclusive to determine enemies to return
     *
    */
    fun rollRandomEnemy(difficulty: Int): EnemyCharacter {
        val modifier: Float = difficulty / 3.0f

        val characterStats = CharacterStats(mutableMapOf(
            BasicStats.BaseHealth to (15 * modifier).toInt(),
            BasicStats.BaseMana to (15 * modifier).toInt(),
            BasicStats.BaseStamina to (15 * modifier).toInt(),
            BasicStats.Strength to (7 * modifier).toInt(),
            BasicStats.Casting to (7 * modifier).toInt(),
            BasicStats.Constitution to (7 * modifier).toInt(),
            BasicStats.Intelligence to (7 * modifier).toInt(),
            BasicStats.Dexterity to (7 * modifier).toInt()
        ))

        return EnemyCharacter(characterStats)
    }

}