package com.example.bikinggame.homepage.inventory

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.key
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bikinggame.R
import com.example.bikinggame.databinding.FragmentInventoryBinding
import com.example.bikinggame.dungeonPrep.DungeonPrepViewModel
import com.example.bikinggame.homepage.HomePageViewModel
import com.example.bikinggame.homepage.inventory.PlayerInventory.playerCharacters
import com.example.bikinggame.homepage.inventory.PlayerInventory.playerEquipment
import com.example.bikinggame.playerCharacter.Equipment
import com.example.bikinggame.playerCharacter.EquipmentSlot
import com.example.bikinggame.playerCharacter.PlayerCharacter
import com.example.bikinggame.requests.getUserJson
import com.example.bikinggame.requests.makeGetRequest
import com.example.bikinggame.requests.makePutRequest
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.LinkedList
import kotlin.getValue

class InventoryFragment() : Fragment() {
    enum class InventoryMode { VIEW, PICK }

    var mode: InventoryMode = InventoryMode.VIEW

    private var _binding: FragmentInventoryBinding? = null
    private val viewModel: HomePageViewModel by activityViewModels()

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

        if (inventoryList.isEmpty()) {
            loadPlayerCharactersReq()
            loadPlayerEquipment()
        }

    }

    fun refreshInventoryScreen() {
        inventoryList.clear()
        playerCharacters.forEach { playerCharacter ->
            inventoryList.add(Item(R.drawable.truck, playerCharacter.toString()))
        }
        requireActivity().runOnUiThread {
            recyclerView.adapter = InventoryManager(inventoryList, ::playerCharacterClicked)
        }
    }

    fun playerCharacterClicked(position: Int) {
        if (mode == InventoryMode.VIEW) {
            editCharacter(position)
        } else {
            selectCharacter(position)
        }
    }

    fun editCharacter(position: Int) {
        viewModel.selectCharacter(position)
    }

    fun selectCharacter(position: Int) {
        val viewModel: DungeonPrepViewModel by activityViewModels()
        viewModel.selectCharacter(playerCharacters[position])
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

    fun tempGiveEquipment() {
        if (user == null) return
        lifecycleScope.launch {
            val userData = getUserJson()
            if (userData == null) return@launch
            val body = "1".toRequestBody("application/json".toMediaTypeOrNull())
            makePutRequest("https://bikinggamebackend.vercel.app/api/equipment/1",
                userData.get("token") as String,
                body
            )
        }
    }

    fun loadPlayerEquipment() {
        if (user == null) return
        lifecycleScope.launch {
            val userData = getUserJson()
            if (userData == null) return@launch
            val res = makeGetRequest("https://bikinggamebackend.vercel.app/api/equipment/",
                userData.get("token") as String
            )

            PlayerInventory.updatePlayerEquipment(res)
        }
    }

    fun loadPlayerCharactersRes(json: JSONObject) {
        lifecycleScope.launch {
            val localList: ArrayList<PlayerCharacter> = ArrayList()
            val playerCharacterJSON = json.get("characters") as JSONObject

            for (section: String in (playerCharacterJSON).keys()) {
                try {
                    val playerCharacterArray = playerCharacterJSON.get(section) as JSONArray
                    val playerCharacter = PlayerCharacter(playerCharacterArray)
                    localList.add(playerCharacter)
                } catch (e: Exception) {
                    Log.d("LoadPlayerCharacters", e.toString())
                    return@launch
                }
            }

//        savePlayerCharactersLocally(localList)
            playerCharacters.clear()
            playerCharacters.addAll(localList)
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