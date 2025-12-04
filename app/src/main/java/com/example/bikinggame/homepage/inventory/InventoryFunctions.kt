package com.example.bikinggame.homepage.inventory

import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.bikinggame.requests.getUserJson
import com.example.bikinggame.requests.makePutRequest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


// TODO: Some weird stuff with either "text/plain".toMediaType() or "application/json".toMediaTypeOrNull()
suspend fun saveCharacter(characterID: Int) {
    val userData: JSONObject? = getUserJson()
    if (userData == null) return
    val character = PlayerInventory.getCharacter(characterID)
    val characterJSON = character!!.serialize()

    val body = characterJSON.toString().toRequestBody("application/json".toMediaTypeOrNull())
    makePutRequest(
        "https://bikinggamebackend.vercel.app/api/characters/$characterID",
        userData.get("token") as String,
        body
    )
}

suspend fun updateEquipmentCount(equipmentID: Int) {
    val userData: JSONObject? = getUserJson()
    if (userData == null) return
    val amount = PlayerInventory.getAmountOfEquipment(equipmentID)
    if (amount < 0) {
        Log.d("Inventory Functions", "Invalid number of Equipment")
        return
    }

    val body = amount.toString().toRequestBody("text/plain".toMediaType())
    makePutRequest(
        "https://bikinggamebackend.vercel.app/api/equipment/$equipmentID",
        userData.get("token") as String,
        body
    )
}

suspend fun savePoints() {
    val userData: JSONObject? = getUserJson()
    if (userData == null) return
    val amount = PlayerInventory.getCoins()
    val body = amount.toString().toRequestBody("text/plain".toMediaType())
    makePutRequest(
        "https://bikinggamebackend.vercel.app/api/points",
        userData.get("token") as String,
        body
    )
}