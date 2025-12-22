package com.example.bikinggame.enemy

import com.example.bikinggame.attack.Attack
import com.example.bikinggame.playerCharacter.CharacterStats
import com.example.bikinggame.playerCharacter.Shield
import com.example.bikinggame.playerCharacter.StatusEffect
import kotlin.math.abs
import kotlin.random.Random

class EnemyCharacter {
    val id: Int
    var baseStats: CharacterStats = CharacterStats()
    var currentStats: CharacterStats = CharacterStats()

    var attacks: Array<Attack?> = arrayOfNulls(4)
    var shield: Shield? = null
    val name: String

    constructor(pName: String, pBaseStats: CharacterStats, pAttacks: ArrayList<Attack>, pShield: Shield? = null) {
        id = abs(Random.nextInt())
        name = pName
        baseStats = pBaseStats
        currentStats = CharacterStats(baseStats)
        pAttacks.forEachIndexed { index, attack ->
            attacks[index] = attack
        }
        shield = pShield
    }

    override fun toString(): String {
        return "$id: $currentStats"
    }

    fun getShieldHitPoints(): Int {
        return shield?.getHitPoints() ?: 0
    }

    /**
     *  @return (Msg of Attack)
     */
    fun takeAttack(damage: Int, attack: Attack, hitType: Attack.HitTypes): Pair<Int, String> {
        var damage: Int = damage
        var msg = ""
        var canDodge = true // Can either dodge or use shield
        var blocked = 0

        if (getShieldHitPoints() > 0) {
            canDodge = false
            blocked = getShieldHitPoints()
            val (newDamage, newMsg) = shield!!.blockHit(attack, damage, hitType)
            blocked = blocked - getShieldHitPoints()
            damage = newDamage
            msg = newMsg
        }

        val (damageTaken, otherMsg) = currentStats.getAttacked(damage, attack, hitType, canDodge)

        return Pair(damageTaken + blocked, msg.ifEmpty { otherMsg })
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

    fun isAlive(): Boolean {
        return currentStats.getHealth() > 0
    }

    fun updateNewTurn() {
        currentStats.regenStamina(baseStats.getStamina())
        currentStats.regenMana(baseStats.getMana())
        currentStats.updateNewTurn()
    }

    fun getStatusEffects(): ArrayList<StatusEffect> {
        return currentStats.getStatusEffects()
    }
}