package com.example.bikinggame.dungeon

import com.example.bikinggame.enemy.EnemyCharacter
import com.example.bikinggame.attack.Attack
import com.example.bikinggame.playerCharacter.BasicStats
import com.example.bikinggame.playerCharacter.CharacterStats
import com.example.bikinggame.playerCharacter.StatusEffect

interface Dungeon {

    fun getRoom(roomInd: Int): DungeonRooms?

    fun rollRandomEnemy(): EnemyCharacter

    fun rollRandomBoss(): EnemyCharacter

    fun rollRandomLoot(roomInd: Int): ArrayList<Int>
    fun rollRandomCoins(): Int

    fun getExpForEnemy(): Int

    fun getExpForBoss(): Int

    companion object {

        val enemies = mapOf(
            1 to EnemyCharacter("Goblin", CharacterStats(mutableMapOf(
                BasicStats.BaseHealth to (15),
                BasicStats.BaseMana to (7),
                BasicStats.BaseStamina to (7),
                BasicStats.Strength to (8),
                BasicStats.Casting to (3),
                BasicStats.Constitution to (4),
                BasicStats.Intelligence to (3),
                BasicStats.Dexterity to (6)
            )), arrayListOf(
                Attack(-1, "Dagger Attack", 3, 4, 90, Attack.AttackTypes.PHY),
                Attack(-1, "Poison Dart", 1, 5, 85, Attack.AttackTypes.RAN, Pair(100, StatusEffect.getStatusEffect(1)!!))
            ))
        )

        val bosses = mapOf(
            1 to EnemyCharacter("Rock Golem", CharacterStats(mutableMapOf(
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

        fun getRandomEnemy(): EnemyCharacter {
            val randInd = enemies.keys.random()
            return EnemyCharacter(enemies[randInd]!!)
        }

        fun getRandomBoss(): EnemyCharacter {
            val randInd = bosses.keys.random()
            return EnemyCharacter(bosses[randInd]!!)
        }

        fun getEnemy(id: Int): EnemyCharacter? {
            if (enemies.contains(0)) return null
            return EnemyCharacter(enemies[id]!!)
        }

        fun getBoss(id: Int): EnemyCharacter? {
            if (bosses.contains(0)) return null
            return EnemyCharacter(bosses[id]!!)
        }

    }
}