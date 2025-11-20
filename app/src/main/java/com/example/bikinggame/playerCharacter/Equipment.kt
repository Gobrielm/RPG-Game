package com.example.bikinggame.playerCharacter

import androidx.lifecycle.lifecycleScope
import com.example.bikinggame.homepage.getUserJson
import com.example.bikinggame.homepage.makeGetRequest
import com.example.bikinggame.homepage.makePostRequest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

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
    var attack: Attack? = null
    var shield: Shield? = null

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
            jsonArray.put(attack!!.id)
        }
        if (shield == null) {
            jsonArray.put(null)
        } else {
            jsonArray.put(shield!!.id)
        }
    }
}

suspend fun createEquipment(id: Int): Equipment? {
    val userData: JSONObject? = getUserJson()
    if (userData == null) return null

    val equipmentJSON = makeGetRequest("https://bikinggamebackend.vercel.app/api/equipment/${id}", userData.get("token") as String)

    val equipment = Equipment(equipmentJSON, IntWrapper(0))

    val attackID = equipmentJSON.get(equipmentJSON.length() - 2) as Int
    val shieldID = equipmentJSON.get(equipmentJSON.length() - 1) as Int

    if (attackID != -1) {
        val attackJSON = makeGetRequest("https://bikinggamebackend.vercel.app/api/attacks/${attackID}", userData.get("token") as String)
        val attack = Attack(attackJSON, IntWrapper(0))
        equipment.attack = attack
    }

    if (shieldID != -1) {
        val shieldJSON = makeGetRequest("https://bikinggamebackend.vercel.app/api/shields/${attackID}", userData.get("token") as String)
        val shield = Shield(shieldJSON, IntWrapper(0))
        equipment.shield = shield
    }

   return equipment
}