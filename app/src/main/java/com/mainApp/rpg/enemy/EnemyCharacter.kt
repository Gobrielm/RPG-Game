package com.mainApp.rpg.enemy

import com.mainApp.rpg.attack.Attack
import com.mainApp.rpg.playerCharacter.CharacterStats
import com.mainApp.rpg.playerCharacter.Shield
import com.mainApp.rpg.playerCharacter.StatusEffect
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

    constructor(enemyCharacter: EnemyCharacter) {
        id = enemyCharacter.id
        name = enemyCharacter.name
        baseStats = CharacterStats(enemyCharacter.baseStats)
        currentStats = CharacterStats(enemyCharacter.currentStats)

        enemyCharacter.attacks.forEachIndexed { index, attack ->
            attacks[index] = attack
        }
        shield = if (enemyCharacter.shield != null) Shield(enemyCharacter.shield!!) else null
    }

    override fun toString(): String {
        return "$id: $currentStats"
    }

    fun getShieldHitPoints(): Int {
        return shield?.getHitPoints() ?: 0
    }

    fun applyModifier(modifier: Float) {
        currentStats.applyModifier(modifier)
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

    fun chooseRandAttack(): Attack? {
        val validAttacks = arrayListOf<Attack>()
        for (attack in attacks) {
            if (attack != null && canChooseAttack(attack)) {
                validAttacks.add(attack)
            }
        }

        return if (validAttacks.isNotEmpty()) validAttacks.random() else null
    }

    fun canChooseAttack(attack: Attack): Boolean {
        if (attack.statCost == null) return true
        val (stat, amt) = attack.statCost
        return currentStats.getPrimaryStat(stat) >= amt
    }

    fun isAlive(): Boolean {
        return currentStats.getHealth() > 0
    }

    fun isDead(): Boolean {
        return currentStats.getHealth() <= 0
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