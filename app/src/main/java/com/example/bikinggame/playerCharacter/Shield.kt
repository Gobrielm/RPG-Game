package com.example.bikinggame.playerCharacter

class Shield {
    val deflection: Int // Chance to redirect attacks
    val fortitude: Int  // Hit points

    constructor(pDeflection: Int, pFortitude: Int) {
        deflection = pDeflection
        fortitude = pFortitude
    }
}