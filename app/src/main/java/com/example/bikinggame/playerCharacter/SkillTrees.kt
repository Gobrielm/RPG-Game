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

            // Root
            3 to Offset(0f, -2f),

            // Tier 1 paths
            7 to Offset(-3f, 0f),   // Strength
            8 to Offset(0f, 0f),    // Fortitude
            9 to Offset(3f, 0f),    // Heartiness

            // ───────────── Strength branch (LEFT) ─────────────
            43 to Offset(-3.5f, 1.5f),
            61 to Offset(-4.8f, 1.5f),

            44 to Offset(-3.5f, 3f),
            67 to Offset(-5f, 3f),

            45 to Offset(-3.5f, 4.5f),
            46 to Offset(-5f, 4.5f),

            52 to Offset(-1f, 9f),    // Strength III + Sacrifice II
            60 to Offset(2f, 9f),    // Strength III + Dex II

            // ───────────── Fortitude branch (CENTER) ─────────────
            10 to Offset(-1.2f, 1.5f),
            47 to Offset(0f, 1.5f),
            54 to Offset(-1.6f, 1f),

            48 to Offset(0f, 3f),
            53 to Offset(0f, 4.5f),
            66 to Offset(0f, 6f),

            57 to Offset(-1.6f, 4.5f),   // Shield → Attack style

            // Mixed Fortitude nodes
            55 to Offset(-1.7f, 2.3f),    // Strength + Fortitude
            62 to Offset(1.8f, 3f),     // Fortitude + Heartiness

            // ───────────── Heartiness / HP / Dex branch (RIGHT) ─────────────
            11 to Offset(3f, 1.5f),

            49 to Offset(3f, 3f),
            58 to Offset(4.8f, 3f),

            50 to Offset(2f, 5.3f),
            51 to Offset(1.6f, 4.5f),   // Sacrifice + Fortitude

            59 to Offset(4.8f, 4.5f),

            56 to Offset(3.5f, 4.5f),       // Heartiness III

            // ───────────── Hybrid / Endgame ─────────────
            63 to Offset(2f, 6.5f),   // Magical Melee
            69 to Offset(2f, 7.8f)    // Fire Sword
        ),

        CharacterMainClass.RANGED to arrayOf(

            // Root
            6 to Offset(0f, -2f),

            // Tier 1 paths
            12 to Offset(-3f, 0f),    // Dexterity
            73 to Offset(2f, 0f),     // Stamina
            84 to Offset(5f, 0f),     // Strength

            // ───────────── Magic (FAR LEFT) ─────────────
            94 to Offset(-6f, 1.5f),
            95 to Offset(-6f, 3f),
            96 to Offset(-6f, 4.5f),

            // ───────────── Dexterity (LEFT) ─────────────
            70 to Offset(-3f, 1.5f),
            79 to Offset(-3.5f, 5f),
            80 to Offset(-4.6f, 3f),
            88 to Offset(-4.5f, 6f),

            // ───────────── Toxicology (MID-LEFT) ─────────────
            71 to Offset(-1f, 1.5f),
            72 to Offset(-1f, 3f),
            13 to Offset(-1.8f, 2.5f),
            77 to Offset(-2.2f, 4.5f),
            78 to Offset(-0.2f, 4.5f),
            81 to Offset(-1f, 4.5f),

            // ───────────── Health (MID-RIGHT) ─────────────
            90 to Offset(1f, 1.5f),
            91 to Offset(1f, 3f),

            // ───────────── Stamina (RIGHT) ─────────────
            74 to Offset(2.8f, 1.5f),
            75 to Offset(2.8f, 3f),
            76 to Offset(4.2f, 3f),
            93 to Offset(2.8f, 4.5f),
            89 to Offset(2.8f, 6f),

            // ───────────── Strength (FAR RIGHT) ─────────────
            85 to Offset(5f, 3f),
            92 to Offset(5.8f, 5f),
            87 to Offset(6.6f, 4.5f),

            // ───────────── Hybrid / Endgame Attacks ─────────────
            82 to Offset(-1.8f, 6f),  // Dex III + Toxicology III
            83 to Offset(0.6f, 6f),   // Dex III + Stamina III
            86 to Offset(4.2f, 6f)    // Strength II + Stamina III
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
            // Start of Magic Tree
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

            // Start of Melee Tree
            3 to Skill(3, "Humble Beginnings", emptyMap(), emptyArray(), 1, -1),
            7 to Skill(7, "Path of Strength", mapOf(BasicStats.Strength to 1), arrayOf(3), -1, -1),
            8 to Skill(8, "Path of Fortitude", mapOf(BasicStats.Constitution to 1), arrayOf(3), -1, -1),
            9 to Skill(9, "Path of Heartiness", mapOf(BasicStats.BaseHealth to 2), arrayOf(3), -1, -1),

            10 to Skill(10, "Improvised Block", emptyMap(), arrayOf(8), -1, 3), // TODO: Buff Block
            11 to Skill(11, "Heartiness II", mapOf(BasicStats.BaseHealth to 2), arrayOf(8), -1, -1),
            43 to Skill(43, "Strong Strike", emptyMap(), arrayOf(7), -1, -1),// TODO: Add Atk
            47 to Skill(47, "Fortitude II", emptyMap(), arrayOf(8), -1, -1),// TODO
            54 to Skill(54, "Strength and Weakness", emptyMap(), arrayOf(8), -1, -1),// TODO
            61 to Skill(61, "Additional Workout", emptyMap(), arrayOf(7), -1, -1), // TODO
            62 to Skill(62, "Stone Skin", emptyMap(), arrayOf(8, 9), -1, -1),// TODO

            44 to Skill(44, "Strength II", emptyMap(), arrayOf(43), -1, -1),// TODO
            55 to Skill(55, "Weakening Strike", emptyMap(), arrayOf(43, 54), -1, -1),// TODO: Add Atk
            48 to Skill(48, "Impenetrable Shield", emptyMap(), arrayOf(47), -1, -1),// TODO: Add Shield
            49 to Skill(49, "Path of Sacrifice", emptyMap(), arrayOf(11), -1, -1),// TODO
            56 to Skill(56, "Heartiness III", emptyMap(), arrayOf(11), -1, -1),// TODO
            58 to Skill(58, "Path of Dexterity", emptyMap(), arrayOf(11), -1, -1),// TODO

            45 to Skill(45, "Strength III", emptyMap(), arrayOf(44), -1, -1),// TODO
            46 to Skill(46, "Strength Strike", emptyMap(), arrayOf(44), -1, -1),// TODO: Add atk
            50 to Skill(50, "Sacrifice II", emptyMap(), arrayOf(49), -1, -1),// TODO
            51 to Skill(51, "Empowering Sacrifice", emptyMap(), arrayOf(47, 49), -1, -1),// TODO: Add atk
            53 to Skill(53, "Fortitude III", emptyMap(), arrayOf(48), -1, -1),// TODO
            57 to Skill(57, "Pierce Strike", emptyMap(), arrayOf(48), -1, -1),// TODO: Add atk
            59 to Skill(59, "Dexterity II", emptyMap(), arrayOf(58), -1, -1),// TODO
            67 to Skill(67, "Strength Hyper-Focus", emptyMap(), arrayOf(44), -1, -1),// TODO

            52 to Skill(52, "Bludgeoning Blow", emptyMap(), arrayOf(45, 50), -1, -1),// TODO: Add Atk
            60 to Skill(60, "Speed Attack", emptyMap(), arrayOf(45, 59), -1, -1),// TODO: Add Atk
            63 to Skill(63, "Magical Melee Attacks", emptyMap(), arrayOf(50), -1, -1),// TODO
            66 to Skill(66, "Constitution Hyper-Focus", emptyMap(), arrayOf(53), -1, -1),// TODO

            69 to Skill(69, "Fire Sword", emptyMap(), arrayOf(63), -1, -1),// TODO: Add atk

            // Start of North Tree
//            4 to Skill(4, "Humble Beginnings", emptyMap(), emptyArray(), 1, -1),

            // Start of TraditionalRanged Tree
//            5 to Skill(5, "Humble Beginnings", emptyMap(), emptyArray(), 3, -1),

            // Start of Ranged Tree
            6 to Skill(6, "Humble Beginnings", emptyMap(), emptyArray(), 3, -1),
            12 to Skill(12, "Path of Dexterity", mapOf(BasicStats.Dexterity to 1), arrayOf(6), -1, -1),
            73 to Skill(73, "Path of Stamina", mapOf(BasicStats.BaseStamina to 3), arrayOf(6), -1, -1),
            84 to Skill(84, "Path of Strength", mapOf(BasicStats.Strength to 1), arrayOf(6), -1, -1),

            94 to Skill(94, "Ranged Magic", emptyMap(), arrayOf(12), -1, -1), // TODO
            70 to Skill(70, "Dexterity II", emptyMap(), arrayOf(12), -1, -1),// TODO
            71 to Skill(71, "Toxicology", emptyMap(), arrayOf(12), -1, -1),// TODO
            90 to Skill(90, "Increased Health", emptyMap(), arrayOf(73), -1, -1), // TODO
            74 to Skill(74, "Improvised Shield", emptyMap(), arrayOf(73), -1, -1),// TODO: Add shield
            85 to Skill(85, "Strength II", emptyMap(), arrayOf(84), -1, -1),// TODO

            13 to Skill(13, "Poison Craft", emptyMap(), arrayOf(71), 5, -1), // TODO
            95 to Skill(95, "Ranged Magic II", emptyMap(), arrayOf(94), -1, -1), // TODO
            79 to Skill(79, "Dexterity III", emptyMap(), arrayOf(70), -1, -1), // TODO
            80 to Skill(80, "Barrage Attack", emptyMap(), arrayOf(70), -1, -1), // TODO: ADd Attack
            72 to Skill(72, "Toxicology II", emptyMap(), arrayOf(71), -1, -1), // TODO
            91 to Skill(91, "Increased Health II", emptyMap(), arrayOf(90), -1, -1), // TODO
            75 to Skill(75, "Stamina II", emptyMap(), arrayOf(74), -1, -1), // TODO
            76 to Skill(76, "Improvised Shield II", emptyMap(), arrayOf(74), -1, -1), // TODO: Add shield
            92 to Skill(92, "Strength Hyper-Focus", emptyMap(), arrayOf(85), -1, -1), // TODO
            87 to Skill(87, "Increased Constitution", emptyMap(), arrayOf(85), -1, -1), // TODO

            77 to Skill(77, "Weakening Shots", emptyMap(), arrayOf(72), -1, -1), // TODO: Add Attack
            78 to Skill(78, "Mana-Drain Shots", emptyMap(), arrayOf(72), -1, -1), // TODO: Add Attack
            96 to Skill(96, "Elemental Arrow", emptyMap(), arrayOf(95), -1, -1), // TODO: ADd Attack
            88 to Skill(88, "Dexterity Hyper-Focus", emptyMap(), arrayOf(79), -1, -1), // TODO
            81 to Skill(81, "Toxicology III", emptyMap(), arrayOf(72), -1, -1), // TODO
            93 to Skill(93, "Stamina III", emptyMap(), arrayOf(75), -1, -1), // TODO

            82 to Skill(82, "Venom Arrow", emptyMap(), arrayOf(79, 81), -1, -1), // TODO: ADd Attack
            83 to Skill(83, "Lightning Arrow", emptyMap(), arrayOf(79, 93), -1, -1), // TODO: ADd attack
            86 to Skill(86, "Ballista Shot", emptyMap(), arrayOf(85, 93), -1, -1), // TODO: ADd attack
            89 to Skill(89, "Stamina Hyper-Focus", emptyMap(), arrayOf(93), -1, -1), // TODO
        )

        init {
            // Wait for skills
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