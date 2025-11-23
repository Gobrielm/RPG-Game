package com.example.bikinggame.homepage.inventory

import com.example.bikinggame.playerCharacter.Equipment
import com.example.bikinggame.playerCharacter.EquipmentSlot
import com.example.bikinggame.playerCharacter.PlayerCharacter
import kotlin.collections.forEach

object PlayerInventory {
    val playerCharacters: ArrayList<PlayerCharacter> = ArrayList()
    val playerEquipment: MutableMap<Int, Int> = mutableMapOf()

    fun getCharacter(id: Int): PlayerCharacter? {
        playerCharacters.forEach { character ->
            if (character.id == id) return character
        }
        return null
    }

    fun getAvailableEquipment(slot: EquipmentSlot): ArrayList<Pair<Equipment, Int>> {
        val a = ArrayList<Pair<Equipment, Int>>()
        playerEquipment.forEach { id, amount ->
            val equipment = Equipment.getEquipment(id)
            if (equipment?.slot == slot) {
                a.add(Pair(equipment, amount))
            }
        }
        return a
    }
    fun hasEquipment(id: Int): Boolean {
        return playerEquipment.contains(id) && playerEquipment[id]!! > 0
    }

    fun getAmountOfEquipment(id: Int): Int {
        return if (hasEquipment(id)) playerEquipment[id]!! else 0
    }

    fun usePieceOfEquipment(id: Int) {
        if (hasEquipment(id)) {
            playerEquipment[id] = playerEquipment[id]!! - 1
        }
    }

    fun addPieceOfEquipment(id: Int) {
        playerEquipment[id] = if (hasEquipment(id)) {
            playerEquipment[id]!! + 1
        } else {
            1
        }
    }
}