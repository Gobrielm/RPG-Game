package com.example.bikinggame.homepage.inventory

import android.content.Context
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
import com.example.bikinggame.homepage.HomePage
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

        if (inventoryList.isEmpty()) {
            loadPlayerCharactersLocally()
            loadPlayerCharactersReq()
            loadPlayerEquipment()
        }

        binding.newCharacterButton.setOnClickListener {
            (requireContext() as HomePage).openCharacterCreator()
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

    fun loadPlayerEquipment() {
        if (user == null) return
        lifecycleScope.launch {
            val userData = getUserJson()
            if (userData == null) return@launch
            val res = makeGetRequest("https://bikinggamebackend.vercel.app/api/equipment/",
                userData.get("token") as String
            )

            if (!res.has("data")) {
                Log.e("InventoryFragment", "Missing 'data' in response: $res")
                return@launch
            }

            try {
                val data = res.get("data") as JSONObject

                PlayerInventory.updatePlayerEquipment(data)
            } catch (error: Exception) {
                Log.d("InventoryFragment", error.toString())
            }
        }
    }

    fun loadPlayerCharactersRes(json: JSONObject) {
        lifecycleScope.launch {
            val localList: ArrayList<PlayerCharacter> = ArrayList()

            if (!json.has("data")) {
                Log.e("InventoryFragment", "Missing 'data' in response: $json")
                return@launch
            }

            val playerCharacterJSON = json.get("data") as JSONObject

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

            playerCharacters.clear()
            playerCharacters.addAll(localList)
            savePlayerCharactersLocally()
            refreshInventoryScreen()
        }
    }

    fun loadPlayerCharactersLocally() {
        val filename = "characters_data"
        val localCharacters: ArrayList<PlayerCharacter> = ArrayList()
        try {
            requireContext().openFileInput(filename).bufferedReader().useLines { lines ->
                for (line in lines) {
                    val jsonArray = JSONArray(line) // parse the String into a JSONArray
                    localCharacters.add(PlayerCharacter(jsonArray))
                }
            }

        } catch (err: Exception) {
            Log.d("PlayerCharacterStorage", err.toString())
        }
        playerCharacters.clear()
        playerCharacters.addAll(localCharacters)
        refreshInventoryScreen()
    }

    fun savePlayerCharactersLocally() {
        val filename = "characters_data"
        var data = ""
        for (playerCharacter in playerCharacters) {
            data += playerCharacter.serialize().toString() + '\n'
        }

        try {
            requireContext().openFileOutput(filename, Context.MODE_PRIVATE).use {
                it.write(data.toByteArray())
            }

        } catch (err: Exception) {
            Log.d("PlayerCharacterStorage", err.toString())
        }
    }
}