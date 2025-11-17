package com.example.bikinggame.dungeonPrep

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.bikinggame.databinding.FragmentSelectDungeonBinding
import com.example.bikinggame.dungeon.Dungeon
import kotlin.getValue


class SelectDungeonFragment : Fragment() {

    private var _binding: FragmentSelectDungeonBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DungeonPrepViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSelectDungeonBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.forestButton.setOnClickListener {
            selectDungeon(1)
        }

        binding.caveButton.setOnClickListener {
            selectDungeon(2)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun selectDungeon(id: Int) {
        viewModel.selectDungeon(Dungeon(id))
    }

}