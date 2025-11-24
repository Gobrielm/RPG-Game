package com.example.bikinggame.playerCharacter

import android.util.Log
import androidx.compose.ui.geometry.Offset
import org.json.JSONArray
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow

class CharacterSkillTree {
    var exp: Int = 0
    val skillsUnlocked: ArrayList<Skill> = arrayListOf()

    constructor()

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

object SkillTrees {
    var skillTrees: Map<CharacterSubClass, Array<Pair<Int, Offset>>> = mapOf(
        CharacterSubClass.Knight to arrayOf(
            3 to Offset(0f, 0f),

            7 to Offset(0f, -1f),
            8 to Offset(0.71f, 0.71f),
            9 to Offset(-0.71f, 0.71f),

            10 to Offset(0f, -2f),
            11 to Offset(1.42f, 1.42f),
        )
    )
}


class Skill {

    val id: Int
    val statIncrease: MutableMap<BasicStats, Int> = mutableMapOf()
    val prerequisites: ArrayList<Int> = arrayListOf()
    private val attackID: Int
    private val shieldID: Int
    var attack: Attack? = null
    var shield: Shield? = null

    private constructor(pID: Int, pStatIncrease: Map<BasicStats, Int>, pPrerequisites: Array<Int>, pAttackID: Int, pShieldID: Int) {
        id = pID
        pStatIncrease.forEach { stat, amount ->
            statIncrease[stat] = amount
        }
        pPrerequisites.forEach { id ->
            prerequisites.add(id)
        }
        attackID = pAttackID
        shieldID = pShieldID
    }

    companion object {
        val skillIDtoSkill = hashMapOf<Int, Skill>(
            // Start of TraditionalMagic Tree
            1 to Skill(1, emptyMap(), emptyArray(), 2, -1),

            // Start of RitualMagic Tree
            2 to Skill(2, emptyMap(), emptyArray(), 2, -1),

            // Start of Knight Tree
            3 to Skill(3, emptyMap(), emptyArray(), 1, -1),

            7 to Skill(7, mapOf(BasicStats.Strength to 1), arrayOf(3), -1, -1),
            8 to Skill(8, mapOf(BasicStats.Constitution to 1), arrayOf(3), -1, -1),
            9 to Skill(9, mapOf(BasicStats.BaseHealth to 2), arrayOf(3), -1, -1),

            10 to Skill(10, emptyMap(), arrayOf(7), -1, -1),
            11 to Skill(11, mapOf(BasicStats.BaseHealth to 2), arrayOf(8), -1, -1),

            // Start of North Tree
            4 to Skill(4, emptyMap(), emptyArray(), 1, -1),

            // Start of TraditionalRanged Tree
            5 to Skill(5, emptyMap(), emptyArray(), 3, -1),

            // Start of NonTraditionalRanged Tree
            6 to Skill(6, emptyMap(), emptyArray(), 3, -1)


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