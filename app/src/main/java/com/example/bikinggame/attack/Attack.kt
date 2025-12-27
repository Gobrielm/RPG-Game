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
            4 to Attack(4, "Intermediate Hit", 4, 5, 90, AttackTypes.PHY),
            7 to Attack(7, "Bear Strike", 10, 3, 80, AttackTypes.PHY),
            21 to Attack(21, "Weakening Strike", 4, 5, 80, AttackTypes.PHY,
                Pair(BasicStats.BaseMana, 6),
                Pair(100, StatusEffect.getStatusEffect(2)!!)
            ),
            22 to Attack(22, "Overpowering Blow", 7, 5, 85, AttackTypes.PHY),
            23 to Attack(23, "Suffering Strike", 10, 7, 85, AttackTypes.PHY,
                Pair(BasicStats.BaseHealth, 10), null
            ),
            24 to Attack(24, "Piercing Hit", 3, 10, 85, AttackTypes.PHY),
            25 to Attack(25, "Bludgeoning Blow", 11, 6, 85, AttackTypes.PHY),
            26 to Attack(26, "Speed Attack", 6, 10, 85, AttackTypes.PHY),
            27 to Attack(27, "Flaming Sword", 9, 9, 85, AttackTypes.PHY,
                Pair(BasicStats.BaseMana, 20), null
            ),

            3 to Attack(3, "Normal Shot", 1, 7, 85, AttackTypes.RAN),
            5 to Attack(5, "Poison Arrow", 4, 8, 85, AttackTypes.PHY,
                Pair(100,
                    StatusEffect.Companion.getStatusEffect(1)!!)
            ),
            8 to Attack(8, "Eagle Shot", 3, 10, 85, AttackTypes.PHY),
            11 to Attack(11, "Knife Stab", 3, 6, 90, AttackTypes.PHY,
                Pair(100, StatusEffect.getStatusEffect(4)!!)
            ),
            28 to Attack(28, "Barrage Attack", 5, 7, 95, AttackTypes.PHY),
            29 to Attack(29, "Weakening Shots", 3, 5, 90, AttackTypes.PHY,
                Pair(BasicStats.BaseMana, 4),
                Pair(100, StatusEffect.getStatusEffect(9)!!)
            ),
            30 to Attack(30, "Mana-Drain Shots", 3, 6, 90, AttackTypes.PHY,
                Pair(BasicStats.BaseMana, 4),
                Pair(100, StatusEffect.getStatusEffect(6)!!)
            ),
            31 to Attack(31, "Elemental Arrow", 5, 10, 85, AttackTypes.PHY,
                Pair(BasicStats.BaseMana, 10), null
            ),
            32 to Attack(32, "Venom Arrow", 7, 8, 85, AttackTypes.PHY,
                Pair(BasicStats.BaseMana, 10),
                Pair(90, StatusEffect.getStatusEffect(10)!!)
            ),
            33 to Attack(33, "Lightning Arrow", 5, 15, 90, AttackTypes.PHY,),
            34 to Attack(34, "Ballista Shot", 7, 12, 90, AttackTypes.PHY),

            2 to Attack(2, "Mana Blast", 1, 8, 80, AttackTypes.MAG, Pair(BasicStats.BaseMana, 3)),
            6 to Attack(6, "Healing Touch", 8, Pair(BasicStats.BaseMana, 4), null),
            9 to Attack(9, "Forest Blast", 5, 6, 85, AttackTypes.MAG, Pair(BasicStats.BaseMana, 6)),
            10 to Attack(10, "Burning Wisp", 3, 3, 80, AttackTypes.MAG,
                Pair(BasicStats.BaseMana, 4),
                Pair(80, StatusEffect.getStatusEffect(3)!!)
            ),
            12 to Attack(12, "Fireball", 4, 4, 80, AttackTypes.MAG,
                Pair(BasicStats.BaseMana, 6),
                Pair(70, StatusEffect.getStatusEffect(3)!!)
            ),
            13 to Attack(13, "Frost Snap", 5, 3, 85, AttackTypes.MAG,
                Pair(BasicStats.BaseMana, 6),
                Pair(70, StatusEffect.getStatusEffect(5)!!)
            ),
            14 to Attack(14, "Drain Attack", 5, 5, 80, AttackTypes.MAG,
                Pair(BasicStats.BaseMana, 9),
                Pair(70, StatusEffect.getStatusEffect(6)!!)
            ),
            15 to Attack(15, "Super Mana Blast", 8, 5, 90, AttackTypes.MAG,
                Pair(BasicStats.BaseMana, 13), null
            ),
            16 to Attack(16, "Improved Healing", 25,
                Pair(BasicStats.BaseMana, 10), null
            ),
            17 to Attack(17, "Blizzard Strike", 8, 6, 80, AttackTypes.MAG,
                Pair(BasicStats.BaseMana, 15),
                Pair(60, StatusEffect.getStatusEffect(8)!!)
            ),
            18 to Attack(18, "Firestorm", 8, 6, 80, AttackTypes.MAG,
                Pair(BasicStats.BaseMana, 15),
                Pair(60, StatusEffect.getStatusEffect(7)!!)
            ),
            19 to Attack(19, "Light Lance", 5, 13, 80, AttackTypes.MAG,
                Pair(BasicStats.BaseMana, 25), null
            ),
            20 to Attack(20, "Explosion Burst", 12, 7, 75, AttackTypes.MAG,
                Pair(BasicStats.BaseMana, 23), null
            ),
        )


        fun getAttack(attackID: Int): Attack? {
            return if (attackIDToAttack.contains(attackID)) attackIDToAttack[attackID] else null
        }
    }
}