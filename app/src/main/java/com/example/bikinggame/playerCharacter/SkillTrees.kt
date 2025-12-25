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
    var skillTrees: Map<CharacterMainClass, Array<Pair<Int, Offset>>> = mapOf(
        CharacterMainClass.MELEE to arrayOf(
            3 to Offset(0f, 0f),

            7 to Offset(0f, -1f),
            8 to Offset(0.71f, 0.71f),
            9 to Offset(-0.71f, 0.71f),

            10 to Offset(1.576f, 1.21f),
            11 to Offset(1.21f, 1.576f),
        ),

        CharacterMainClass.RANGED to arrayOf(
            6 to Offset(0f, 0f),

            12 to Offset(0.71f, 0.71f),

            13 to Offset(1.21f, 1.576f),
        ),

        CharacterMainClass.MAGIC to arrayOf(

            // Root
            1 to Offset(0f, -2f),

            // Core magic
            14 to Offset(0f, 0f),
            15 to Offset(0f, 1.5f),
            17 to Offset(0f, 3f),
            30 to Offset(0f, 4.5f),

            // Magic study chain
            24 to Offset(1f, 1.5f),
            25 to Offset(1f, 3f),

            // Frost branch
            16 to Offset(-2f, 1.5f),
            22 to Offset(-3.5f, 3f),
            29 to Offset(-5f, 4.5f),
            36 to Offset(-1.2f, 3f),
            39 to Offset(-3.5f, 4.5f),
            31 to Offset(-5f, 6f),

            // Fire branch
            19 to Offset(1.8f, 1.5f),
            23 to Offset(3.5f, 3f),
            28 to Offset(5f, 4.5f),
            33 to Offset(6.5f, 6f),
            38 to Offset(1.8f, 3f),
            32 to Offset(5f, 6f),

            // Healing branch
            18 to Offset(-1.8f, 3f),
            40 to Offset(-1.8f, 4.5f),
            20 to Offset(0f, 6f), // requires 18 + 25

            // Forbidden / banned magic
            21 to Offset(3.5f, 1.5f),
            26 to Offset(5.5f, 3f),
            27 to Offset(3.5f, 3f),
            41 to Offset(4.5f, 3.5f),
            42 to Offset(3.5f, 6f),

            // High-tier / combined
            37 to Offset(1.8f, 4.5f),
            34 to Offset(0.9f, 6.5f), // Casting III + Magic Study III
            35 to Offset(4f, 7f)  // Fire III + Banned Magic II
        )

    )

    fun getSkillTree(mainClass: CharacterMainClass): Array<Pair<Int, Offset>> {
        return skillTrees[mainClass] ?: Array(0) { Pair(0, Offset(0f, 0f))}
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
            16 to Skill(16, "Frost Study", emptyMap(), arrayOf(1), -1, -1), //TODO
            19 to Skill(19, "Fire Study", emptyMap(), arrayOf(1), -1, -1), //TODO

            15 to Skill(15, "Casting", mapOf(BasicStats.Casting to 1), arrayOf(14), -1, -1), //TODO
            18 to Skill(18, "Path of Healing", emptyMap(), arrayOf(14), 6, -1),
            21 to Skill(21, "Banned Magic", emptyMap(), arrayOf(14), -1, -1), // TODO
            22 to Skill(22, "Frost Shield", emptyMap(), arrayOf(16), -1, -1), // TODO: Add Shield
            23 to Skill(23, "Fire Attack", emptyMap(), arrayOf(19), -1, -1), // TODO: ADd Atk
            24 to Skill(24, "Magic Study II", emptyMap(), arrayOf(14), -1, -1), // TODO
            36 to Skill(36, "Frost Blast", emptyMap(), arrayOf(16), -1, -1), // TODO: Add atk
            38 to Skill(38, "Burning Passion", emptyMap(), arrayOf(19), -1, -1), // TODO

            17 to Skill(17, "Casting II", mapOf(BasicStats.Casting to 1), arrayOf(15), -1, -1), //TODO
            25 to Skill(25, "Magic Study III", emptyMap(), arrayOf(24), -1, -1), // TODO
            26 to Skill(26, "Drain Attack", emptyMap(), arrayOf(21), -1, -1), // TODO: Add drain atk
            27 to Skill(27, "Banned Magic II", emptyMap(), arrayOf(21), -1, -1), // TODO
            28 to Skill(28, "Fire Study II", emptyMap(), arrayOf(23), -1, -1), // TODO
            29 to Skill(29, "Frost Study II", emptyMap(), arrayOf(22), -1, -1), // TODO
            37 to Skill(37, "Mana Blast", emptyMap(), arrayOf(24), -1, -1), // TODO: Add atk
            39 to Skill(39, "Icy Veins", emptyMap(), arrayOf(22), -1, -1), // TODO
            40 to Skill(40, "Healthy Fortitude", emptyMap(), arrayOf(18), -1, -1), // TODO
            41 to Skill(41, "Forbidden Knowledge", emptyMap(), arrayOf(21), -1, -1), // TODO

            20 to Skill(20, "Healing Improved", emptyMap(), arrayOf(18, 25), -1, -1), // TODO: Add IMP Healing
            30 to Skill(30, "Casting III", emptyMap(), arrayOf(17), -1, -1), // TODO
            31 to Skill(31, "Frost Attack", emptyMap(), arrayOf(29), -1, -1), // TODO: Frost Atk
            32 to Skill(32, "Fire Attack", emptyMap(), arrayOf(28), -1, -1), // TODO: Fire Atk
            33 to Skill(33, "Fire Study III", emptyMap(), arrayOf(28), -1, -1), // TODO
            42 to Skill(42, "Undead Fortification", emptyMap(), arrayOf(27), -1, -1), // TODO

            34 to Skill(34, "Magic Spear", emptyMap(), arrayOf(30, 25), -1, -1), // TODO: Spear Atk
            35 to Skill(35, "Explosion Attack", emptyMap(), arrayOf(33, 27), -1, -1), // TODO: Explosion Atk



            // Start of RitualMagic Tree
//            2 to Skill(2, "Humble Beginnings", emptyMap(), emptyArray(), 2, -1),

            // Start of Knight Tree
            3 to Skill(3, "Humble Beginnings", emptyMap(), emptyArray(), 1, -1),
            7 to Skill(7, "Path of Strength", mapOf(BasicStats.Strength to 1), arrayOf(3), -1, -1),
            8 to Skill(8, "Path of Fortitude", mapOf(BasicStats.Constitution to 1), arrayOf(3), -1, -1),
            9 to Skill(9, "Path of Heartiness", mapOf(BasicStats.BaseHealth to 2), arrayOf(3), -1, -1),
            10 to Skill(10, "Improvised Block", emptyMap(), arrayOf(8), -1, 3),
            11 to Skill(11, "Improved Heartiness", mapOf(BasicStats.BaseHealth to 2), arrayOf(8), -1, -1),

            // Start of North Tree
//            4 to Skill(4, "Humble Beginnings", emptyMap(), emptyArray(), 1, -1),

            // Start of TraditionalRanged Tree
//            5 to Skill(5, "Humble Beginnings", emptyMap(), emptyArray(), 3, -1),

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