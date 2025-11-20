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
    val playerClass: CharacterClass
    val baseStats: CharacterStats
    val currentStats: CharacterStats

    val attackIDs: IntArray = IntArray(4) { -1 }
    val shieldIDs: ArrayList<Int> = ArrayList()
    val attacks: Array<Attack?> = arrayOfNulls(4)
    val shields: ArrayList<Shield?> = arrayListOf()

    val currentEquipmentIDs: IntArray = IntArray(EquipmentSlot.entries.size) { -1 }
    val currentEquipment = arrayOfNulls<Equipment>(EquipmentSlot.entries.size)


    constructor(pPlayerClass: CharacterClass, pId: Int) {
        id = pId
        playerClass = pPlayerClass
        baseStats = CharacterStats(pPlayerClass.subClass)
        currentStats = baseStats
    }

    // Used for local creation
    constructor(pPlayerClass: CharacterClass) {
        id = -1
        playerClass = pPlayerClass
        baseStats = CharacterStats(pPlayerClass.subClass)
        currentStats = baseStats
    }

    private constructor(jsonArray: JSONArray) {
        var offset = IntWrapper(0)
        id = jsonArray.get(offset.value++) as Int
        playerClass = CharacterClass(jsonArray, offset) // uses 1 & 2
        baseStats = CharacterStats(playerClass.subClass)
        currentStats = baseStats

        for (i in 0 until EquipmentSlot.entries.size) {
            currentEquipmentIDs[i] = if (jsonArray.isNull(offset.value)) {
                offset.value++
                -1
            } else {
                val id = jsonArray[offset.value++] as Int
                id
            }
        }

        for (i in 0 until 4) {
            attackIDs[i] = if (jsonArray.isNull(offset.value)) {
                offset.value++
                -1
            } else {
                val id = jsonArray[offset.value++] as Int
                id
            }
        }
        val shieldsSize = jsonArray.get(offset.value++) as Int
        for (i in 0 until shieldsSize) {
            val id = jsonArray[offset.value++] as Int
            shieldIDs.add(id)
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
            if (attackIDs[i] == -1) {
                jsonArray.put(null)
            } else {
                jsonArray.put(attackIDs[i])
            }
        }
        jsonArray.put(shieldIDs.size)
        for (i in 0 until shieldIDs.size) {
            jsonArray.put(shieldIDs[i])
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

    fun fetchAttacksAndShields() { // TODO: Currently only attacks/shields are from equipment, no skills
        for (i in 0 until currentEquipmentIDs.size) {
            val equipment = currentEquipment[i]
            if (equipment != null) {
                if (equipment.attack != null) addAttack(equipment.attack)
                if (equipment.shield != null) addShield(equipment.shield)
            }
        }
    }

    fun addAttack(attack: Attack) {
        for (i in 0 until 4) {
            if (attackIDs[i] == attack.id) {
                attacks[i] = attack
            }
        }
    }

    fun addShield(shield: Shield) {
        for (i in 0 until shieldIDs.size) {
            if (shieldIDs[i] == shield.id) {
                shields.add(shield)
            }
        }
    }

    suspend fun fetchEquipment() {
        for (i in 0 until currentEquipmentIDs.size) {
            currentEquipment[i] = getEquipment(currentEquipmentIDs[i])
        }
        fetchAttacksAndShields()
    }

    companion object {
        suspend fun createCharacter(jsonArray: JSONArray): PlayerCharacter {
            val playerCharacter: PlayerCharacter = PlayerCharacter(jsonArray)
            playerCharacter.fetchEquipment()
            return playerCharacter
        }
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