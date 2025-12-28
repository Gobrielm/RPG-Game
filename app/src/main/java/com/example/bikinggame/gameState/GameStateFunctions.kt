package com.example.bikinggame.gameState

import android.content.Context
import android.util.Log
import com.example.bikinggame.dungeonPrep.deepestRoomAllowed
import com.example.bikinggame.homepage.inventory.PlayerInventory
import com.example.bikinggame.homepage.inventory.PlayerInventory.playerCharacters
import com.example.bikinggame.playerCharacter.PlayerCharacter
import com.example.bikinggame.requests.getUserJson
import com.example.bikinggame.requests.makeGetRequest
import com.example.bikinggame.requests.makePutRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

const val TAG = "GameStateFunctions"

fun saveLocally(context: Context) {
    Log.d(TAG, "Saving Locally")
    savePlayerCharactersLocally(context)
    savePlayerEquipmentLocally(context)
    savePointsLocally(context)
    saveTimeStampLocally(context)
    saveDeepestRoomLocally(context)
}

suspend fun saveToCloud() {
    Log.d(TAG, "Saving To Cloud")
    playerCharacters.forEach { (id, _) ->
        saveCharacter(id)
    }
    PlayerInventory.playerEquipment.forEach { (id, _) ->
        saveEquipmentNumbers(id)
    }
    savePoints()
    saveTimeStamp()
}

suspend fun saveCharacter(characterID: Int) {
    val userData: JSONObject? = getUserJson()
    if (userData == null) return
    val character = PlayerInventory.getCharacter(characterID)
    val characterJSON = character!!.serialize()

    val jsonObject = JSONObject()
    jsonObject.put("characterInfo", characterJSON)

    val body = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
    makePutRequest(
        "https://bikinggamebackend.vercel.app/api/characters/$characterID",
        userData.get("token") as String,
        body
    )

    saveTimeStamp()
}

suspend fun saveEquipmentNumbers(equipmentID: Int) {
    val userData: JSONObject? = getUserJson()
    if (userData == null) return
    val quantity = PlayerInventory.getAmountOfEquipment(equipmentID)
    if (quantity <= 0) {
        Log.d("Inventory Functions", "Invalid number of Equipment")
        return
    }
    val jsonObject = JSONObject()
    jsonObject.put("quantity", quantity)

    val body = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
    makePutRequest(
        "https://bikinggamebackend.vercel.app/api/equipment/$equipmentID",
        userData.get("token") as String,
        body
    )

    saveTimeStamp()
}

suspend fun savePoints() {
    val userData: JSONObject? = getUserJson()
    if (userData == null) return
    val amount = PlayerInventory.getCoins()

    val jsonObject = JSONObject()
    jsonObject.put("points", amount)

    val body = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
    makePutRequest(
        "https://bikinggamebackend.vercel.app/api/points",
        userData.get("token") as String,
        body
    )

    saveTimeStamp()
}

suspend fun saveTimeStamp() {
    val calendar = Calendar.getInstance()

    val userData = getUserJson()
    if (userData == null) return

    val dateArray = arrayOf(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DATE),
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE)
    )

    val jsonObject = JSONObject()
    jsonObject.put("timestamp", JSONArray(dateArray))

    val body = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

    makePutRequest(
        "https://bikinggamebackend.vercel.app/api/timestamps/",
        userData.get("token") as String,
        body
    )
}

fun saveTimeStampLocally(context: Context) {
    val filename = "timestamp"
    val calendar = Calendar.getInstance()

    val data = listOf(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH),
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE)
    ).joinToString(",")

    try {
        context.openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(data.toByteArray())
        }
    } catch (err: Exception) {
        Log.d("$TAG: saveTimeStampLocally", err.toString())
    }
}

fun saveDeepestRoomLocally(context: Context) {
    val filename = "deepest_room"
    val data = deepestRoomAllowed.deepestRoom.toString()

    try {
        context.openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(data.toByteArray())
        }
        saveTimeStampLocally(context)
    } catch (err: Exception) {
        Log.d("$TAG: saveDeepestRoomLocally", err.toString())
    }
}

