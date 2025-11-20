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
    OFF_HAND,
    MAIN_HAND,

    // Trinkets
    NECK,
    WRIST,
    RING,
}


class Equipment {
    val id: Int
    val slot: EquipmentSlot
    val statBoost: Array<Pair<BasicStats, Int>>
    val attack: Attack?
    val shield: Shield?

    constructor(pId: Int, pSlot: EquipmentSlot, pStatBoost: Array<Pair<BasicStats, Int>>) {
        id = pId
        slot = pSlot
        statBoost = pStatBoost
        attack = null
        shield = null
    }

    constructor(pId: Int, pSlot: EquipmentSlot, pStatBoost: Array<Pair<BasicStats, Int>>, pAbility: Attack?, pShield: Shield? = null) {
        id = pId
        slot = pSlot
        statBoost = pStatBoost
        attack = pAbility
        shield = pShield
    }

    constructor(pId: Int, pSlot: EquipmentSlot, pStatBoost: Array<Pair<BasicStats, Int>>, pShield: Shield) {
        id = pId
        slot = pSlot
        statBoost = pStatBoost
        shield = pShield
        attack = null
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
            // TODO: GET ATTACK WITH THE ID
            val id = jsonArray[offset.value++]
            null
        }

        shield = if (jsonArray[offset.value] == null) {
            offset.value++
            null
        } else {
            // TODO: GET SHIELD WITH THE ID
            val id = jsonArray[offset.value++]
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
        if (attack == null) {
            jsonArray.put(null)
        } else {
            jsonArray.put(attack.id)
        }
        if (shield == null) {
            jsonArray.put(null)
        } else {
            jsonArray.put(shield.id)
        }
    }
}