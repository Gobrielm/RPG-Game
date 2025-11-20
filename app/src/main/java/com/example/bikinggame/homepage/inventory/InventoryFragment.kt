package com.example.bikinggame.homepage.inventory

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bikinggame.R
import com.example.bikinggame.dungeonPrep.DungeonPrepViewModel
import com.example.bikinggame.homepage.getUserJson
import com.example.bikinggame.homepage.getUserToken
import com.example.bikinggame.homepage.inventory.playerInventory.characters
import com.example.bikinggame.homepage.makeGetRequest
import com.example.bikinggame.homepage.makeRequest
import com.example.bikinggame.playerCharacter.PlayerCharacter
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.LinkedList
import kotlin.getValue

object playerInventory {
    val characters: ArrayList<PlayerCharacter> = ArrayList()

    fun getCharacter(id: Int): PlayerCharacter? {
        characters.forEach { character ->
            if (character.id == id) return character
        }
        return null
    }
}

class InventoryFragment() : Fragment() {
    enum class InventoryMode { VIEW, PICK }

    var mode: InventoryMode = InventoryMode.VIEW

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inventory, container, false)
    }

    private val user = Firebase.auth.currentUser
    var inventoryList: LinkedList<Item> = LinkedList<Item>()
    lateinit var recyclerView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val status = arguments?.getBoolean("PICK")
        if (status != null && status) mode = InventoryMode.PICK

        recyclerView =  view.findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(context)

        recyclerView.adapter = InventoryManager(inventoryList, ::playerCharacterClicked)
//        loadPlayerCharactersLocally()
        loadPlayerCharactersReq()
    }

    fun refreshInventoryScreen() {
        inventoryList.clear()
        characters.forEach { playerCharacter ->
            Log.d("TTT", "HHHHH")
            inventoryList.add(Item(R.drawable.truck, playerCharacter.toString()))
        }
        requireActivity().runOnUiThread {
            recyclerView.adapter = InventoryManager(inventoryList, ::playerCharacterClicked)
        }
    }

    fun playerCharacterClicked(position: Int) {
        if (mode == InventoryMode.VIEW) {
            showItemDetails(position)
        } else {
            returnSelectedItem(position)
        }
    }

    fun showItemDetails(position: Int) {
        Log.d("showItemDetails", position.toString())
    }

    fun returnSelectedItem(position: Int) {
        val viewModel: DungeonPrepViewModel by activityViewModels()
        viewModel.selectCharacter(characters[position])
    }

    fun loadPlayerCharactersReq() {
        if (user == null) return
        lifecycleScope.launch {
            val userData = getUserJson()
            if (userData == null) return@launch

            makeGetRequest(
                "https://bikinggamebackend.vercel.app/api/characters/",
                userData.get("token") as String,
                ::loadPlayerCharactersRes
            )
        }
    }

    fun loadPlayerCharactersRes(json: JSONObject) {
        lifecycleScope.launch {
            val localList: ArrayList<PlayerCharacter> = ArrayList()
            val playerCharacterJSON = json.get("characters") as JSONObject

            for (section: String in (playerCharacterJSON).keys()) {
                try {
                    val playerCharacterArray = playerCharacterJSON.get(section) as JSONArray
                    val playerCharacter = PlayerCharacter.createCharacter(playerCharacterArray)
                    localList.add(playerCharacter)
                } catch (e: Exception) {
                    Log.d("LoadPlayerCharacters", e.toString())
                    return@launch
                }
            }

//        savePlayerCharactersLocally(localList)
            characters.clear()
            characters.addAll(localList)
            refreshInventoryScreen()
        }
    }

//    fun loadPlayerCharactersLocally() {
//        val filename = "characters_data"
//        var playerCharacters: ArrayList<PlayerCharacter> = ArrayList()
//        try {
//            requireContext().openFileInput(filename).bufferedReader().useLines { lines ->
//                for (line in lines) {
//                    val jsonArray = JSONArray(line) // parse the String into a JSONArray
//                    playerCharacters.add(PlayerCharacter(jsonArray))
//                }
//            }
//
//        } catch (err: Exception) {
//            Log.d("PlayerCharacterStorage", err.toString())
//        }
//        characters.clear()
//        characters.addAll(playerCharacters)
//        refreshInventoryScreen()
//    }
//
//    fun savePlayerCharactersLocally(playerCharacters: ArrayList<PlayerCharacter>) {
//        val filename = "characters_data"
//        var data = ""
//        for (playerCharacter in playerCharacters) {
//            data += playerCharacter.serialize().toString() + '\n'
//        }
//
//        try {
//            requireContext().openFileOutput(filename, Context.MODE_PRIVATE).use {
//                it.write(data.toByteArray())
//            }
//
//        } catch (err: Exception) {
//            Log.d("PlayerCharacterStorage", err.toString())
//        }
//    }
}