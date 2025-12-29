package com.mainApp.rpg.dungeon

import com.mainApp.rpg.enemy.EnemyCharacter
import com.mainApp.rpg.attack.Attack
import com.mainApp.rpg.playerCharacter.BasicStats
import com.mainApp.rpg.playerCharacter.CharacterStats
import com.mainApp.rpg.playerCharacter.StatusEffect

interface Dungeon {

    fun getRoom(roomInd: Int): DungeonRooms?

    fun rollRandomEnemy(): EnemyCharacter

    fun rollRandomBoss(): EnemyCharacter

    fun rollRandomLoot(roomInd: Int): ArrayList<Int>
    fun rollRandomCoins(): Int

    fun getExpForEnemy(): Int

    fun getExpForBoss(): Int

    companion object {

        val enemies = arrayOf(
            EnemyCharacter("Goblin", CharacterStats(mutableMapOf(
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
            )),
            EnemyCharacter("Slime", CharacterStats(mutableMapOf(
                BasicStats.BaseHealth to (12),
                BasicStats.BaseMana to (12),
                BasicStats.BaseStamina to (12),
                BasicStats.Strength to (6),
                BasicStats.Casting to (6),
                BasicStats.Constitution to (6),
                BasicStats.Intelligence to (6),
                BasicStats.Dexterity to (6)
            )), arrayListOf(
                Attack.getAttack(4)!!,
                Attack.getAttack(1)!!,
            )),
            EnemyCharacter("Witch", CharacterStats(mutableMapOf(
                BasicStats.BaseHealth to (8),
                BasicStats.BaseMana to (15),
                BasicStats.BaseStamina to (6),
                BasicStats.Strength to (2),
                BasicStats.Casting to (10),
                BasicStats.Constitution to (2),
                BasicStats.Intelligence to (8),
                BasicStats.Dexterity to (2)
            )), arrayListOf(
                Attack.getAttack(12)!!,
                Attack.getAttack(13)!!,
            ))

        )

        val bosses = arrayOf(
            EnemyCharacter("Rock Golem", CharacterStats(mutableMapOf(
                BasicStats.BaseHealth to (40),
                BasicStats.BaseMana to (15),
                BasicStats.BaseStamina to (10),
                BasicStats.Strength to (20),
                BasicStats.Casting to (3),
                BasicStats.Constitution to (10),
                BasicStats.Intelligence to (3),
                BasicStats.Dexterity to (3)
            )), arrayListOf(
                Attack(-1, "Boulder Smash", 10, 4, 70, Attack.AttackTypes.PHY),
                Attack(-1, "Rock Throw", 4, 5, 85, Attack.AttackTypes.RAN),
                Attack(-1, "Rock Tumble", 5, 5, 85, Attack.AttackTypes.PHY)

            )),
            EnemyCharacter("Goblin King", CharacterStats(mutableMapOf(
                BasicStats.BaseHealth to (30),
                BasicStats.BaseMana to (14),
                BasicStats.BaseStamina to (14),
                BasicStats.Strength to (16),
                BasicStats.Casting to (6),
                BasicStats.Constitution to (8),
                BasicStats.Intelligence to (6),
                BasicStats.Dexterity to (12)
            )), arrayListOf(
                Attack.getAttack(24)!!,
                Attack.getAttack(11)!!,
                Attack.getAttack(22)!!,
            ))
        )

        fun getRandomEnemy(): EnemyCharacter {
            return EnemyCharacter(enemies.random())
        }

        fun getRandomBoss(): EnemyCharacter {
            return EnemyCharacter(bosses.random())
        }
    }
}