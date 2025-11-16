package com.example.bikinggame.playerCharacter

import org.json.JSONArray

enum class EquipmentSlot {
    // Main Slots
    HEAD,
    TORSO,
    BELT,
    LEGS,
    FEET,
    BACK,
    L_HAND,
    R_HAND,

    // Trinkets
    NECK,
    L_WRIST,
    R_WRIST,
    L_RING,
    R_RING,
}


class Equipment {
    val id: Int
    val slot: EquipmentSlot
    val statBoost: Array<Pair<BasicStats, Int>>
    val attack: Attack?

    constructor(pId: Int, pSlot: EquipmentSlot, pStatBoost: Array<Pair<BasicStats, Int>>, pAbility: Attack? = null) {
        id = pId
        slot = pSlot
        statBoost = pStatBoost
        attack = pAbility
    }

    constructor(jsonArray: JSONArray, offset: Int) {
        var currentInd = offset
        id = jsonArray.get(currentInd++) as Int
        slot = EquipmentSlot.entries[jsonArray.get(currentInd++) as Int]
        val size = jsonArray.get(currentInd++) as Int

        statBoost = Array(size) { i ->
            val statIndex = jsonArray.get(2 * i + currentInd) as Int
            val boostValue = jsonArray.get(2 * i + currentInd + 1) as Int

            Pair(BasicStats.entries[statIndex], boostValue)
        }
        currentInd += size * 2 + 1

        attack = if (currentInd != jsonArray.length()) {
            Attack(jsonArray, currentInd)
        } else {
            null
        }

    }

    fun serialize(jsonArray: JSONArray) {
        jsonArray.put(id)
        jsonArray.put(slot.ordinal)
        jsonArray.put(statBoost.size)
        for ((index: BasicStats, value: Int) in statBoost) {
            jsonArray.put(index.ordinal)
            jsonArray.put(value)
        }
        attack?.serialize(jsonArray)
    }
}