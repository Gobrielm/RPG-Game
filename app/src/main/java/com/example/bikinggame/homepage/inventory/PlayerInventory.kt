package com.example.bikinggame.homepage.inventory

import com.example.bikinggame.playerCharacter.Equipment
import com.example.bikinggame.playerCharacter.EquipmentSlot
import com.example.bikinggame.playerCharacter.PlayerCharacter
import org.json.JSONObject
import kotlin.collections.forEach
import kotlin.collections.set

object PlayerInventory {
    val playerCharacters: ArrayList<PlayerCharacter> = ArrayList()
    val playerEquipment: MutableMap<Int, Int> = mutableMapOf()
    val usedPlayerEquipment: MutableMap<Int, Int> = mutableMapOf()

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
            if (equipment?.slot == slot && hasEquipment(equipment.id)) {
                val actualAmount = getAmountOfEquipment(equipment.id)
                a.add(Pair(equipment, actualAmount))
            }
        }
        return a
    }
    fun hasEquipment(id: Int): Boolean {
        if (playerEquipment.contains(id)) {
            return playerEquipment[id]!! - usedPlayerEquipment[id]!! > 0
        }
        return false
    }

    fun getAmountOfEquipment(id: Int): Int {
        return if (hasEquipment(id)) (playerEquipment[id]!! - usedPlayerEquipment[id]!!) else 0
    }

    fun usePieceOfEquipment(id: Int) {
        if (hasEquipment(id)) {
            usedPlayerEquipment[id] = usedPlayerEquipment[id]!! + 1
        }
    }

    fun addPieceOfEquipment(id: Int) {
        if (usedPlayerEquipment[id] != 0) {
            usedPlayerEquipment[id] = usedPlayerEquipment[id]!! - 1
        }
    }

    fun updateUsedEquipment(id: Int) {
        val equipment = Equipment.getEquipment(id)
        if (equipment == null || id == -1) return
        val slot = equipment.slot
        for (character in playerCharacters) {
            if ((character.getEquipment(slot)?.id ?: -1) == id) {
                usePieceOfEquipment(id)
            }
        }
    }

    fun updatePlayerEquipment(jsonObject: JSONObject) {
        jsonObject.keys().forEach { key ->
            val equipmentID = key.toInt()
            playerEquipment[equipmentID] = jsonObject[key] as Int
            usedPlayerEquipment[equipmentID] = 0
            updateUsedEquipment(equipmentID)
        }
    }
}