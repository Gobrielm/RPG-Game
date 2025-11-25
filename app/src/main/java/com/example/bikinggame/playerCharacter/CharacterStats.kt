package com.example.bikinggame.playerCharacter

import android.util.Log

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
     *  @return Whether or not this character has gone below 0 health
     */
    fun getAttacked(attack: Attack): Boolean {
        var health: Int = getHealth()
        health -= attack.getMomentum()
        return if (health < 0) {
            setHealth(0)
            true
        } else {
            setHealth(health)
            false
        }
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

    fun raiseStat(stat: BasicStats, amount: Int) {
        characterStats[stat] = characterStats[stat]!! + amount
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
            CharacterSubClass.TraditionalMagic to 4,
            CharacterSubClass.RitualMagic to 3,
            CharacterSubClass.Knight to 15,
            CharacterSubClass.North to 12,
            CharacterSubClass.TraditionalRanged to 4,
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