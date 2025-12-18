package com.example.bikinggame.playerCharacter

import android.util.Log
import org.json.JSONArray
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
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
    var shield: Shield? = null

    val currentEquipment = arrayOfNulls<Equipment>(EquipmentSlot.entries.size)


    constructor(pPlayerClass: CharacterClass, pId: Int) {
        id = pId
        playerClass = pPlayerClass
        baseStats = CharacterStats(pPlayerClass.subClass)
        currentStats = CharacterStats(baseStats)
        skillTree = CharacterSkillTree()
        val skill = Skill.getSkill(pPlayerClass.subClass.ordinal)
        skillTree.skillsUnlocked.add(skill!!)
    }

    // Used for local creation
    constructor(pPlayerClass: CharacterClass) {
        id = -1
        playerClass = pPlayerClass
        baseStats = CharacterStats(pPlayerClass.subClass)
        currentStats = CharacterStats(baseStats)
        skillTree = CharacterSkillTree()
    }

    constructor(jsonArray: JSONArray) {
        var offset = IntWrapper(0)
        id = jsonArray.get(offset.value++) as Int
        playerClass = CharacterClass(jsonArray, offset)
        baseStats = CharacterStats(playerClass.subClass)
        currentStats = CharacterStats(baseStats)

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
        val shieldID = jsonArray.get(offset.value++) as Int
        shield = Shield.getShield(shieldID)
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
        if (shield != null) {
            jsonArray.put(shield!!.id)
        } else {
            jsonArray.put(-1)
        }
        return jsonArray
    }

    override fun toString(): String {
        return "${playerClass.subClass} \nLevel: ${CharacterSkillTree.getCurrentLevel(skillTree.exp)} \nExp: ${skillTree.exp} \n$currentStats"
    }

    fun addEquipment(slot: EquipmentSlot, equipment: Equipment) {
        currentEquipment[slot.ordinal] = equipment
        for (statBoost: Pair<BasicStats, Int> in equipment.statBoost) {
            currentStats.raiseStat(statBoost.first, statBoost.second)
        }
    }

    fun removeEquipment(slot: EquipmentSlot) {
        val toRemove: Equipment? = currentEquipment[slot.ordinal]
        if (toRemove != null) {
            for (statBoost: Pair<BasicStats, Int> in toRemove.statBoost) {
                currentStats.lowerStat(statBoost.first, statBoost.second)
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

    fun addExp(amount: Int) {
        skillTree.exp += amount
    }

    /**
     * Used to update shields, status affects, regenerating
     */
    fun updateNewTurn() {
        if (shield != null) {
            shield!!.regenShield()
        }
        currentStats.regenStamina(baseStats.getStamina())
        currentStats.regenMana(baseStats.getMana())
        currentStats.updateNewTurn()
    }

    fun addStatusEffect(statusEffect: StatusEffect) {
        currentStats.addStatusEffect(statusEffect)
    }

    fun getStatusEffects(): ArrayList<StatusEffect> {
        return currentStats.getStatusEffects()
    }

    fun canChooseAttack(attack: Attack): Boolean {
        if (attack.statCost == null) return true
        val (stat, amt) = attack.statCost
        return currentStats.getPrimaryStat(stat) >= amt
    }

    fun takeCostFromAttackUsed(attack: Attack) {
        if (attack.statCost == null) return
        val (stat, amt) = attack.statCost
        currentStats.lowerStat(stat, amt)
    }

    fun getShieldHitPoints(): Int {
        return shield?.getHitPoints() ?: 0
    }

    /**
     *  @return (Msg of Event)
     */
    fun takeAttack(attack: Attack, damage: Int, hitType: Attack.HitTypes): String {
        var damage: Int = damage
        var msg = ""
        var canDodge = true // Can either dodge or use shield
        if (getShieldHitPoints() > 0) {
            canDodge = false
            val (newDamage, newMsg) = shield!!.blockHit(attack, damage, hitType)
            damage = newDamage
            msg = newMsg

        }
        val otherMsg = currentStats.getAttacked(damage, attack, hitType, canDodge)

        return msg.ifEmpty { otherMsg }
    }

    fun calculateDamageForAttack(attack: Attack): Pair<Int, Attack.HitTypes> {
        return currentStats.calculateDamageForAttack(attack)
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

    fun getAvailableShields(): ArrayList<Shield> {
        val shieldsToReturn = ArrayList<Shield>()
        val idToAvoid = shield?.id ?: -1

        currentEquipment.forEach { equipment ->
            if (equipment != null && equipment.shield != null && equipment.shield.id != idToAvoid) {
                shieldsToReturn.add(equipment.shield)
            }
        }

        skillTree.skillsUnlocked.forEach { skill ->
            if (skill.shield != null && skill.shield!!.id != idToAvoid) {
                shieldsToReturn.add(skill.shield!!)
            }
        }

        return shieldsToReturn
    }

    fun healCharacter(percentage: Double) {
        val amt1 = round(baseStats.getHealth() * percentage).toInt()
        val newHealth = min(currentStats.getHealth() + amt1, baseStats.getHealth())
        currentStats.setHealth(newHealth)

        val amt2 = round(baseStats.getStamina() * percentage).toInt()
        val newStamina = min(currentStats.getStamina() + amt2, baseStats.getStamina())
        currentStats.setStamina(newStamina)

        val amt3 = round(baseStats.getMana() * percentage).toInt()
        val newMana = min(currentStats.getMana() + amt3, baseStats.getMana())
        currentStats.setMana(newMana)
    }

    fun isAlive(): Boolean {
        return currentStats.getHealth() > 0
    }
}