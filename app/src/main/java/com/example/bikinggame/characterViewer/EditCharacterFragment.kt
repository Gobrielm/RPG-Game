package com.example.bikinggame.characterViewer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bikinggame.R
import com.example.bikinggame.databinding.FragmentEditCharacterBinding
import com.example.bikinggame.homepage.inventory.InventoryManager
import com.example.bikinggame.homepage.inventory.Item
import com.example.bikinggame.homepage.inventory.PlayerInventory.playerCharacters
import com.example.bikinggame.playerCharacter.Attack
import com.example.bikinggame.requests.getUserJson
import com.example.bikinggame.requests.makePostRequest
import com.example.bikinggame.requests.makePutRequest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.LinkedList
import kotlin.arrayOf

class EditCharacterFragment: Fragment() {

    private var _binding: FragmentEditCharacterBinding? = null

    private val binding get() = _binding!!

    private val viewModel: CharacterViewerViewModel by activityViewModels()
    private var attacksToChooseFrom: ArrayList<Attack> = arrayListOf()
    private var attackSlot: Int = -1
    private val inventoryList: LinkedList<Item> = LinkedList<Item>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditCharacterBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.attackList.layoutManager = LinearLayoutManager(context)
        binding.attackList.adapter = InventoryManager(inventoryList, ::selectedAttackID)

        if (viewModel.getSelectedCharacterID() != null) {
            updateInfo()
        } else {
            viewModel.selectedCharacterID.observe(viewLifecycleOwner, Observer {
                updateInfo()
            })
        }
        val buttons = arrayOf(binding.mv1Button, binding.mv2Button, binding.mv3Button, binding.mv4Button)
        for (i in 0 until 4) {
            buttons[i].setOnClickListener { showAttackChooser(i) }
        }

        binding.equipmentButton.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.editEquipmentFragment)
        }

        binding.skillsButton.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.skillTreeFragment)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateInfo() {
        val characterID = viewModel.getSelectedCharacterID()!!
        val character = playerCharacters[characterID]
        binding.statsPreview.text = character.toString()

        val buttons = arrayOf(binding.mv1Button, binding.mv2Button, binding.mv3Button, binding.mv4Button)
        var ind = 0
        character.attacks.forEach { attack ->
            buttons[ind++].text = attack?.name ?: "None Chosen"
        }
    }

    fun showAttackChooser(pAttackSlot: Int) {
        Log.d("AAA", "AAA")
        if (attackSlot != pAttackSlot) {
            Log.d("BBB", "BBB")
            unShowAttackChooser()
            attacksToChooseFrom.clear()
            inventoryList.clear()
            attackSlot = -1
        }

        attackSlot = pAttackSlot
        val characterID = viewModel.getSelectedCharacterID()!!
        val character = playerCharacters[characterID]
        binding.equipmentButton.visibility = View.GONE
        binding.skillsButton.visibility = View.GONE
        binding.attackList.visibility = View.VISIBLE

        attacksToChooseFrom = character.getAvailableAttacks(attackSlot)

        inventoryList.clear()
        attacksToChooseFrom.forEach { attack ->
            inventoryList.add(Item(R.drawable.truck, attack.toString()))
        }
    }

    fun selectedAttackID(ind: Int) {
        val characterID = viewModel.getSelectedCharacterID()!!
        val character = playerCharacters[characterID]

        if (attackSlot < 0 || attackSlot > 3) {
            Log.d("Selecting Attack", "Invalid Attack Slot")
        } else {
            val characterAttacks = character.attacks
            val idToRemove = attacksToChooseFrom[ind].id
            for (slot in 0 until 4) {
                if (characterAttacks[slot] != null && characterAttacks[slot]!!.id == idToRemove) {
                    characterAttacks[slot] = null
                }
            }
            characterAttacks[attackSlot] = attacksToChooseFrom[ind]

        }

        attacksToChooseFrom.clear()
        attackSlot = -1

        updateInfo()
        unShowAttackChooser()
        lifecycleScope.launch {
            val userData: JSONObject? = getUserJson()
            if (userData == null) return@launch
            val characterJSON = character.serialize()

            val body = characterJSON.toString().toRequestBody("application/json".toMediaTypeOrNull())
            makePutRequest(
                "https://bikinggamebackend.vercel.app/api/characters/$characterID",
                userData.get("token") as String,
                body
            )
        }

    }

    fun unShowAttackChooser() {
        binding.equipmentButton.visibility = View.VISIBLE
        binding.skillsButton.visibility = View.VISIBLE
        binding.attackList.visibility = View.GONE
    }
}