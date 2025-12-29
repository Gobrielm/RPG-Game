package com.mainApp.rpg.playerCharacter

import com.mainApp.rpg.attack.Attack

enum class EquipmentSlot {
    // Main Slots
    HEAD,
    TORSO,
    OFF_HAND,
    MAIN_HAND,

    // Trinkets
    NECK,
    RING;
}


class Equipment {
    val id: Int
    val name: String
    val slot: EquipmentSlot
    val statBoost: Array<Pair<BasicStats, Int>>
    val attack: Attack?
    val shield: Shield?

    constructor(pId: Int, pName: String, pSlot: EquipmentSlot, pStatBoost: Array<Pair<BasicStats, Int>>, pAttack: Attack?, pShield: Shield?) {
        id = pId
        name = pName
        slot = pSlot
        statBoost = pStatBoost
        attack = pAttack
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
            // Starter Weapons
            1 to Equipment(1, "Rusty Sword", EquipmentSlot.MAIN_HAND,
                arrayOf(Pair(BasicStats.Strength, 2)),
                null, null
            ),
            2 to Equipment(2, "Worn-Out Bow", EquipmentSlot.MAIN_HAND,
                arrayOf(Pair(BasicStats.Strength, 1), Pair(BasicStats.Dexterity, 1)),
                null, null
            ),
            3 to Equipment(3, "Basic Staff", EquipmentSlot.MAIN_HAND,
                arrayOf(Pair(BasicStats.Casting, 1)),
                null, null
            ),

            // Warrior
            4 to Equipment(4, "Iron Longsword", EquipmentSlot.MAIN_HAND,
                arrayOf(
                    Pair(BasicStats.Strength, 4),
                    Pair(BasicStats.Constitution, 2)
                ),
                null, null
            ),
            5 to Equipment(5, "Heavy War Axe", EquipmentSlot.MAIN_HAND,
                arrayOf(
                    Pair(BasicStats.Strength, 5),
                    Pair(BasicStats.BaseStamina, 5)
                ),
                null, null
            ),
            6 to Equipment(6, "Knight’s Sword", EquipmentSlot.MAIN_HAND,
                arrayOf(
                    Pair(BasicStats.Strength, 4),
                    Pair(BasicStats.Constitution, 3),
                    Pair(BasicStats.BaseHealth, 10)
                ),
                null, null
            ),

            // Ranged
            7 to Equipment(7, "Reinforced Shortbow", EquipmentSlot.MAIN_HAND,
                arrayOf(
                    Pair(BasicStats.Dexterity, 3),
                    Pair(BasicStats.Strength, 2)
                ),
                null, null
            ),
            8 to Equipment(8, "Hunter’s Longbow", EquipmentSlot.MAIN_HAND,
                arrayOf(
                    Pair(BasicStats.Dexterity, 4),
                    Pair(BasicStats.Strength, 2),
                    Pair(BasicStats.BaseStamina, 5)
                ),
                null, null
            ),
            9 to Equipment(9, "Warbow", EquipmentSlot.MAIN_HAND,
                arrayOf(
                    Pair(BasicStats.Dexterity, 3),
                    Pair(BasicStats.Strength, 3)
                ),
                null, null
            ),

            // Mage
            10 to Equipment(10, "Apprentice Wand", EquipmentSlot.MAIN_HAND,
                arrayOf(
                    Pair(BasicStats.Casting, 3),
                    Pair(BasicStats.Intelligence, 2)
                ),
                null, null
            ),
            11 to Equipment(11, "Channeler’s Staff", EquipmentSlot.MAIN_HAND,
                arrayOf(
                    Pair(BasicStats.Casting, 4),
                    Pair(BasicStats.Intelligence, 3),
                    Pair(BasicStats.BaseMana, 10)
                ),
                null, null
            ),
            12 to Equipment(12, "Arcane Focus Rod", EquipmentSlot.MAIN_HAND,
                arrayOf(
                    Pair(BasicStats.Casting, 5),
                    Pair(BasicStats.Intelligence, 2)
                ),
                null, null
            ),

            //Shields
            13 to Equipment(13, "Buckler", EquipmentSlot.OFF_HAND,
                arrayOf(
                    Pair(BasicStats.Constitution, 1)
                ),
                null, Shield.getShield(1)
            ),
            14 to Equipment(14, "Tower Shield", EquipmentSlot.OFF_HAND,
                arrayOf(
                    Pair(BasicStats.Constitution, 3)
                ),
                null, Shield.getShield(4)
            ),
            15 to Equipment(15, "Plate Shield", EquipmentSlot.OFF_HAND,
                arrayOf(
                    Pair(BasicStats.Constitution, 5)
                ),
                null, Shield.getShield(5)
            ),

            // Mage Off-Hands
            16 to Equipment(16, "Magic Tomb", EquipmentSlot.OFF_HAND,
                arrayOf(
                    Pair(BasicStats.Intelligence, 2)
                ),
                Attack.getAttack(10), null
            ),
            17 to Equipment(17, "Focusing Orb", EquipmentSlot.OFF_HAND,
                arrayOf(
                    Pair(BasicStats.Casting, 3)
                ),
                null, null
            ),

            // Ranged Off-Hands
            18 to Equipment(18, "Quiver", EquipmentSlot.OFF_HAND,
                arrayOf(
                    Pair(BasicStats.Dexterity, 3)
                ),
                null, null
            ),
            19 to Equipment(19, "Ranger Knife", EquipmentSlot.OFF_HAND,
                arrayOf(
                    Pair(BasicStats.Strength, 2),
                    Pair(BasicStats.Dexterity, 2)
                ),
                Attack.getAttack(11), null
            ),

            // Melee Armor
            20 to Equipment(20, "Boiled-Leather Helmet", EquipmentSlot.HEAD,
                arrayOf(
                    Pair(BasicStats.Constitution, 2)
                ),
                null, null
            ),
            21 to Equipment(21, "Boiled-Leather Tunic", EquipmentSlot.TORSO,
                arrayOf(
                    Pair(BasicStats.Constitution, 3)
                ),
                null, null
            ),
            22 to Equipment(22, "Plate Helm", EquipmentSlot.HEAD,
                arrayOf(
                    Pair(BasicStats.Constitution, 4)
                ),
                null, null
            ),
            23 to Equipment(23, "Plate Armor", EquipmentSlot.TORSO,
                arrayOf(
                    Pair(BasicStats.Constitution, 5)
                ),
                null, null
            ),

            // Magic Armor
            24 to Equipment(24, "Apprentice Hat", EquipmentSlot.HEAD,
                arrayOf(
                    Pair(BasicStats.Intelligence, 2)
                ),
                null, null
            ),
            25 to Equipment(25, "Apprentice Robes", EquipmentSlot.TORSO,
                arrayOf(
                    Pair(BasicStats.Constitution, 3)
                ),
                null, null
            ),
            26 to Equipment(26, "Mage Helm", EquipmentSlot.HEAD,
                arrayOf(
                    Pair(BasicStats.Intelligence, 3),
                    Pair(BasicStats.Constitution, 1),
                    Pair(BasicStats.Constitution, 1),
                ),
                null, null
            ),
            27 to Equipment(27, "Mage Chestpiece", EquipmentSlot.TORSO,
                arrayOf(
                    Pair(BasicStats.Intelligence, 4),
                    Pair(BasicStats.Constitution, 1),
                    Pair(BasicStats.Constitution, 2),
                ),
                null, null
            ),

            // Ranged Armor
            28 to Equipment(28, "Hunting Hat", EquipmentSlot.HEAD,
                arrayOf(
                    Pair(BasicStats.Dexterity, 2)
                ),
                null, null
            ),
            29 to Equipment(29, "Hunting Garb", EquipmentSlot.TORSO,
                arrayOf(
                    Pair(BasicStats.Dexterity, 2)
                ),
                null, null
            ),
            30 to Equipment(30, "Steel Half-Helm", EquipmentSlot.HEAD,
                arrayOf(
                    Pair(BasicStats.Dexterity, 2),
                    Pair(BasicStats.Constitution, 2)
                ),
                null, null
            ),
            31 to Equipment(31, "Chainmail", EquipmentSlot.TORSO,
                arrayOf(
                    Pair(BasicStats.Dexterity, 4),
                    Pair(BasicStats.Constitution, 2)
                ),
                null, null
            ),

            // Rings
            32 to Equipment(32, "Garnet Ring", EquipmentSlot.RING,
                arrayOf(
                    Pair(BasicStats.BaseHealth, 7)
                ),
                null, null
            ),
            33 to Equipment(33, "Sapphire Ring", EquipmentSlot.RING,
                arrayOf(
                    Pair(BasicStats.BaseMana, 5)
                ),
                null, null
            ),
            34 to Equipment(34, "Jade Ring", EquipmentSlot.RING,
                arrayOf(
                    Pair(BasicStats.BaseStamina, 5)
                ),
                null, null
            ),
            // Upgraded
            35 to Equipment(35, "Ruby Ring", EquipmentSlot.RING,
                arrayOf(
                    Pair(BasicStats.BaseHealth, 15)
                ),
                null, null
            ),
            36 to Equipment(36, "Kyanite Ring", EquipmentSlot.RING,
                arrayOf(
                    Pair(BasicStats.BaseMana, 10)
                ),
                null, null
            ),
            37 to Equipment(37, "Emerald Ring", EquipmentSlot.RING,
                arrayOf(
                    Pair(BasicStats.BaseStamina, 10)
                ),
                null, null
            ),

            //Neck
            38 to Equipment(38, "Bear Talisman", EquipmentSlot.NECK,
                emptyArray<Pair<BasicStats, Int>>(),
                Attack.getAttack(7), null
            ),
            39 to Equipment(39, "Eagle Talisman", EquipmentSlot.NECK,
                emptyArray<Pair<BasicStats, Int>>(),
                Attack.getAttack(8), null
            ),
            40 to Equipment(40, "Moose Talisman", EquipmentSlot.NECK,
                emptyArray<Pair<BasicStats, Int>>(),
                Attack.getAttack(9), null
            ),
        )


        fun getEquipment(equipmentID: Int): Equipment? {
            return equipmentIDtoEquipment[equipmentID]
        }
    }
}