package com.example.bikinggame.dungeon

import com.example.bikinggame.enemy.EnemyCharacter
import com.example.bikinggame.playerCharacter.Attack
import com.example.bikinggame.playerCharacter.BasicStats
import com.example.bikinggame.playerCharacter.CharacterStats
import org.json.JSONArray

class Dungeon {
    val id: Int
    val difficulty: Int
    val dungeonLayout: DungeonLayout


    constructor(pId: Int, pDifficulty: Int, pDungeonLayout: DungeonLayout) {
        id = pId
        difficulty = pDifficulty
        dungeonLayout = pDungeonLayout
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


        return EnemyCharacter(characterStats, arrayListOf(Attack(-1, "AAAA", 10, 5, 0, 100)))
    }

    companion object {

        val dungeons = mapOf(
            // Forest Dungeon
            1 to Dungeon(1, 1, DungeonLayout(5, 0, 0.15f)),

            // Cave Dungeon
            2 to Dungeon(2, 2, DungeonLayout(7, 1, 0.15f)),
        )

        fun getDungeon(dungeonID: Int): Dungeon? {
            return dungeons[dungeonID]
        }
    }

}