fun savePlayerEquipmentLocally(context: Context) {
    val filename = "equipment_data"
    val jsonArray = JSONArray()

    for ((equipmentId, amount) in PlayerInventory.playerEquipment) {
        val pair = JSONArray()
        pair.put(equipmentId)
        pair.put(amount)
        jsonArray.put(pair)
    }

    try {
        context.openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(jsonArray.toString().toByteArray())
        }
        saveTimeStampLocally(context)
    } catch (err: Exception) {
        Log.d("$TAG: savePlayerEquipmentLocally", err.toString())
    }
}

fun savePlayerCharactersLocally(context: Context) {
    val filename = "characters_data"
    var data = ""
    for ((_, playerCharacter) in playerCharacters) {
        data += playerCharacter.serialize().toString() + '\n'
    }

    try {
        context.openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(data.toByteArray())
        }
        saveTimeStampLocally(context)
    } catch (err: Exception) {
        Log.d("$TAG: savePlayerCharactersLocally", err.toString())
    }
}

fun savePointsLocally(context: Context) {
    val filename = "user_data"
    val points = PlayerInventory.getCoins().toString()
    try {
        context.openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(points.toByteArray())
        }
        saveTimeStampLocally(context)
    } catch (err: Exception) {
        Log.d("$TAG: savePointsLocally", err.toString())
    }
}

/**
 * Gets a save from server if valid else pulls locally
 */
suspend fun loadGameState(context: Context) {
    val localTimeStamp = loadLocalTimeStamp(context)
    val serverTimeStamp = loadServerTimeStamp()

    // Local is newer
    if (localTimeStamp > serverTimeStamp) {
        loadLocalGameState(context)
    } else {
        // server is newer
        loadServerGameState(context)
    }
}

fun loadLocalTimeStamp(context: Context): Calendar {
    val filename = "timestamp"
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, 0)
    try {
        context.openFileInput(filename).bufferedReader().use { reader ->
            val parts = reader.readLine().split(",")

            if (parts.size == 5) {
                calendar.set(
                    parts[0].toInt(), // year
                    parts[1].toInt(), // month (0-based)
                    parts[2].toInt(), // day
                    parts[3].toInt(), // hour
                    parts[4].toInt()  // minute
                )
            }
        }
    } catch (err: Exception) {
        Log.d(TAG, "No timestamp found, using current time")
        saveTimeStampLocally(context)
    }

    return calendar
}

suspend fun loadServerTimeStamp(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, 0)

    val userData = getUserJson()
    if (userData == null) return calendar

    val res = makeGetRequest(
        "https://bikinggamebackend.vercel.app/api/timestamps/",
        userData.get("token") as String
    )
    if (!res.has("data")) {
        saveTimeStamp() // Save current timestamp
        return calendar
    }

    val date = res.get("data") as JSONArray

    calendar.set(
        date[0] as Int, // year
        date[1] as Int, // month (0-based)
        date[2] as Int, // day
        date[3] as Int, // hour
        date[4] as Int  // minute
    )
    return calendar
}

suspend fun loadLocalGameState(context: Context) {
    loadPlayerCharactersLocally(context)
    loadDeepestRoomLocally(context)
    loadPlayerEquipmentLocally(context)
    loadPointsLocally(context)

    playerCharacters.forEach { (id, _) ->
        saveCharacter(id)
    }
    PlayerInventory.playerEquipment.forEach { (id, _) ->
        saveEquipmentNumbers(id)
    }
    savePoints()
    saveTimeStamp()
}

fun loadPlayerCharactersLocally(context: Context) {
    val filename = "characters_data"
    val localCharacters: ArrayList<PlayerCharacter> = ArrayList()
    try {
        context.openFileInput(filename).bufferedReader().useLines { lines ->
            for (line in lines) {
                val jsonArray = JSONArray(line) // parse the String into a JSONArray
                localCharacters.add(PlayerCharacter(jsonArray))
            }
        }

    } catch (err: Exception) {
        Log.d("PlayerCharacterStorage", err.toString())
    }
    playerCharacters.clear()
    localCharacters.forEach { playerCharacter ->
        PlayerInventory.addCharacter(playerCharacter)
    }
}

