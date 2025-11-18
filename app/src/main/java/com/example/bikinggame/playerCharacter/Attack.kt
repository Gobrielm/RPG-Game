package com.example.bikinggame.playerCharacter

import org.json.JSONArray
import kotlin.random.Random

class Attack {
    val mass: Int
    val velocity: Int
    val pierce: Int
    val accuracy: Int // Chance to do direct hit, 0 - 100

    constructor(pMass: Int, pVelocity: Int, pPierce: Int, pAccuracy: Int) {
        mass = pMass
        velocity = pVelocity
        pierce = pPierce
        accuracy = pAccuracy
    }

    constructor(jsonArray: JSONArray, offset: IntWrapper) {
        mass = jsonArray.get(offset.value++) as Int
        velocity = jsonArray.get(offset.value++) as Int
        pierce = jsonArray.get(offset.value++) as Int
        accuracy = jsonArray.get(offset.value++) as Int
    }

    fun serialize(jsonArray: JSONArray) {
        jsonArray.put(mass)
        jsonArray.put(velocity)
        jsonArray.put(pierce)
        jsonArray.put(accuracy)
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
}