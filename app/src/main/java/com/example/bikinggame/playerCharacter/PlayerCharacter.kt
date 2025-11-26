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
    val skillTree: CharacterSkillTree
    val attacks: Array<Attack?> = arrayOfNulls(4)
    val shields: ArrayList<Shield> = arrayListOf()

    val currentEquipment = arrayOfNulls<Equipment>(EquipmentSlot.entries.size)


    constructor(pPlayerClass: CharacterClass, pId: Int) {
        id = pId
        playerClass = pPlayerClass
        baseStats = CharacterStats(pPlayerClass.subClass)
        currentStats = baseStats
        skillTree = CharacterSkillTree()
        val skill = Skill.getSkill(pPlayerClass.subClass.ordinal)
        skillTree.skillsUnlocked.add(skill!!)
    }

    // Used for local creation
    constructor(pPlayerClass: CharacterClass) {
        id = -1
        playerClass = pPlayerClass
        baseStats = CharacterStats(pPlayerClass.subClass)
        currentStats = baseStats
        skillTree = CharacterSkillTree()
    }

    constructor(jsonArray: JSONArray) {
        var offset = IntWrapper(0)
        id = jsonArray.get(offset.value++) as Int
        playerClass = CharacterClass(jsonArray, offset)
        baseStats = CharacterStats(playerClass.subClass)
        currentStats = baseStats

        skillTree = CharacterSkillTree()
        skillTree.exp = jsonArray[offset.value++] as Int
        val skillsUnlocked = jsonArray[offset.value++] as Int
        for (i in 0 until skillsUnlocked) {
            addSkill(jsonArray[offset.value++] as Int)
        }

        for (i in 0 until EquipmentSlot.entries.size) {
            currentEquipment[i] = if (jsonArray.isNull(offset.value)) {
                offset.value++
                null
            } else {
                val id = jsonArray[offset.value++] as Int
                Equipment.getEquipment(id)
            }
        }

        for (i in 0 until 4) {
            if (jsonArray.isNull(offset.value)) {
                offset.value++
                attacks[i] = null
            } else {
                val id = jsonArray.get(offset.value++) as Int
                val attack = Attack.getAttack(id)
                attacks[i] = attack
            }

        }
        val shieldsSize = jsonArray.get(offset.value++) as Int
        for (i in 0 until shieldsSize) {
            val id = jsonArray[offset.value++] as Int
            val shield = Shield.getShield(id)
            if (shield != null) shields.add(shield)
        }
    }


    fun serialize(): JSONArray {
        val jsonArray = JSONArray()
        jsonArray.put(id)
        playerClass.serialize(jsonArray)
        skillTree.serialize(jsonArray)

        for (i in 0 until EquipmentSlot.entries.size) {
            jsonArray.put(if (currentEquipment[i] == null) -1 else currentEquipment[i]!!.id)
        }

        for (i in 0 until 4) {
            if (attacks[i] == null) {
                jsonArray.put(-1)
            } else {
                jsonArray.put(attacks[i]!!.id)
            }
        }
        jsonArray.put(shields.size)
        for (i in 0 until shields.size) {
            jsonArray.put(shields[i].id)
        }
        return jsonArray
    }

    override fun toString(): String {
        return "${playerClass.subClass} \nLevel: ${CharacterSkillTree.getCurrentLevel(skillTree.exp)} \nExp: ${skillTree.exp} \n$currentStats"
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

    fun getEquipment(slot: EquipmentSlot): Equipment? {
        return currentEquipment[slot.ordinal]
    }

    fun addSkill(skillID: Int) {
        val skill = Skill.getSkill(skillID)
        if (skill == null || skillTree.skillsUnlocked.contains(skill)) return
        skillTree.skillsUnlocked.add(skill)

        for ((stat, amount) in skill.statIncrease) {
            currentStats.raiseStat(stat, amount)
        }
    }

    fun hasSkill(skillID: Int): Boolean {
        for (skill in skillTree.skillsUnlocked) {
            if (skill.id == skillID) {
                return true
            }
        }
        return false
    }

    /**
     *  @return Whether or not this character has gone below 0 health
     */
    fun takeAttack(attack: Attack): Boolean {
        return currentStats.getAttacked(attack)
    }

    fun getAttack(slot: Int): Attack? {
        return attacks[slot]
    }

    fun getAvailableAttacks(attackSlotToExclude: Int): ArrayList<Pair<Attack, Boolean>> {
        val attacksToReturn = ArrayList<Pair<Attack, Boolean>>()
        val idsThatReassign = arrayListOf<Int>()
        for (i in 0 until 3) {
            if (attackSlotToExclude != i && attacks[i] != null) {
                idsThatReassign.add(attacks[i]!!.id)
            }
        }
        val idToAvoid: Int = attacks[attackSlotToExclude]?.id ?: -1

        currentEquipment.forEach { equipment ->
            if (equipment != null && equipment.attack != null && equipment.attack.id != idToAvoid) {
                attacksToReturn.add(Pair(equipment.attack, idsThatReassign.contains(equipment.attack.id)))
            }
        }

        skillTree.skillsUnlocked.forEach { skill ->
            if (skill.attack != null && skill.attack!!.id != idToAvoid) {
                attacksToReturn.add(Pair(skill.attack!!, idsThatReassign.contains(skill.attack!!.id)))
            }
        }

        return attacksToReturn
    }
}