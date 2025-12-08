package com.example.bikinggame.playerCharacter

import android.util.Log
import java.util.Random
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

private const val TAG: String = "CharacterStats"

enum class BasicStats {
    BaseHealth,
    BaseMana,
    BaseStamina,

    Strength,
    Casting,

    Constitution,
    Intelligence,
    Dexterity;

    companion object {
        fun fromInt(value: Int): BasicStats? {
            return BasicStats.entries.find { it.ordinal == value }
        }
    }
}

class CharacterStats {


    /**
     * @return with (Hit Type, Damage)
     *
     */
    fun calculateDamageForAttack(attack: Attack): Pair<Int, Attack.HitTypes> {
        val baseDamage = attack.getMomentum()
        val hitType = attack.getRandomHitType()
        val statMult = getStatDamageMultipler(attack.type)

        return Pair(round(baseDamage * hitType.getMultiplier() * statMult).toInt(), hitType)
    }

    fun getStatDamageMultipler(attackType: Attack.AttackTypes): Float {
        return 1f + when (attackType) {
            Attack.AttackTypes.PHY -> {
                getStrength() / 50.0f
            }
            Attack.AttackTypes.MAG -> {
                getCasting() / 50.0f
            }
            else -> {
                getStrength() / 120.0f
            }
        }
    }

    fun getDamageBlockedForAttack(attackType: Attack.AttackTypes): Int {
        if (attackType == Attack.AttackTypes.PHY) {
            return round(getConstitution() / 2.0f).toInt()
        } else if (attackType == Attack.AttackTypes.RAN) {
            return round(getConstitution() / 4.0f).toInt()
        }
        return 0
    }

    /**
     *  @return Whether true if character has gone below 0 health
     */
    fun getAttacked(damage: Int, attack: Attack, hitType: Attack.HitTypes, canDodge: Boolean): Pair<Boolean, String> {
        val blocked = getDamageBlockedForAttack(attack.type)
        var damage = max(0, damage - blocked)

        if (damage < 0) return Pair(false, "")

        var msg = ""
        if (canDodge) {
            val (status, newMsg) = attemptDodge(attack, hitType)
            if (status) {
                damage = 0
            }
            msg = newMsg
        }

        var health: Int = getHealth()
        health -= damage

        return Pair(if (health <= 0) {
            setHealth(0)
            true
        } else {
            setHealth(health)
            false
        }, msg)
    }

    /**
     * @return (If dodge was successful, desc msg)
     */
    fun attemptDodge(attack: Attack, hitType: Attack.HitTypes): Pair<Boolean, String> {
        val rand: Int = kotlin.random.Random.nextInt(0, 100)
        val velocity = attack.velocity * hitType.getMultiplier()

        // Can attempt dodge
        if (getDexterity() > velocity) {
            val cost = round(velocity * 2.0f).toInt()
            if (getStamina() < cost) return Pair(false, "")

            setStamina(getStamina() - cost)

            val chanceToFail = 100 - round(velocity / 2.0f / getDexterity() * 100).toInt()

            val msg = if (rand < chanceToFail) {
                "Failed Dodge"
            } else {
                "Dodged"
            }

            return Pair(rand >= chanceToFail, msg)
        }
        return Pair(false, "")
    }

    fun getHealth(): Int {
        return characterStats[BasicStats.BaseHealth] ?: 0
    }

    fun setHealth(value: Int) {
        characterStats[BasicStats.BaseHealth] = value
    }

    fun getMana(): Int {
        return characterStats[BasicStats.BaseMana] ?: 0
    }

    fun setMana(value: Int) {
        characterStats[BasicStats.BaseMana] = value
    }

    fun getStamina(): Int {
        return characterStats[BasicStats.BaseStamina] ?: 0
    }

    fun setStamina(value: Int) {
        characterStats[BasicStats.BaseStamina] = value
    }

    fun getStrength(): Int {
        return characterStats[BasicStats.Strength]!!
    }

    fun getCasting(): Int {
        return characterStats[BasicStats.Casting]!!
    }

    fun getConstitution(): Int {
        return characterStats[BasicStats.Constitution]!!
    }

    fun getIntelligence(): Int {
        return characterStats[BasicStats.Intelligence]!!
    }

    fun getDexterity(): Int {
        return characterStats[BasicStats.Dexterity]!!
    }

    fun raiseStat(stat: BasicStats, amount: Int) {
        characterStats[stat] = characterStats[stat]!! + amount
    }

    fun lowerStat(stat: BasicStats, amount: Int) {
        characterStats[stat] = characterStats[stat]!! - amount
        characterStats[stat] = max(characterStats[stat]!!, 0)
    }

    fun regenStamina(maxStamina: Int) {
        val newStamina = min(maxStamina, round(getStamina() + getDexterity() / 3.0f).toInt())
        setStamina(newStamina)
    }

    fun regenMana(maxMana: Int) {
        val newMana = min(maxMana, round(getMana() + getIntelligence() / 2.0f).toInt())
        setMana(newMana)
    }

