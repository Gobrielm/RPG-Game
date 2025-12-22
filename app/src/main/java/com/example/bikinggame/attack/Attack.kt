package com.example.bikinggame.attack

import com.example.bikinggame.playerCharacter.BasicStats
import com.example.bikinggame.playerCharacter.StatusEffect
import kotlin.math.min
import kotlin.random.Random

class Attack {
    // TODO: Figure out how to allow friend attacks, either complexity or interface
    val id: Int
    val name: String
    val mass: Int
    val velocity: Int
    val accuracy: Int // Chance to do direct hit, 0 - 100
    val type: AttackTypes
    val statCost: Pair<BasicStats, Int>?
    val statusEffectInflictChance: Pair<Int, StatusEffect>? // (0 - 100, Status Effect)
    val friendlyAttack: Boolean

    constructor(pId: Int, pName: String, pMass: Int, pVelocity: Int, pAccuracy: Int, pType: AttackTypes) {
        id = pId
        mass = pMass
        name = pName
        velocity = pVelocity
        accuracy = pAccuracy
        type = pType
        statCost = null
        statusEffectInflictChance = null
        friendlyAttack = false
    }

    constructor(pId: Int, pName: String, pMass: Int, pVelocity: Int, pAccuracy: Int,
                pType: AttackTypes, pStatCost: Pair<BasicStats, Int>?, pStatusEffectInflictChance: Pair<Int, StatusEffect>? = null) {
        id = pId
        mass = pMass
        name = pName
        velocity = pVelocity
        accuracy = pAccuracy
        type = pType
        statCost = pStatCost
        statusEffectInflictChance = pStatusEffectInflictChance
        friendlyAttack = false
    }

    constructor(pId: Int, pName: String, pMass: Int, pVelocity: Int, pAccuracy: Int,
                pType: AttackTypes, pStatusEffectInflictChance: Pair<Int, StatusEffect>) {
        id = pId
        mass = pMass
        name = pName
        velocity = pVelocity
        accuracy = pAccuracy
        type = pType
        statCost = null
        statusEffectInflictChance = pStatusEffectInflictChance
        friendlyAttack = false
    }

    /**
     * Used for Creating Friendly Attacks
     */
    constructor(pId: Int, pName: String, pHealing: Int, pStatCost: Pair<BasicStats, Int>?,
                pStatusEffectInflictChance: Pair<Int, StatusEffect>?) {
        id = pId
        mass = 1
        name = pName
        velocity = pHealing
        accuracy = 100
        type = AttackTypes.MAG
        statCost = pStatCost
        statusEffectInflictChance = pStatusEffectInflictChance
        friendlyAttack = true
    }

    fun getMomentum(): Int {
        return velocity * mass
    }

    fun getRandomHitType(): HitTypes {
        val rand: Int = Random.Default.nextInt(0, 100)
        val chanceToMiss = 100 - accuracy
        val glanceChance = min((accuracy - chanceToMiss) * 0.3, 20.0) + chanceToMiss
        return if (rand < chanceToMiss) {
            HitTypes.MISS
        } else if (rand < glanceChance) {
            HitTypes.GLANCING_HIT
        } else {
            HitTypes.DIRECT_HIT
        }
    }

    override fun toString(): String {
        var str = if (friendlyAttack) {
            "$name --- Healing: ${getHealing()}\n"
        } else {
            "$name --- Mass: $mass  Velocity: $velocity  Accuracy: $accuracy\n"
        }


        if (statusEffectInflictChance != null) {
            str += "Inflicts: ${statusEffectInflictChance.second.name} ${statusEffectInflictChance.first}% Chance"
        }
        return str
    }

    // For Attack as Healing

    fun getHealing(): Int {
        if (!friendlyAttack) return 0
        return getMomentum()
    }

    enum class HitTypes {
        DIRECT_HIT,
        GLANCING_HIT,
        MISS;

        override fun toString(): String {
            if (this == DIRECT_HIT) return "Direct Hit"
            if (this == GLANCING_HIT) return "Glancing Hit"
            return "Miss"
        }

        fun getMultiplier(): Float {
            if (this == DIRECT_HIT) return 1.0f
            if (this == GLANCING_HIT) return 0.5f
            return 0f
        }
    }

    enum class AttackTypes {
        PHY,
        MAG,
        RAN;

        companion object {
            fun fromInt(value: Int): AttackTypes? {
                return AttackTypes.entries.find { it.ordinal == value }
            }
        }
    }

    companion object {
        val attackIDToAttack = hashMapOf<Int, Attack>(
            1 to Attack(1, "Basic Hit", 2, 5, 90, AttackTypes.PHY),

            2 to Attack(2, "Mana Blast", 1, 8, 80, AttackTypes.MAG, Pair(BasicStats.BaseMana, 3)),

            3 to Attack(3, "Normal Shot", 1, 7, 85, AttackTypes.RAN),

            4 to Attack(4, "Intermediate Hit", 4, 5, 90, AttackTypes.PHY),

            5 to Attack(5, "Poison Arrow", 2, 5, 85, AttackTypes.PHY, Pair<Int, StatusEffect>(100, StatusEffect.Companion.getStatusEffect(1)!!)),

            6 to Attack(6, "Healing Touch", 8, Pair(BasicStats.BaseMana, 4),null)
        )


        fun getAttack(attackID: Int): Attack? {
            return if (attackIDToAttack.contains(attackID)) attackIDToAttack[attackID] else null
        }
    }
}