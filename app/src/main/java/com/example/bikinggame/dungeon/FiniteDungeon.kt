package com.example.bikinggame.dungeon

import com.example.bikinggame.enemy.EnemyCharacter
import com.example.bikinggame.playerCharacter.Attack
import com.example.bikinggame.playerCharacter.BasicStats
import com.example.bikinggame.playerCharacter.CharacterStats

class FiniteDungeon: Dungeon {
    val id: Int
    val difficulty: Int
    val dungeonLayout: DungeonLayout


    constructor(pId: Int, pDifficulty: Int, pDungeonLayout: DungeonLayout) {
        id = pId
        difficulty = pDifficulty
        dungeonLayout = pDungeonLayout
    }

    override fun getRoom(roomInd: Int): DungeonRooms? {
        if (dungeonLayout.totalRooms > roomInd && roomInd >= 0) {
            return dungeonLayout.getRoom(roomInd)
        }
        return null
    }

    /**
     *
     * @param difficulty Number 0..5 inclusive to determine enemies to return
     *
    */
    override fun rollRandomEnemy(): EnemyCharacter {
        val modifier: Float = difficulty.toFloat()

        val characterStats = CharacterStats(mutableMapOf(
            BasicStats.BaseHealth to (40 * modifier).toInt(),
            BasicStats.BaseMana to (15 * modifier).toInt(),
            BasicStats.BaseStamina to (15 * modifier).toInt(),
            BasicStats.Strength to (7 * modifier).toInt(),
            BasicStats.Casting to (7 * modifier).toInt(),
            BasicStats.Constitution to (7 * modifier).toInt(),
            BasicStats.Intelligence to (7 * modifier).toInt(),
            BasicStats.Dexterity to (7 * modifier).toInt()
        ))


        return EnemyCharacter(characterStats, arrayListOf(Attack(-1, "AAAA", 1, 5, 0, 100)))
    }
}