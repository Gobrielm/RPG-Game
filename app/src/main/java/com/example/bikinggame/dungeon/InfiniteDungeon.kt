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
        if (roomInd % 10 == 9) {
            return DungeonRooms.BOSS
        }

        val currentRoom: DungeonRooms = when (lastRoom) {
            DungeonRooms.BOSS -> DungeonRooms.TREASURE
            DungeonRooms.TREASURE -> DungeonRooms.REST
            DungeonRooms.REST -> DungeonRooms.REGULAR

            else -> {
                val odds = arrayOf(DungeonRooms.REGULAR, DungeonRooms.REGULAR, DungeonRooms.REGULAR, DungeonRooms.REGULAR, DungeonRooms.REGULAR, DungeonRooms.REGULAR, DungeonRooms.TREASURE)
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
        val lootAvail = arrayListOf<Int>()
        for ((itemID, levelAvail) in lootAvailable) {
            if (roomInd >= levelAvail) {
                lootAvail.add(itemID)
            }
        }

        val loot = ArrayList<Int>()
        for (i in 0 until Random().nextInt(3) + 1) {
            loot.add(lootAvail.random())
        }

        return loot
    }

    override fun rollRandomCoins(): Int {
        return (difficulty * (Random().nextInt(30) + 5)).toInt()
    }

    override fun getExpForEnemy(): Int {
        return round((Random().nextInt(4) + 8) * difficulty).toInt()
    }

    override fun getExpForBoss(): Int {
        return round(Random().nextInt(20) + 30 * difficulty).toInt()
    }

    companion object {
        // Equipment ID to first Level available
        val lootAvailable = mapOf(
            1 to 0,
            2 to 0,
            3 to 0,

            4 to 10,
            5 to 10,
            6 to 10,

            7 to 10,
            8 to 10,
            9 to 10,

            10 to 10,
            11 to 10,
            12 to 10,

            13 to 0,
            14 to 10,
            15 to 10,

            16 to 0,
            17 to 10,
            18 to 10,
            19 to 0,

            20 to 0,
            21 to 0,
            22 to 10,
            23 to 10,

            24 to 0,
            25 to 0,
            26 to 10,
            27 to 10,

            28 to 0,
            29 to 0,
            30 to 10,
            31 to 10,

            32 to 0,
            33 to 0,
            34 to 0,

            35 to 10,
            36 to 10,
            37 to 10,

            38 to 10,
            39 to 10,
            40 to 10,
        )
    }
}