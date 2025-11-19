package com.example.bikinggame.playerCharacter

import org.json.JSONArray

class Shield {
    val deflection: Int // Chance to redirect attacks
    val fortitude: Int  // Hit points
    val regeneration: Int // Regenerates Hit points every round

    constructor(pDeflection: Int, pFortitude: Int, pRegeneration: Int) {
        deflection = pDeflection
        fortitude = pFortitude
        regeneration = pRegeneration
    }

    constructor(jsonArray: JSONArray, offset: IntWrapper) {
        deflection = jsonArray.get(offset.value++) as Int
        fortitude = jsonArray.get(offset.value++) as Int
        regeneration = jsonArray.get(offset.value++) as Int
    }

    fun serialize(jsonArray: JSONArray) {
        jsonArray.put(deflection)
        jsonArray.put(fortitude)
        jsonArray.put(regeneration)
    }
}