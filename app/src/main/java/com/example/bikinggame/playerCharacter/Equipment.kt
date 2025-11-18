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

    constructor(jsonArray: JSONArray, offset: IntWrapper) {
        id = jsonArray.get(offset.value++) as Int
        slot = EquipmentSlot.entries[jsonArray.get(offset.value++) as Int]
        val size = jsonArray.get(offset.value++) as Int

        statBoost = Array(size) { i ->
            val statIndex = jsonArray.get(offset.value++) as Int
            val boostValue = jsonArray.get(offset.value++) as Int

            Pair(BasicStats.entries[statIndex], boostValue)
        }

        attack = if (jsonArray[offset.value] == null) {
            offset.value++
            null
        } else {
            Attack(jsonArray, offset)
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
        if (attack == null) {
            jsonArray.put(null)
        } else {
            attack.serialize(jsonArray)
        }

    }
}