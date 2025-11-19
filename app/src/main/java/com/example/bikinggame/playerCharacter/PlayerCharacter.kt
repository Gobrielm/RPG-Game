package com.example.bikinggame.playerCharacter

import android.util.Log
import org.json.JSONArray
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

class IntWrapper(var value: Int)

private const val TAG = "CharacterCreation"

class PlayerCharacter {
    val id: Int
    var playerClass: CharacterClass
    var baseStats: CharacterStats = CharacterStats()
    var currentStats: CharacterStats = CharacterStats()

    var attacks: Array<Attack?> = arrayOfNulls<Attack>(4)
//    var shields:
    var currentEquipment = arrayOfNulls<Equipment>(EquipmentSlot.entries.size)


    constructor(pPlayerClass: CharacterClass, pId: Int) {
        id = pId
        playerClass = pPlayerClass
        baseStats = CharacterStats(pPlayerClass.subClass)
        currentStats = baseStats
        attacks[0] = Attack(10, currentStats.characterStats[BasicStats.Strength] as Int, 0, 90)
    }

    // Used for local creation
    constructor(pPlayerClass: CharacterClass) {
        id = -1
        playerClass = pPlayerClass
        baseStats = CharacterStats(pPlayerClass.subClass)
        currentStats = baseStats
        attacks[0] = Attack(10, currentStats.characterStats[BasicStats.Strength] as Int, 0, 90)
    }

    constructor(jsonArray: JSONArray) {
        var offset = IntWrapper(0)
        id = jsonArray.get(offset.value++) as Int
        playerClass = CharacterClass(jsonArray, offset) // uses 1 & 2
        baseStats = CharacterStats(playerClass.subClass)
        currentStats = baseStats

        for (i in 0 until EquipmentSlot.entries.size) {
            currentEquipment[i] = if (jsonArray.isNull(offset.value)) {
                offset.value++
                null
            } else {
                // TODO: GET EQUIPMENT WITH THE ID
                val id = jsonArray[offset.value++]
                null
            }
        }

        for (i in 0 until 4) {
            if (jsonArray.isNull(offset.value)) {
                offset.value++
                attacks[i] = null
            } else {
                attacks[i] = Attack(jsonArray, offset)
            }
        }
    }


    fun serialize(): JSONArray {
        val jsonArray = JSONArray()
        jsonArray.put(id)
        playerClass.serialize(jsonArray)
        for (i in 0 until EquipmentSlot.entries.size) {
            jsonArray.put(if (currentEquipment[i] == null) null else currentEquipment[i]!!.id)
        }
        for (i in 0 until 4) {
            if (attacks[i] == null) {
                jsonArray.put(null)
            } else {
                attacks[i]!!.serialize(jsonArray)
            }
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

    /**
     *  @return Whether or not this character has gone below 0 health
     */
    fun takeAttack(attack: Attack): Boolean {
        return currentStats.getAttacked(attack)
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

    constructor(jsonArray: JSONArray, offset: IntWrapper) {
        try {
            mainClass = CharacterMainClass.fromInt(jsonArray[offset.value++] as Int)!!
            subClass = CharacterSubClass.fromInt(jsonArray[offset.value++] as Int)!!
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

    companion object {
        val SERIALIZED_OFFSET = 2
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