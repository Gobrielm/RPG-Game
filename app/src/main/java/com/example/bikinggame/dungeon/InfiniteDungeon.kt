package com.example.bikinggame.dungeon

import com.example.bikinggame.enemy.EnemyCharacter
import com.example.bikinggame.playerCharacter.Attack
import com.example.bikinggame.playerCharacter.BasicStats
import com.example.bikinggame.playerCharacter.CharacterStats
import java.util.Random

class InfiniteDungeon: Dungeon {

    var difficulty: Int
    var lastRoom: DungeonRooms? = null

    constructor(pDifficulty: Int) {
        difficulty = pDifficulty
    }

    override fun getRoom(roomInd: Int): DungeonRooms? {
        val currentRoom: DungeonRooms = when (lastRoom) {
            DungeonRooms.BOSS -> DungeonRooms.TREASURE
            DungeonRooms.TREASURE -> DungeonRooms.REST
            DungeonRooms.REST -> DungeonRooms.REGULAR

            else -> {
                val odds = arrayOf(DungeonRooms.REGULAR, DungeonRooms.REGULAR, DungeonRooms.REGULAR, DungeonRooms.REGULAR, DungeonRooms.REGULAR, DungeonRooms.REGULAR, DungeonRooms.BOSS, DungeonRooms.TREASURE)
                odds[Random().nextInt(odds.size)]
            }
        }

        lastRoom = currentRoom
        return currentRoom
    }

    override fun rollRandomEnemy(): EnemyCharacter {
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

}