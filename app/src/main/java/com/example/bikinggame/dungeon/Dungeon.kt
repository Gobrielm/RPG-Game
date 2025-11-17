package com.example.bikinggame.dungeon

import org.json.JSONArray

class Dungeon {
    val id: Int

    constructor(pId: Int) {
        id = pId
    }

    constructor(jsonArray: JSONArray) {
        id = jsonArray.get(0) as Int
    }

    fun serialize(): JSONArray {
        val jsonArray = JSONArray()
        jsonArray.put(id)
        return jsonArray
    }

}