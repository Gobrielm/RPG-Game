package com.mainApp.rpg.homepage.inventory

import com.mainApp.rpg.playerCharacter.Equipment
import com.mainApp.rpg.playerCharacter.EquipmentSlot
import com.mainApp.rpg.playerCharacter.PlayerCharacter
import org.json.JSONObject
import kotlin.collections.forEach
import kotlin.collections.set

object PlayerInventory {
    val playerCharacters: MutableMap<Int, PlayerCharacter> = mutableMapOf<Int, PlayerCharacter>()
    val playerEquipment: MutableMap<Int, Int> = mutableMapOf()
    val usedPlayerEquipment: MutableMap<Int, Int> = mutableMapOf()
    private var coins: Int = 0

    fun addCharacter(character: PlayerCharacter) {
        playerCharacters[character.id] = character
    }
    fun getCharacter(id: Int): PlayerCharacter? {
        playerCharacters.forEach { (idToCheck, character) ->
            if (id == idToCheck) return character
        }
        return null
    }

    fun deleteCharacter(idToRemove: Int) {
        playerCharacters.forEach { (id, _) ->
            if (idToRemove == id) {
                playerCharacters.remove(id)
                return
            }
        }
    }

    fun setCoins(newAmt: Int) {
        coins = newAmt
    }

    fun getCoins(): Int {
        return coins
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

    fun unUsePieceOfEquipment(id: Int) {
        if (usedPlayerEquipment[id] != 0) {
            usedPlayerEquipment[id] = usedPlayerEquipment[id]!! - 1
        }
    }

    fun addPieceOfEquipment(id: Int) {
        addEquipment(id, 1)
    }

    fun addEquipment(id: Int, amt: Int) {
        if (!playerEquipment.contains(id)) {
            playerEquipment[id] = 0
            usedPlayerEquipment[id] = 0
        }
        playerEquipment[id] = playerEquipment[id]!! + amt;
    }

    fun updateUsedEquipment(id: Int) {
        val equipment = Equipment.getEquipment(id)
        if (equipment == null || id == -1) return
        val slot = equipment.slot
        for ((_, character) in playerCharacters) {
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