    var characterStats: MutableMap<BasicStats, Int> = mutableMapOf(
        BasicStats.BaseHealth to 0,
        BasicStats.BaseMana to 0,
        BasicStats.BaseStamina to 0,
        BasicStats.Strength to 0,
        BasicStats.Casting to 0,
        BasicStats.Constitution to 0,
        BasicStats.Intelligence to 0,
        BasicStats.Dexterity to 0
    )

    constructor()

    constructor(pCharacterStats: MutableMap<BasicStats, Int>) {
        characterStats = pCharacterStats
    }

    constructor(pOtherCharacterStats: CharacterStats): this((pOtherCharacterStats.characterStats).toMutableMap())

    constructor(subClass: CharacterSubClass) {
        try {
            characterStats[BasicStats.BaseHealth] = baseHealthMap[subClass]!!
            characterStats[BasicStats.BaseMana] = baseManaMap[subClass]!!
            characterStats[BasicStats.BaseStamina] = baseStaminaMap[subClass]!!

            characterStats[BasicStats.Strength] = baseStrengthMap[subClass]!!
            characterStats[BasicStats.Casting] = baseCastingMap[subClass]!!

            characterStats[BasicStats.Constitution] = baseConstitutionMap[subClass]!!
            characterStats[BasicStats.Intelligence] = baseIntelligenceMap[subClass]!!
            characterStats[BasicStats.Dexterity] = baseDexterityMap[subClass]!!


        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }

    override fun toString(): String {
        return """
            Health: ${characterStats[BasicStats.BaseHealth]}
            Mana: ${characterStats[BasicStats.BaseMana]}
            Stamina: ${characterStats[BasicStats.BaseStamina]}
            Strength: ${characterStats[BasicStats.Strength]}
            Casting: ${characterStats[BasicStats.Casting]}
            Constitution: ${characterStats[BasicStats.Constitution]}
            Intelligence: ${characterStats[BasicStats.Intelligence]}
            Dexterity: ${characterStats[BasicStats.Dexterity]}
        """.trimIndent()
    }

    companion object {
        val baseHealthMap: Map<CharacterSubClass, Int> = mapOf(
            CharacterSubClass.TraditionalMagic to 20,
            CharacterSubClass.RitualMagic to 25,
            CharacterSubClass.Knight to 40,
            CharacterSubClass.North to 35,
            CharacterSubClass.TraditionalRanged to 20,
            CharacterSubClass.NonTraditionalRanged to 25
        )

        val baseManaMap: Map<CharacterSubClass, Int> = mapOf(
            CharacterSubClass.TraditionalMagic to 15,
            CharacterSubClass.RitualMagic to 10,
            CharacterSubClass.Knight to 5,
            CharacterSubClass.North to 5,
            CharacterSubClass.TraditionalRanged to 5,
            CharacterSubClass.NonTraditionalRanged to 10
        )

        val baseStaminaMap: Map<CharacterSubClass, Int> = mapOf(
            CharacterSubClass.TraditionalMagic to 10,
            CharacterSubClass.RitualMagic to 13,
            CharacterSubClass.Knight to 5,
            CharacterSubClass.North to 5,
            CharacterSubClass.TraditionalRanged to 17,
            CharacterSubClass.NonTraditionalRanged to 20
        )

        val baseStrengthMap: Map<CharacterSubClass, Int> = mapOf(
            CharacterSubClass.TraditionalMagic to 3,
            CharacterSubClass.RitualMagic to 4,
            CharacterSubClass.Knight to 12,
            CharacterSubClass.North to 15,
            CharacterSubClass.TraditionalRanged to 8,
            CharacterSubClass.NonTraditionalRanged to 6
        )

        val baseCastingMap: Map<CharacterSubClass, Int> = mapOf(
            CharacterSubClass.TraditionalMagic to 10,
            CharacterSubClass.RitualMagic to 8,
            CharacterSubClass.Knight to 1,
            CharacterSubClass.North to 1,
            CharacterSubClass.TraditionalRanged to 2,
            CharacterSubClass.NonTraditionalRanged to 4
        )

        val baseConstitutionMap: Map<CharacterSubClass, Int> = mapOf(
            CharacterSubClass.TraditionalMagic to 2,
            CharacterSubClass.RitualMagic to 1,
            CharacterSubClass.Knight to 7,
            CharacterSubClass.North to 4,
            CharacterSubClass.TraditionalRanged to 2,
            CharacterSubClass.NonTraditionalRanged to 2
        )

        val baseIntelligenceMap: Map<CharacterSubClass, Int> = mapOf(
            CharacterSubClass.TraditionalMagic to 10,
            CharacterSubClass.RitualMagic to 7,
            CharacterSubClass.Knight to 3,
            CharacterSubClass.North to 2,
            CharacterSubClass.TraditionalRanged to 4,
            CharacterSubClass.NonTraditionalRanged to 2
        )

        val baseDexterityMap: Map<CharacterSubClass, Int> = mapOf(
            CharacterSubClass.TraditionalMagic to 4,
            CharacterSubClass.RitualMagic to 6,
            CharacterSubClass.Knight to 2,
            CharacterSubClass.North to 4,
            CharacterSubClass.TraditionalRanged to 10,
            CharacterSubClass.NonTraditionalRanged to 12
        )

    }
}