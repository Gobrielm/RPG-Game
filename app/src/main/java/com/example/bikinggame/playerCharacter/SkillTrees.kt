package com.example.bikinggame.playerCharacter

class SkillTree {


    companion object {
        val skillTrees: Map<CharacterSubClass, Array<Int>> = mapOf(
            CharacterSubClass.Knight to arrayOf(1, 2),


        )

//        val skills: ArrayList<Skill> = arrayListOf(
//            Skill()
//        )
    }

}


class Skill {

    val id: Int
    val statIncrease: Map<BasicStats, Int>
    val prerequisites: Array<Int>
    val attack: Attack?


    constructor(pStatIncrease: Map<BasicStats, Int>, pPrerequisites: Array<Int> = arrayOf()) {
        id = numberOfSkills++
        statIncrease = pStatIncrease
        prerequisites = pPrerequisites
        attack = null

    }

    constructor(pStatIncrease: Map<BasicStats, Int>, pPrerequisites: Array<Int>, pAbility: Attack) {
        id = numberOfSkills++
        statIncrease = pStatIncrease
        prerequisites = pPrerequisites
        attack = pAbility
    }

    companion object {
        var numberOfSkills = 0
    }
}