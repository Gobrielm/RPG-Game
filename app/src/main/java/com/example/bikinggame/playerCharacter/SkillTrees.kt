package com.example.bikinggame.playerCharacter

import android.util.Log
import androidx.compose.ui.geometry.Offset
import com.example.bikinggame.attack.Attack
import org.json.JSONArray
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class CharacterSkillTree {
    var exp: Int = 0
    val skillsUnlocked: ArrayList<Skill> = arrayListOf()

    constructor() {}

    constructor(jsonArray: JSONArray, offset: IntWrapper) {
        exp = jsonArray.get(offset.value++) as Int
        val size = jsonArray.get(offset.value++) as Int
        for (i in 0 until size) {
            val skillID: Int = jsonArray.get(offset.value++) as Int
            val skill = Skill.getSkill(skillID)
            if (skill != null) skillsUnlocked.add(skill)
        }
    }

    fun serialize(jsonArray: JSONArray) {
        jsonArray.put(exp)
        jsonArray.put(skillsUnlocked.size)
        skillsUnlocked.forEach { skill ->
            jsonArray.put(skill.id)
        }
    }

    fun getAvailableSkillPoints(): Int {
        val level: Int = getCurrentLevel(exp)
        return max(0, (level + 1) - skillsUnlocked.size) // Gets one skill for free
    }

    companion object {
        const val EXP_COEFFICIENT = 60
        const val EXP_FACTOR = 0.825
        fun getCurrentLevel(exp: Int): Int {
            return min(30, floor((exp / EXP_COEFFICIENT).toDouble().pow(EXP_FACTOR)).toInt())
        }

        fun getExpRequiredForLevel(level: Int): Int {
            val level = min(level, 30)
            return (level.toDouble().pow(1 / EXP_FACTOR) * EXP_COEFFICIENT).toInt()
        }
    }
}

object SkillTrees {
    var skillTrees: Map<CharacterSubClass, Array<Pair<Int, Offset>>> = mapOf(
        CharacterSubClass.Knight to arrayOf(
            3 to Offset(0f, 0f),

            7 to Offset(0f, -1f),
            8 to Offset(0.71f, 0.71f),
            9 to Offset(-0.71f, 0.71f),

            10 to Offset(1.576f, 1.21f),
            11 to Offset(1.21f, 1.576f),
        ),

        CharacterSubClass.NonTraditionalRanged to arrayOf(
            6 to Offset(0f, 0f),

            12 to Offset(0.71f, 0.71f),

            13 to Offset(1.21f, 1.576f),
        ),

        CharacterSubClass.TraditionalMagic to arrayOf(
            1 to Offset(0f, 0f),

            14 to Offset(-1.06f,0.6f),
                17 to Offset(-1.82f,0.07f),
                18 to Offset(-0.8f, 1.3f),

            15 to Offset(0.94f,0.04f),
                16 to Offset(1.72f,0.6f),


        )
    )

    fun getSkillTree(subClass: CharacterSubClass): Array<Pair<Int, Offset>> {
        return skillTrees[subClass] ?: Array(0) { Pair(0, Offset(0f, 0f))}
    }
}


class Skill {

    val id: Int
    val name: String
    val statIncrease: MutableMap<BasicStats, Int> = mutableMapOf()
    val prerequisites: ArrayList<Int> = arrayListOf()
    private val attackID: Int
    private val shieldID: Int
    var attack: Attack? = null
    var shield: Shield? = null

    private constructor(pID: Int, pName: String, pStatIncrease: Map<BasicStats, Int>, pPrerequisites: Array<Int>, pAttackID: Int, pShieldID: Int) {
        id = pID
        name = pName
        pStatIncrease.forEach { stat, amount ->
            statIncrease[stat] = amount
        }
        pPrerequisites.forEach { id ->
            prerequisites.add(id)
        }
        attackID = pAttackID
        shieldID = pShieldID
    }

    override fun toString(): String {
        var toReturn = "$name: \n"

        toReturn += "Prerequisites:"
        for (id in prerequisites) {
            toReturn += " "
            toReturn += getSkill(id)!!.name + ","
        }

        if (prerequisites.isEmpty()) toReturn += " None"

        toReturn = toReturn.trimEnd { it == ',' }
        toReturn += '\n'

        for ((stat, increase) in statIncrease) {
            toReturn += "$stat: $increase"
        }
        toReturn += '\n'

        if (attack != null) toReturn += "Attack: $attack\n"
        if (shield != null) toReturn += "Shield: $shield\n"

        return toReturn
    }

    companion object {
        val skillIDtoSkill = hashMapOf<Int, Skill>(
            // Start of TraditionalMagic Tree
            1 to Skill(1, "Humble Beginnings", emptyMap(), emptyArray(), 2, -1),
            14 to Skill(14, "Magic Study", mapOf(BasicStats.Intelligence to 1), arrayOf(1), -1, -1),
            15 to Skill(15, "Magic Practice", mapOf(BasicStats.Casting to 1), arrayOf(1), -1, -1),
            16 to Skill(16, "Frost Study", emptyMap(), arrayOf(15), -1, 2),
            17 to Skill(17, "Improved Casting", mapOf(BasicStats.Casting to 1), arrayOf(14), -1, -1),
            18 to Skill(18, "Path of Healing", emptyMap(), arrayOf(14), 6, -1),

            // Start of RitualMagic Tree
            2 to Skill(2, "Humble Beginnings", emptyMap(), emptyArray(), 2, -1),

            // Start of Knight Tree
            3 to Skill(3, "Humble Beginnings", emptyMap(), emptyArray(), 1, -1),
            7 to Skill(7, "Path of Strength", mapOf(BasicStats.Strength to 1), arrayOf(3), -1, -1),
            8 to Skill(8, "Path of Fortitude", mapOf(BasicStats.Constitution to 1), arrayOf(3), -1, -1),
            9 to Skill(9, "Path of Heartiness", mapOf(BasicStats.BaseHealth to 2), arrayOf(3), -1, -1),
            10 to Skill(10, "Improvised Block", emptyMap(), arrayOf(8), -1, 3),
            11 to Skill(11, "Improved Heartiness", mapOf(BasicStats.BaseHealth to 2), arrayOf(8), -1, -1),

            // Start of North Tree
            4 to Skill(4, "Humble Beginnings", emptyMap(), emptyArray(), 1, -1),

            // Start of TraditionalRanged Tree
            5 to Skill(5, "Humble Beginnings", emptyMap(), emptyArray(), 3, -1),

            // Start of NonTraditionalRanged Tree
            6 to Skill(6, "Humble Beginnings", emptyMap(), emptyArray(), 3, -1),
            12 to Skill(12, "Tree Dwelling", mapOf(BasicStats.Dexterity to 1), arrayOf(6), -1, -1),
            13 to Skill(13, "Poison Craft", emptyMap(), arrayOf(12), 5, -1),


        )

        init {
            // Now that both maps exist, resolve dependencies
            skillIDtoSkill.values.forEach { skill ->
                skill.attack = Attack.getAttack(skill.attackID)
                if (skill.attack == null && skill.attackID != -1) {
                    Log.d("Skill Init", "Error getting attack w/id: ${skill.attackID}")
                }
                skill.shield = Shield.getShield(skill.shieldID)
                if (skill.shield == null && skill.shieldID != -1) {
                    Log.d("Skill Init", "Error getting shield w/id: ${skill.shieldID}")
                }
            }
        }

        fun getSkill(skillID: Int): Skill? {
            return if (skillIDtoSkill.contains(skillID)) skillIDtoSkill[skillID] else null
        }
    }
}