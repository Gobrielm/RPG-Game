package com.example.bikinggame.playerCharacter

import android.util.Log
import org.json.JSONArray


private const val TAG: String = "CharacterClass"
enum class CharacterMainClass(val ind: Int) {
    NONE(0),
    MELEE(1),
    RANGED(2),
    MAGIC(3);

    companion object {
        fun fromInt(value: Int): CharacterMainClass? {
            return CharacterMainClass.entries.find { it.ind == value }
        }
    }
}

enum class CharacterSubClass(val ind: Int) {
    Filler(0),
    TraditionalMagic(1),
    RitualMagic(2),
    Knight(3),
    North(4),
    TraditionalRanged(5),
    NonTraditionalRanged(6);

    companion object {
        fun fromInt(value: Int): CharacterSubClass? {
            return CharacterSubClass.entries.find { it.ind == value }
        }
    }
}

class CharacterClass {
    var mainClass: CharacterMainClass = CharacterMainClass.NONE
    var subClass: CharacterSubClass = CharacterSubClass.Filler

    constructor() {}

    constructor(p_mainClass: CharacterMainClass, p_subClass: CharacterSubClass) {
        mainClass = p_mainClass
        subClass = p_subClass
    }

    constructor(jsonArray: JSONArray, offset: IntWrapper) {
        try {
            mainClass = CharacterMainClass.fromInt(jsonArray[offset.value++] as Int)!!
            subClass = CharacterSubClass.fromInt(jsonArray[offset.value++] as Int)!!
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }

    fun serialize(jsonArray: JSONArray) {
        jsonArray.put(mainClass.ind)
        jsonArray.put(subClass.ind)
    }

    override fun toString(): String {
        return "$mainClass-$subClass"
    }
}