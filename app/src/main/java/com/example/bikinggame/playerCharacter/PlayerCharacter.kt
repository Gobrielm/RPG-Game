package com.example.bikinggame.playerCharacter

import android.util.Log
import org.json.JSONArray
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

private const val TAG = "CharacterCreation"

class PlayerCharacter {
    val id: Int
    var playerClass: CharacterClass
    var baseStats: CharacterStats = CharacterStats()
    var currentStats: CharacterStats = CharacterStats()
    var currentEquipment = arrayOfNulls<Equipment>(EquipmentSlot.entries.size)

    // Used for creating the first character
    constructor(pPlayerClass: CharacterClass, pId: Int) {
        id = pId
        playerClass = pPlayerClass
        baseStats = CharacterStats(pPlayerClass.subClass)
        currentStats = baseStats
    }

    constructor(pPlayerClass: CharacterClass) {
        id = abs(Random.nextInt())
        playerClass = pPlayerClass
        baseStats = CharacterStats(pPlayerClass.subClass)
        currentStats = baseStats
    }

    constructor(jsonArray: JSONArray) {
        id = jsonArray.get(0) as Int
        playerClass = CharacterClass(jsonArray, 1)
        baseStats = CharacterStats(playerClass.subClass)
        currentStats = baseStats
    }


    fun serialize(): JSONArray {
        val jsonArray = JSONArray()
        jsonArray.put(id)
        playerClass.serialize(jsonArray)
        for (i in 0 until EquipmentSlot.entries.size) {
            jsonArray.put(if (currentEquipment[i] == null) null else currentEquipment[i]!!.id)
        }
        return jsonArray
    }

    override fun toString(): String {
        return "$id: $playerClass \n $currentStats"
    }

    fun addEquipment(slot: EquipmentSlot, equipment: Equipment) {
        currentEquipment[slot.ordinal] = equipment
        for (statBoost: Pair<BasicStats, Int> in equipment.statBoost) {
            val prev: Int = currentStats.characterStats[statBoost.first]!!
            currentStats.characterStats[statBoost.first] = prev + statBoost.second
        }
    }

    fun removeEquipment(slot: EquipmentSlot) {
        val toRemove: Equipment? = currentEquipment[slot.ordinal]
        if (toRemove != null) {
            for (statBoost: Pair<BasicStats, Int> in toRemove.statBoost) {
                val prev: Int = currentStats.characterStats[statBoost.first]!!
                currentStats.characterStats[statBoost.first] = prev - statBoost.second
            }
        }
        currentEquipment[slot.ordinal] = null

    }
}

enum class CharacterMainClass(val ind: Int) {
    NONE(0),
    MELEE(1),
    RANGED(2),
    MAGIC(3);

    companion object {
        fun fromInt(value: Int): CharacterMainClass? {
            return CharacterMainClass.entries.find { it.ind == value }
        }
    }
}

enum class CharacterSubClass(val ind: Int) {
    Filler(0),
    TraditionalMagic(1),
    RitualMagic(2),
    Knight(3),
    North(4),
    TraditionalRanged(5),
    NonTraditionalRanged(6);

    companion object {
        fun fromInt(value: Int): CharacterSubClass? {
            return CharacterSubClass.entries.find { it.ind == value }
        }
    }
}

class CharacterClass {
    var mainClass: CharacterMainClass = CharacterMainClass.NONE
    var subClass: CharacterSubClass = CharacterSubClass.Filler

    constructor() {}

    constructor(p_mainClass: CharacterMainClass, p_subClass: CharacterSubClass) {
        mainClass = p_mainClass
        subClass = p_subClass
    }

    constructor(jsonArray: JSONArray, offset: Int) {
        try {
            mainClass = CharacterMainClass.fromInt(jsonArray[offset] as Int)!!
            subClass = CharacterSubClass.fromInt(jsonArray[offset + 1] as Int)!!
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }

    fun serialize(jsonArray: JSONArray) {
        jsonArray.put(mainClass.ind)
        jsonArray.put(subClass.ind)
    }

    override fun toString(): String {
        return "$mainClass-$subClass"
    }
}

class CharacterSkillTree {
    var exp: Int = 0




    companion object {
        fun getCurrentLevel(exp: Int): Int {
            val exp = min(exp, getExpRequiredForLevel(30))

            return floor((exp / 28).toDouble().pow(0.625)).toInt()
        }

        fun getExpRequiredForLevel(level: Int): Int {
            val level = min(level, 30)
            return (level.toDouble().pow(1.6) * 28).toInt()
        }
    }
}

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

    fun getHealth(): Int {
        return if (characterStats.contains(BasicStats.BaseHealth)) characterStats[BasicStats.BaseHealth]!! else 0
    }

    fun getMana(): Int {
        return if (characterStats.contains(BasicStats.BaseMana)) characterStats[BasicStats.BaseMana]!! else 0
    }

    fun getStamina(): Int {
        return if (characterStats.contains(BasicStats.BaseStamina)) characterStats[BasicStats.BaseStamina]!! else 0
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