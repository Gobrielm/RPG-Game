package com.example.bikinggame.dungeon

import com.example.bikinggame.enemy.EnemyCharacter
import com.example.bikinggame.attack.Attack
import com.example.bikinggame.playerCharacter.BasicStats
import com.example.bikinggame.playerCharacter.CharacterStats

interface Dungeon {

    fun getRoom(roomInd: Int): DungeonRooms?

    fun rollRandomEnemy(): EnemyCharacter

    fun rollRandomBoss(): EnemyCharacter

    fun rollRandomLoot(): ArrayList<Int>
    fun rollRandomCoins(): Int

    fun getExpForEnemy(): Int

    fun getExpForBoss(): Int

    companion object {

//        val dungeons = mapOf(
//            // Forest Dungeon
//            1 to FiniteDungeon(1, 1, DungeonLayout(5, 0, 0.15f)),
//
//            // Cave Dungeon
//            2 to FiniteDungeon(2, 2, DungeonLayout(7, 1, 0.15f)),
//        )

        val bosses = arrayListOf(
            EnemyCharacter("Rock Golem", CharacterStats(mutableMapOf(
                BasicStats.BaseHealth to (100),
                BasicStats.BaseMana to (15),
                BasicStats.BaseStamina to (10),
                BasicStats.Strength to (20),
                BasicStats.Casting to (3),
                BasicStats.Constitution to (20),
                BasicStats.Intelligence to (3),
                BasicStats.Dexterity to (3)
            )), arrayListOf(
                Attack(-1, "Boulder Smash", 10, 4, 80, Attack.AttackTypes.PHY),
                Attack(-1, "Rock Throw", 4, 5, 100, Attack.AttackTypes.RAN),
                Attack(-1, "AAAA", 1, 5, 100, Attack.AttackTypes.PHY),
                Attack(-1, "AAAA", 1, 5, 100, Attack.AttackTypes.PHY)
            ))
        )

//        fun getDungeon(dungeonID: Int): FiniteDungeon? {
//            return dungeons[dungeonID]
//        }
    }
}