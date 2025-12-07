package com.example.bikinggame.enemy

import com.example.bikinggame.playerCharacter.Attack
import com.example.bikinggame.playerCharacter.CharacterStats
import kotlin.math.abs
import kotlin.random.Random

class EnemyCharacter {
    val id: Int
    var baseStats: CharacterStats = CharacterStats()
    var currentStats: CharacterStats = CharacterStats()

    var attacks: Array<Attack?> = arrayOfNulls(4)

    // Used for creating the first character
    constructor(pId: Int, pBaseStats: CharacterStats, pAttacks: ArrayList<Attack>) {
        id = pId
        baseStats = pBaseStats
        currentStats = CharacterStats(baseStats)
        pAttacks.forEachIndexed { index, attack ->
            attacks[index] = attack
        }
    }

    constructor(pBaseStats: CharacterStats, pAttacks: ArrayList<Attack>) {
        id = abs(Random.nextInt())
        baseStats = pBaseStats
        currentStats = CharacterStats(baseStats)
        pAttacks.forEachIndexed { index, attack ->
            attacks[index] = attack
        }
    }

    override fun toString(): String {
        return "$id: $currentStats"
    }

    /**
     *  @return (Whether or not this character has gone below 0 health, Msg of Attack)
     */
    fun takeAttack(damage: Int, hitType: Attack.HitTypes): Pair<Boolean, String> {
        return Pair(currentStats.getAttacked(damage, hitType), "Direct Attack")
    }

    fun calculateDamageForAttack(attack: Attack): Pair<Int, Attack.HitTypes> {
        return currentStats.calculateDamageForAttack(attack)
    }

    fun chooseRandAttack(): Attack {
        var numValidAttacks = 0
        for (attack in attacks) {
            if (attack != null) numValidAttacks++
        }

        return attacks[Random.nextInt(0, numValidAttacks)]!!
    }
}