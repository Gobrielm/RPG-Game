package com.example.bikinggame.playerCharacter

import com.example.bikinggame.attack.Attack

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
    RING;
}


class Equipment {
    val id: Int
    val name: String
    val slot: EquipmentSlot
    val statBoost: Array<Pair<BasicStats, Int>>
    val attack: Attack?
    val shield: Shield?

    constructor(pId: Int, pName: String, pSlot: EquipmentSlot, pStatBoost: Array<Pair<BasicStats, Int>>, pAbility: Attack?, pShield: Shield?) {
        id = pId
        name = pName
        slot = pSlot
        statBoost = pStatBoost
        attack = pAbility
        shield = pShield
    }

    override fun toString(): String {
        var string = "$name: $slot -- "

        statBoost.forEach { (stat, amount) ->
            string += "$stat: $amount "
        }
        string += '\n'
        if (attack != null) string += "Attack: $attack "
        if (shield != null) string += "Shield: $shield "

        return string
    }

    companion object {
        val equipmentIDtoEquipment = mapOf<Int, Equipment>(
            1 to Equipment(1, "Rusty Sword", EquipmentSlot.MAIN_HAND,
            arrayOf(Pair(BasicStats.Strength, 2)),
            null, null
            )
        )

        fun getEquipment(equipmentID: Int): Equipment? {
            return equipmentIDtoEquipment[equipmentID]
        }
    }
}