package com.example.bikinggame.dungeon

import com.example.bikinggame.enemy.EnemyCharacter
import com.example.bikinggame.attack.Attack
import com.example.bikinggame.playerCharacter.BasicStats
import com.example.bikinggame.playerCharacter.CharacterStats
import com.example.bikinggame.playerCharacter.Shield
import java.util.Random
import kotlin.math.round

class InfiniteDungeon: Dungeon {

    var difficulty: Float = 1.0f
    var lastRoom: DungeonRooms = DungeonRooms.REGULAR

    constructor()

    override fun getRoom(roomInd: Int): DungeonRooms? {
        if (roomInd % 10 == 0 && roomInd != 0) {
            difficulty += 0.4f
        }
        val currentRoom: DungeonRooms = when (lastRoom) {
            DungeonRooms.BOSS -> DungeonRooms.TREASURE
            DungeonRooms.TREASURE -> DungeonRooms.REST
            DungeonRooms.REST -> DungeonRooms.REGULAR

            else -> {
                val odds = arrayOf(DungeonRooms.REGULAR, DungeonRooms.REGULAR, DungeonRooms.REGULAR, DungeonRooms.REGULAR, DungeonRooms.REGULAR, DungeonRooms.REGULAR, DungeonRooms.BOSS, DungeonRooms.TREASURE)
                DungeonRooms.TREASURE
//                odds[Random().nextInt(odds.size)]
            }
        }

        lastRoom = currentRoom
        return currentRoom
    }

    override fun rollRandomEnemy(): EnemyCharacter {
        val modifier: Float = difficulty.toFloat()

        val enemy: EnemyCharacter = Dungeon.getRandomEnemy()

        return enemy
    }

    override fun rollRandomBoss(): EnemyCharacter {
        val modifier: Float = difficulty.toFloat()

        val boss: EnemyCharacter = Dungeon.getRandomBoss()

        return boss
    }

    override fun rollRandomLoot(roomInd: Int): ArrayList<Int> {
        val a = arrayListOf<Int>()
        for ((levelAvail, itemID) in lootAvailable) {
            if (roomInd >= levelAvail) {
                a.add(itemID)
            }
        }
        return a
    }

    override fun rollRandomCoins(): Int {
        return (difficulty * (Random().nextInt(30) + 5)).toInt()
    }

    override fun getExpForEnemy(): Int {
        return round(10 * difficulty).toInt()
    }

    override fun getExpForBoss(): Int {
        return round(40 * difficulty).toInt()
    }

    companion object {
        // First Level available to Equipment ID
        val lootAvailable = mapOf(
            0 to 1 // TODO: Loot generation
        )
    }
}