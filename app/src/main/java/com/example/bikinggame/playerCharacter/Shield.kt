package com.example.bikinggame.playerCharacter

import org.json.JSONArray

class Shield {
    val id: Int
    val name: String
    val deflection: Int // Chance to redirect attacks
    val fortitude: Int  // Hit points
    val regeneration: Int // Regenerates Hit points every round

    constructor(pId: Int, pName: String, pDeflection: Int, pFortitude: Int, pRegeneration: Int) {
        id = pId
        name = pName
        deflection = pDeflection
        fortitude = pFortitude
        regeneration = pRegeneration
    }

    constructor(jsonArray: JSONArray, offset: IntWrapper) {
        id = jsonArray.get(offset.value++) as Int
        name = jsonArray.get(offset.value++) as String
        deflection = jsonArray.get(offset.value++) as Int
        fortitude = jsonArray.get(offset.value++) as Int
        regeneration = jsonArray.get(offset.value++) as Int
    }

//    fun serialize(jsonArray: JSONArray) {
//        jsonArray.put(id)
//        jsonArray.put(deflection)
//        jsonArray.put(fortitude)
//        jsonArray.put(regeneration)
//    }

    companion object {
        val shieldIDToShield = hashMapOf<Int, Shield>(
            1 to Shield(1, "Basic Shield", 10, 8, 0),

            2 to Shield(2, "Basic Magic Shield", 10, 4, 1)
        )

        fun getShield(shieldID: Int): Shield? {
            return if (shieldIDToShield.contains(shieldID)) shieldIDToShield[shieldID] else null
        }
    }
}