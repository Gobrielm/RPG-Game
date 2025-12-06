package com.example.bikinggame.dungeonExploration

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bikinggame.R
import com.example.bikinggame.databinding.FragmentDungeonFinishBinding
import com.example.bikinggame.homepage.inventory.InventoryManager
import com.example.bikinggame.homepage.inventory.Item
import com.example.bikinggame.homepage.inventory.PlayerInventory
import com.example.bikinggame.homepage.inventory.saveCharacter
import com.example.bikinggame.homepage.inventory.savePoints
import com.example.bikinggame.homepage.inventory.updateEquipmentCount
import com.example.bikinggame.playerCharacter.Equipment
import kotlinx.coroutines.launch
import java.util.LinkedList
import kotlin.getValue

class DungeonFinishFragment: Fragment() {

    private var _binding: FragmentDungeonFinishBinding? = null

    private val binding get() = _binding!!
    private val viewModel: DungeonExplorationViewModel by activityViewModels()

    private val inventoryList: LinkedList<Item> = LinkedList<Item>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDungeonFinishBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.backButton.setOnClickListener {
            (requireContext() as DungeonExplorationActivity).moveToMainMenu()
        }

        val loot = viewModel.getLootEarned()
        val exp = loot.first()
        loot.removeAt(0)
        val coins = loot.first()
        loot.removeAt(0)

        // Update with loot locally
        inventoryList.add(Item(R.drawable.truck, "Exp Earned: $exp"))
        viewModel.getSelectedCharacter()!!.addExp(exp)

        inventoryList.add(Item(R.drawable.truck, "Coins Earned: $coins"))
        PlayerInventory.setCoins(PlayerInventory.getCoins() + coins)

        loot.forEach { lootID ->
            val equipment = Equipment.getEquipment(lootID)
            inventoryList.add(Item(R.drawable.truck, equipment!!.name))
            PlayerInventory.addPieceOfEquipment(lootID)
        }

        // Update on cloud
        lifecycleScope.launch {
            saveCharacter(viewModel.getSelectedCharacter()!!.id)
            savePoints()

            loot.forEach { equipmentID ->
                updateEquipmentCount(equipmentID)
            }
        }

        val linearLayout = binding.lootEarned

        linearLayout.layoutManager = LinearLayoutManager(context)

        linearLayout.adapter = InventoryManager(inventoryList, ::doNothing)

        return root
    }

    fun doNothing(ind: Int) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}