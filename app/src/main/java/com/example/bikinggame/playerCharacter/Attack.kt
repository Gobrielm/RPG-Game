package com.example.bikinggame.playerCharacter

import org.json.JSONArray
import kotlin.random.Random

class Attack {
    val id: Int
    val name: String
    val mass: Int
    val velocity: Int
    val accuracy: Int // Chance to do direct hit, 0 - 100
    val type: AttackTypes
    val statCost: Pair<BasicStats, Int>?
    val statusEffectInflictChance: Pair<Int, StatusEffect>? // (0 - 100, Status Effect)

    constructor(pId: Int, pName: String, pMass: Int, pVelocity: Int, pAccuracy: Int, pType: AttackTypes) {
        id = pId
        mass = pMass
        name = pName
        velocity = pVelocity
        accuracy = pAccuracy
        type = pType
        statCost = null
        statusEffectInflictChance = null
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
    }

    fun getMomentum(): Int {
        return velocity * mass
    }

    fun getRandomHitType(): HitTypes {
        val rand: Int = Random.nextInt(0, 100)
        val chanceToMiss = 100 - accuracy
        val glanceChance = (accuracy - chanceToMiss) * 0.4 + chanceToMiss
        return if (rand < chanceToMiss) {
            HitTypes.MISS
        } else if (rand < glanceChance) {
            HitTypes.GLANCING_HIT
        } else {
            HitTypes.DIRECT_HIT
        }
    }

    override fun toString(): String {
        return "$name --- Mass: $mass  Velocity: $velocity  Accuracy: $accuracy"
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

            5 to Attack(5, "Poison Arrow", 2, 5, 85, AttackTypes.PHY, Pair<Int, StatusEffect>(100, StatusEffect.getStatusEffect(1)!!))
        )

        fun getAttack(attackID: Int): Attack? {
            return if (attackIDToAttack.contains(attackID)) attackIDToAttack[attackID] else null
        }
    }
}