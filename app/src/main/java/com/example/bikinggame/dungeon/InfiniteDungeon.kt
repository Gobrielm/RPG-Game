package com.example.bikinggame.dungeon

import com.example.bikinggame.enemy.EnemyCharacter
import com.example.bikinggame.playerCharacter.Attack
import com.example.bikinggame.playerCharacter.BasicStats
import com.example.bikinggame.playerCharacter.CharacterStats
import com.example.bikinggame.playerCharacter.Shield
import java.util.Random
import kotlin.math.round

class InfiniteDungeon: Dungeon {

    var difficulty: Float = 1.0f
    var lastRoom: DungeonRooms = DungeonRooms.REST

    constructor()

    override fun getRoom(roomInd: Int): DungeonRooms? {
        if (roomInd % 10 == 0 && roomInd != 0) {
            difficulty += 0.5f
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
        val modifier: Float = difficulty

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

        return EnemyCharacter(characterStats,
            arrayListOf(Attack(-1, "AAAA", 1, 5,100, Attack.AttackTypes.PHY)),
            Shield.getShield(1)
        )
    }

    override fun rollRandomBoss(): EnemyCharacter {
        val modifier: Float = difficulty.toFloat()

        val boss: EnemyCharacter = Dungeon.bosses.random()


        return boss
    }

    override fun rollRandomLoot(): ArrayList<Int> {
        return arrayListOf(1) // TODO: Loot generation
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

}