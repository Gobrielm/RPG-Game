package com.example.bikinggame.dungeonExploration

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bikinggame.R
import com.example.bikinggame.databinding.FragmentDungeonFinishBinding
import com.example.bikinggame.homepage.inventory.InventoryManager
import com.example.bikinggame.homepage.inventory.Item
import com.example.bikinggame.playerCharacter.Equipment
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
        val exp = loot[0]

        loot.removeAt(0)

        inventoryList.add(Item(R.drawable.truck, "Exp Earned: $exp"))

        loot.forEach { lootID ->
            val equipment = Equipment.getEquipment(lootID)
            inventoryList.add(Item(R.drawable.truck, equipment!!.name))
        }

        //TODO: Save Loot and exp here

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