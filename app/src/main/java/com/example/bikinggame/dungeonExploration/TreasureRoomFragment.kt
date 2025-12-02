package com.example.bikinggame.dungeonExploration

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.bikinggame.R
import com.example.bikinggame.databinding.FragmentTreasureRoomBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.getValue

class TreasureRoomFragment : Fragment() {
    private var _binding: FragmentTreasureRoomBinding? = null

    private val binding get() = _binding!!

    private val viewModel: DungeonExplorationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTreasureRoomBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.openChestButton.setOnClickListener {
            val loot = viewModel.getDungeon()!!.rollRandomLoot()// TODO: Do stuff with loot
            viewModel.addLootEarned(loot)

            (requireActivity() as DungeonExplorationActivity).showLootUi(loot)

            binding.openChestButton.setImageResource(R.drawable.openchest)

            lifecycleScope.launch {
                delay(2000)
                (requireActivity() as DungeonExplorationActivity).unShowLootUi()
                viewModel.setReadyForNextRoom()
            }
        }



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}