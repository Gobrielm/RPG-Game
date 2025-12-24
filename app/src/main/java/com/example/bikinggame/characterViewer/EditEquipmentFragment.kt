package com.example.bikinggame.characterViewer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bikinggame.R
import com.example.bikinggame.databinding.FragmentEditEquipmentBinding
import com.example.bikinggame.homepage.inventory.InventoryManager
import com.example.bikinggame.homepage.inventory.Item
import com.example.bikinggame.homepage.inventory.PlayerInventory
import com.example.bikinggame.homepage.inventory.saveCharacter
import com.example.bikinggame.playerCharacter.Equipment
import com.example.bikinggame.playerCharacter.EquipmentSlot
import com.example.bikinggame.playerCharacter.PlayerCharacter
import com.example.bikinggame.requests.getUserJson
import com.example.bikinggame.requests.makePutRequest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.LinkedList
import kotlin.getValue

class EditEquipmentFragment: Fragment() {

    private var _binding: FragmentEditEquipmentBinding? = null

    private val binding get() = _binding!!

    private val viewModel: CharacterViewerViewModel by activityViewModels()
    private var slotOpen: EquipmentSlot? = null

    private var equipmentToChooseFrom = ArrayList<Pair<Equipment, Int>>()
    private val inventoryList: LinkedList<Item> = LinkedList<Item>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditEquipmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.equipmentList.layoutManager = LinearLayoutManager(context)

        binding.equipmentList.adapter = InventoryManager(inventoryList, ::selectedEquipment) { holder, item ->
            holder.imageButton.setImageResource(item.imageResId)
            holder.imageButton.scaleType = ImageView.ScaleType.CENTER_CROP
            holder.text.text = item.text
        }

        binding.backButton.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.editCharacterFragment)
        }

        binding.closeListButton.setOnClickListener {
            closeEquipmentList()
        }

        val map = mapOf(
            EquipmentSlot.HEAD to binding.helmetButton, EquipmentSlot.NECK to binding.neckButton,
            EquipmentSlot.TORSO to binding.chestButton, EquipmentSlot.MAIN_HAND to binding.mainHandButton,
            EquipmentSlot.OFF_HAND to binding.offHandButton, EquipmentSlot.RING to binding.ringButton
        )

        map.forEach { slot, button ->
            button.setOnClickListener { openEquipmentList(slot) }
        }

        updateView()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateView() {
        val map = mapOf(
            EquipmentSlot.HEAD to binding.helmetButton, EquipmentSlot.NECK to binding.neckButton,
            EquipmentSlot.TORSO to binding.chestButton, EquipmentSlot.MAIN_HAND to binding.mainHandButton,
            EquipmentSlot.OFF_HAND to binding.offHandButton, EquipmentSlot.RING to binding.ringButton
        )

        val defaultImageMap = mapOf(
            binding.helmetButton to R.drawable.helmetslot, binding.neckButton to R.drawable.neckslot,
            binding.chestButton to R.drawable.chestslot, binding.mainHandButton to R.drawable.mainhandslot,
            binding.offHandButton to R.drawable.offhandslot, binding.ringButton to R.drawable.ringslot
        )

        val characterID = viewModel.getSelectedCharacterID()!!
        val character = PlayerInventory.getCharacter(characterID)!!

        map.forEach { slot, button ->
            val equipment = character.currentEquipment[slot.ordinal]
            if (equipment != null) {
                // TODO: Get image from Equipment probably
                button.setImageResource(R.drawable.truck)
            } else {
                button.setImageResource(defaultImageMap[button]!!)
            }
        }
    }

    fun openEquipmentList(slot: EquipmentSlot) {
        closeEquipmentList()

        binding.equipmentList.visibility = View.VISIBLE
        binding.closeListButton.visibility = View.VISIBLE
        slotOpen = slot

        equipmentToChooseFrom = PlayerInventory.getAvailableEquipment(slot)

        inventoryList.add(Item(R.drawable.nothing, "Unequip Item"))

        equipmentToChooseFrom.forEach { pair ->
            if (pair.second > 0) inventoryList.add(Item(R.drawable.truck, pair.first.toString() + "   x${pair.second}"))
        }
    }

    fun closeEquipmentList() {
        slotOpen = null
        binding.equipmentList.visibility = View.GONE
        binding.closeListButton.visibility = View.GONE
        equipmentToChooseFrom.clear()
        inventoryList.clear()
    }

    fun selectedEquipment(ind: Int, item: Item) {
        val characterID = viewModel.getSelectedCharacterID()!!
        val character = PlayerInventory.getCharacter(characterID)

        if (character == null) return

        // Represents first item being X
        if (ind == 0) {
            unassignEquipment(character)

        } else {

            // -1 b/c everything was pushed one back for X
            val pieceOfEquipment = equipmentToChooseFrom[ind - 1].first

            unassignEquipment(character)

            character.addEquipment(slotOpen!!, pieceOfEquipment)
            PlayerInventory.usePieceOfEquipment(pieceOfEquipment.id)
        }

        lifecycleScope.launch {
            saveCharacter(characterID)
        }

        updateView()
        closeEquipmentList()
    }

    fun unassignEquipment(character: PlayerCharacter) {
        if (character.currentEquipment[slotOpen!!.ordinal] != null) {
            val equipmentID = character.currentEquipment[slotOpen!!.ordinal]!!.id
            PlayerInventory.unUsePieceOfEquipment(equipmentID)
            character.removeEquipment(slotOpen!!)
        }
    }
}