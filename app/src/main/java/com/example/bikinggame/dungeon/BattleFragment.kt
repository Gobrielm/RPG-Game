package com.example.bikinggame.dungeon

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.bikinggame.databinding.FragmentBattleBinding
import com.example.bikinggame.databinding.FragmentStatsPreviewBinding
import com.example.bikinggame.playerCharacter.CharacterClass
import com.example.bikinggame.playerCharacter.PlayerCharacter

class BattleFragment : Fragment() {
    private var _binding: FragmentBattleBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBattleBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}