fun loadPlayerEquipmentLocally(context: Context) {
    val filename = "equipment_data"

    try {
        val jsonString = context.openFileInput(filename)
            .bufferedReader()
            .use { it.readText() }

        val jsonArray = JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val pair = jsonArray.getJSONArray(i)
            val equipmentID = pair.getInt(0)
            val amount = pair.getInt(1)

            PlayerInventory.addEquipment(equipmentID, amount)
            PlayerInventory.updateUsedEquipment(equipmentID)
        }

    } catch (err: Exception) {
        Log.d("PlayerCharacterStorage", "No equipment data found")
    }
}

fun loadDeepestRoomLocally(context: Context) {
    val filename = "deepest_room"
    try {
        context.openFileInput(filename).bufferedReader().useLines { lines ->
            for (line in lines) {
                deepestRoomAllowed.deepestRoom = line.toInt()
            }
        }

    } catch (err: Exception) {
        Log.d("SelectDungeonFragment", err.toString())
        saveDeepestRoomLocally(context) // Just save a 0 to avoid future errors
    }
}

fun loadPointsLocally(context: Context) {
    val filename = "user_data"
    var points = "0"
    try {
        context.openFileInput(filename).bufferedReader().useLines { lines ->
            points = (lines.elementAt(0) as String)
        }

    } catch (err: Exception) {
        Log.d("PointsStorage", err.toString())
    }
    PlayerInventory.setCoins(points.toInt())
}

suspend fun loadServerGameState(context: Context) {
    loadPlayerCharacters()
    loadPlayerEquipment()
    loadPoints()

    savePlayerCharactersLocally(context)
    savePlayerEquipmentLocally(context)
    savePointsLocally(context)
    saveTimeStampLocally(context)
}

suspend fun loadPlayerEquipment() {
    val userData = getUserJson()
    if (userData == null) return
    val res = makeGetRequest("https://bikinggamebackend.vercel.app/api/equipment/",
        userData.get("token") as String
    )

    if (!res.has("data")) {
        Log.e("$TAG: loadPlayerEquipment", "Missing 'data' in response: $res")
        return
    }

    try {
        val data = res.get("data") as JSONObject
        PlayerInventory.updatePlayerEquipment(data)
    } catch (error: Exception) {
        Log.d("$TAG: loadPlayerEquipment", error.toString())
    }
}

suspend fun loadPlayerCharacters() {
    val userData = getUserJson()
    if (userData == null) return

    val res = makeGetRequest(
        "https://bikinggamebackend.vercel.app/api/characters/",
        userData.get("token") as String
    )

    if (!res.has("data")) {
        Log.e("$TAG: loadPlayerCharacters", "Missing 'data' in response: $res")
        return
    }

    val playerCharacterJSON = res.get("data") as JSONObject

    val localList: ArrayList<PlayerCharacter> = ArrayList()

    for (section: String in (playerCharacterJSON).keys()) {
        try {
            val playerCharacterArray = playerCharacterJSON.get(section) as JSONArray
            val playerCharacter = PlayerCharacter(playerCharacterArray)
            localList.add(playerCharacter)
        } catch (e: Exception) {
            Log.d("$TAG: loadPlayerCharacters", e.toString())
            return
        }
    }

    playerCharacters.clear()
    localList.forEach { playerCharacter ->
        PlayerInventory.addCharacter(playerCharacter)
    }
}

suspend fun loadPoints() {
    val userData = getUserJson()
    if (userData == null) return
    val res = makeGetRequest("https://bikinggamebackend.vercel.app/api/points", userData.get("token") as String)
    if (!res.has("data")) {
        Log.e("$TAG: loadPoints", "No data in message")
        return
    }
    val points: Int = res.get("data") as Int
    PlayerInventory.setCoins(points)
}