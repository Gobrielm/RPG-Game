package com.mainApp.rpg.characterViewer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mainApp.rpg.R
import com.mainApp.rpg.databinding.FragmentEditEquipmentBinding
import com.mainApp.rpg.gameState.SaveManager
import com.mainApp.rpg.homepage.inventory.InventoryManager
import com.mainApp.rpg.homepage.inventory.Item
import com.mainApp.rpg.homepage.inventory.PlayerInventory
import com.mainApp.rpg.playerCharacter.Equipment
import com.mainApp.rpg.playerCharacter.EquipmentSlot
import com.mainApp.rpg.playerCharacter.PlayerCharacter
import java.util.LinkedList

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

        reRenderEquipmentList()

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

        map.forEach { (slot, button) ->
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
        val buttonMap = mapOf(
            EquipmentSlot.HEAD to binding.helmetButton, EquipmentSlot.NECK to binding.neckButton,
            EquipmentSlot.TORSO to binding.chestButton, EquipmentSlot.MAIN_HAND to binding.mainHandButton,
            EquipmentSlot.OFF_HAND to binding.offHandButton, EquipmentSlot.RING to binding.ringButton
        )

        val defaultImageMap = mapOf(
            binding.helmetButton to R.drawable.helmetslot, binding.neckButton to R.drawable.neckslot,
            binding.chestButton to R.drawable.chestslot, binding.mainHandButton to R.drawable.mainhandslot,
            binding.offHandButton to R.drawable.offhandslot, binding.ringButton to R.drawable.ringslot
        )

        val equippedImageMap = mapOf(
            binding.helmetButton to R.drawable.helmetslotequipped, binding.neckButton to R.drawable.neckslotequipped,
            binding.chestButton to R.drawable.chestslotequipped, binding.mainHandButton to R.drawable.mainhandslotequipped,
            binding.offHandButton to R.drawable.offhandslotequipped, binding.ringButton to R.drawable.ringslotequipped
        )

        val characterID = viewModel.getSelectedCharacterID()!!
        val character = PlayerInventory.getCharacter(characterID)!!

        buttonMap.forEach { (slot, button) ->
            val equipment = character.currentEquipment[slot.ordinal]
            if (equipment != null) {
                button.setImageResource(equippedImageMap[button]!!)
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

        val playerCharacter = PlayerInventory.getCharacter(viewModel.getSelectedCharacterID()!!)!!
        val equipmentCanRemove = playerCharacter.currentEquipment[slot.ordinal]
        if (equipmentCanRemove != null) {
            inventoryList.add(Item(R.drawable.nothing, "Unequip ${equipmentCanRemove.name}"))
        }

        val defaultImageMap = mapOf(
            EquipmentSlot.HEAD to R.drawable.helmetslot, EquipmentSlot.NECK to R.drawable.neckslot,
            EquipmentSlot.TORSO to R.drawable.chestslot, EquipmentSlot.MAIN_HAND to R.drawable.mainhandslot,
            EquipmentSlot.OFF_HAND to R.drawable.offhandslot, EquipmentSlot.RING to R.drawable.ringslot
        )

        equipmentToChooseFrom.forEach { (equipment, amount) ->
            if (amount > 0 && equipment.id != (equipmentCanRemove?.id ?: -1)) {
                inventoryList.add(Item(defaultImageMap[equipment.slot]!!, equipment.toString() + "   x${amount}"))
            }
        }
        reRenderEquipmentList()
    }

    fun reRenderEquipmentList() {
        binding.equipmentList.adapter = InventoryManager(inventoryList, ::selectedEquipment) { holder, item ->
            holder.imageButton.setImageResource(item.imageResId)
            holder.imageButton.scaleType = ImageView.ScaleType.CENTER_CROP
            holder.text.text = item.text
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
        val offset = if (character.currentEquipment[slotOpen!!.ordinal] != null) 1 else 0

        if (ind == 0 && offset == 1) {
            unassignEquipment(character)
        } else {
            val pieceOfEquipment = equipmentToChooseFrom[ind - offset].first

            unassignEquipment(character)

            character.addEquipment(slotOpen!!, pieceOfEquipment)
            PlayerInventory.usePieceOfEquipment(pieceOfEquipment.id)
        }

        SaveManager.markDirty()

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