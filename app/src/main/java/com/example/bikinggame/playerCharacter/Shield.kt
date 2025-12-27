package com.example.bikinggame.playerCharacter

import com.example.bikinggame.attack.Attack
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.math.sqrt
import kotlin.random.Random

class Shield {
    val id: Int
    val name: String
    val deflection: Int // Chance to redirect attacks
    val fortitude: Int  // Hit points
    val regeneration: Int // Regenerates Hit points every round
    var currentHitPoints: Int

    constructor(pId: Int, pName: String, pDeflection: Int, pFortitude: Int, pRegeneration: Int) {
        id = pId
        name = pName
        deflection = pDeflection
        fortitude = pFortitude
        regeneration = pRegeneration
        currentHitPoints = fortitude
    }

    constructor(shield: Shield) {
        id = shield.id
        name = shield.name
        deflection = shield.deflection
        fortitude = shield.fortitude
        regeneration = shield.regeneration
        currentHitPoints = fortitude
    }

    fun getHitPoints(): Int {
        return currentHitPoints
    }

    fun regenShield() {
        currentHitPoints += regeneration
        currentHitPoints = min(currentHitPoints, fortitude)
    }

    /**
     * @return With the (damage after blocking with Shield, Description of Event)
     */
    fun blockHit(attack: Attack, damage: Int, hitType: Attack.HitTypes): Pair<Int, String> {
        if (damage == 0 || hitType.getMultiplier() == 0f) return Pair(0, "")
        if (currentHitPoints == 0) return Pair(damage, "")

        val rand: Int = Random.nextInt(0, 100)
        val area = sqrt(attack.mass.toDouble())
        val pressure = (damage / area)

        val chanceToDeflect = deflection / hitType.getMultiplier() // Higher chance with glancing

        if (rand < chanceToDeflect) return Pair(0, "Deflected") // Deflected

        if (fortitude * 3 < pressure) {
            // Critical Shattering
            val damageBlock = currentHitPoints / 2.0f
            currentHitPoints = 0
            return Pair(round(damage - damageBlock).toInt(), "Critical Shield Shattering")
        }

        val pierceRatio = min(0.5, pressure / 2.0 / fortitude)

        val damageBlocked = min(round((1 - pierceRatio) * damage).toInt(), currentHitPoints)
        currentHitPoints -= damageBlocked

        return Pair(damage - damageBlocked, if (currentHitPoints == 0) {
           "Shield Shattering"
        } else {
            "Block"
        })
    }

    override fun toString(): String {
        return "$name --- Deflection: $deflection  Fortitude: $fortitude  Regeneration: $regeneration"
    }

    companion object {
        val shieldIDToShield = hashMapOf<Int, Shield>(
            1 to Shield(1, "Buckler", 8, 5, 0),
            2 to Shield(2, "Frost Shield", 10, 6, 2),
            3 to Shield(3, "Improvised Block", 2, 2, 0),
            4 to Shield(4, "Tower Shield", 6, 15, 0),
            5 to Shield(5, "Plate Shield", 10, 13, 0),
        )

        fun getShield(shieldID: Int): Shield? {
            return if (shieldIDToShield.contains(shieldID)) Shield(shieldIDToShield[shieldID]!!) else null
        }
    }
}