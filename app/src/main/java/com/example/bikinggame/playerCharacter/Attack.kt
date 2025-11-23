package com.example.bikinggame.playerCharacter

import org.json.JSONArray
import kotlin.random.Random

class Attack {
    val id: Int
    val name: String
    val mass: Int
    val velocity: Int
    val pierce: Int
    val accuracy: Int // Chance to do direct hit, 0 - 100

    constructor(pId: Int, pName: String, pMass: Int, pVelocity: Int, pPierce: Int, pAccuracy: Int) {
        id = pId
        mass = pMass
        name = pName
        velocity = pVelocity
        pierce = pPierce
        accuracy = pAccuracy
    }

    constructor(jsonArray: JSONArray, offset: IntWrapper) {
        id = jsonArray.get(offset.value++) as Int
        name = jsonArray.get(offset.value++) as String
        mass = jsonArray.get(offset.value++) as Int
        velocity = jsonArray.get(offset.value++) as Int
        pierce = jsonArray.get(offset.value++) as Int
        accuracy = jsonArray.get(offset.value++) as Int
    }

    fun getMomentum(): Int {
        return velocity * mass
    }

    fun getHitMultiplier(): Float {
        val rand: Int = Random.nextInt(0, 100)

        return if (rand > accuracy) {
            1.0f
        } else if (rand > accuracy / 2.0f) {
            0.5f
        } else {
            0.0f
        }
    }

    override fun toString(): String {
        return "$name --- Mass: $mass  Velocity: $velocity  Pierce: $pierce  Accuracy: $accuracy"
    }

    companion object {
        val attackIDToAttack = hashMapOf<Int, Attack>(
            1 to Attack(1, "Basic Hit", 3, 5, 1, 90),

            2 to Attack(2, "Mana Blast", 2, 8, 2, 80),

            3 to Attack(3, "Normal Shot", 2, 7, 3, 85)
        )


        fun getAttack(attackID: Int): Attack? {
            return if (attackIDToAttack.contains(attackID)) attackIDToAttack[attackID] else null
        }
    }
}