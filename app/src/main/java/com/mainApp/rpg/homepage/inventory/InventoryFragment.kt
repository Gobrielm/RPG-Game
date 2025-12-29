package com.mainApp.rpg.homepage.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mainApp.rpg.R
import com.mainApp.rpg.databinding.FragmentInventoryBinding
import com.mainApp.rpg.dungeonPrep.DungeonPrepActivity
import com.mainApp.rpg.homepage.HomePage
import com.mainApp.rpg.homepage.inventory.PlayerInventory.playerCharacters
import com.mainApp.rpg.playerCharacter.CharacterMainClass
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.util.LinkedList

class InventoryFragment() : Fragment() {
    enum class InventoryMode { VIEW, PICK }

    var mode: InventoryMode = InventoryMode.VIEW

    private var _binding: FragmentInventoryBinding? = null

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
    private val inventoryList: LinkedList<ItemWID> = LinkedList<ItemWID>()
    lateinit var recyclerView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val status = arguments?.getBoolean("PICK")
        if (status != null && status) {
            mode = InventoryMode.PICK
            binding.newCharacterButton.visibility = View.GONE
        }

        recyclerView =  view.findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(context)

        refreshInventoryScreen()

        binding.newCharacterButton.setOnClickListener {
            (requireContext() as HomePage).openCharacterCreator()
        }
    }

    fun refreshInventoryScreen() {
        inventoryList.clear()

        val getImage: (CharacterMainClass) -> Int = { ind ->
            when (ind) {
                CharacterMainClass.MELEE -> R.drawable.knightpic
                CharacterMainClass.MAGIC -> R.drawable.wizardpic
                CharacterMainClass.RANGED -> R.drawable.rangedpic
                else -> R.drawable.lessthanthree
            }
        }

        playerCharacters.forEach { (_, playerCharacter) ->
            inventoryList.add(ItemWID(playerCharacter.id,
                Item(getImage(playerCharacter.playerClass.mainClass), playerCharacter.toString())
            ))
        }
        requireActivity().runOnUiThread {
            refreshInventoryBinding()
        }
    }

    fun refreshInventoryBinding() {
        recyclerView.adapter = InventoryManager(inventoryList, ::playerCharacterClicked) { holder, item ->
            holder.imageButton.setImageResource(item.item.imageResId)
            holder.imageButton.scaleType = ImageView.ScaleType.CENTER_CROP
            holder.text.text = item.item.text
        }
    }

    fun playerCharacterClicked(position: Int, item: ItemWID) {
        if (mode == InventoryMode.VIEW) {
            editCharacter(item)
        } else {
            selectCharacter(item)
        }
    }

    fun editCharacter(item: ItemWID) {
        (requireActivity() as HomePage).openCharacterViewer(item.id)
    }

    fun selectCharacter(item: ItemWID) {
        (requireContext() as DungeonPrepActivity)
            .selectCharacter(PlayerInventory.getCharacter(item.id)!!, item.item.imageResId)
    }
}