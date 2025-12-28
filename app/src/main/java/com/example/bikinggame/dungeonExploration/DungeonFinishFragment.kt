package com.example.bikinggame.dungeonExploration

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bikinggame.R
import com.example.bikinggame.databinding.FragmentDungeonFinishBinding
import com.example.bikinggame.dungeonPrep.deepestRoomAllowed
import com.example.bikinggame.gameState.SaveManager
import com.example.bikinggame.homepage.inventory.InventoryManager
import com.example.bikinggame.homepage.inventory.Item
import com.example.bikinggame.homepage.inventory.PlayerInventory
import com.example.bikinggame.playerCharacter.Equipment
import java.util.LinkedList
import kotlin.math.max

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
        val exp = loot[-2]!!
        val coins = loot[-1]!!

        // Update with loot locally
        inventoryList.add(Item(R.drawable.truck, "Exp: $exp"))
        val expEach = exp / viewModel.getPartySize()

        viewModel.getCharacter(0)?.addExp(expEach)
        viewModel.getCharacter(1)?.addExp(expEach)
        viewModel.getCharacter(2)?.addExp(expEach)

        inventoryList.add(Item(R.drawable.truck, "Coins: $coins"))
        PlayerInventory.setCoins(PlayerInventory.getCoins() + coins)

        for ((lootID, count) in loot) {
            if (lootID < 0 || count <= 0) continue

            val equipment = Equipment.getEquipment(lootID) ?: continue

            inventoryList.add(Item(R.drawable.truck,"${equipment.name} x$count"))

            PlayerInventory.addEquipment(lootID, count)
        }

        SaveManager.markDirty()

        val linearLayout = binding.lootEarned

        linearLayout.layoutManager = LinearLayoutManager(context)

        linearLayout.adapter = InventoryManager(inventoryList, ::doNothing) { holder, item ->
            holder.imageButton.setImageResource(item.imageResId)
            holder.imageButton.scaleType = ImageView.ScaleType.CENTER_CROP
            holder.text.text = item.text
        }


        // Save Deepest Floor
        val deepestRoomRoundTen = ((requireContext() as DungeonExplorationActivity).getCurrentRoom() / 10) * 10

        deepestRoomAllowed.deepestRoom = max(deepestRoomRoundTen, deepestRoomAllowed.deepestRoom)
        SaveManager.markDirty()

        return root
    }

    fun doNothing(ind: Int, item: Item) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}