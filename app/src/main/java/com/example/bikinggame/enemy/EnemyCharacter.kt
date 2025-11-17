package com.example.bikinggame.enemy

import com.example.bikinggame.playerCharacter.CharacterStats
import kotlin.math.abs
import kotlin.random.Random

class EnemyCharacter {
    val id: Int
    var baseStats: CharacterStats = CharacterStats()
    var currentStats: CharacterStats = CharacterStats()

    // Used for creating the first character
    constructor(pId: Int, pBaseStats: CharacterStats) {
        id = pId
        baseStats = pBaseStats
        currentStats = baseStats
    }

    constructor(pBaseStats: CharacterStats) {
        id = abs(Random.nextInt())
        baseStats = pBaseStats
        currentStats = baseStats
    }

    override fun toString(): String {
        return "$id: $currentStats"
    }
}