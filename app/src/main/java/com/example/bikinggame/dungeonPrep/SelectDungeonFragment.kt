package com.example.bikinggame.dungeonPrep

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.bikinggame.databinding.FragmentSelectDungeonBinding
import com.example.bikinggame.homepage.HomePage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

        binding.dungeonButton.setOnClickListener {
            viewModel.startDungeon()
            if (viewModel.getCharacter() == null) {
                lifecycleScope.launch {
                    binding.errorText.text = "Pick a Character"
                    delay(1400)
                    binding.errorText.text = ""
                }
            }
        }

        binding.backButton.setOnClickListener {
            val intent = Intent(requireContext(), HomePage::class.java)
            startActivity(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}