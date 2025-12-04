package com.example.bikinggame.characterCreation

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.bikinggame.databinding.FragmentStatsPreviewBinding
import com.example.bikinggame.homepage.inventory.PlayerInventory
import com.example.bikinggame.playerCharacter.CharacterClass
import com.example.bikinggame.playerCharacter.PlayerCharacter
import com.google.common.math.IntMath.pow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "StatsViewer"
class StatsFragment : Fragment() {
    private var _binding: FragmentStatsPreviewBinding? = null

    private val binding get() = _binding!!

    private val viewModel: ClassChoiceViewModel by activityViewModels()
    private var playerCharacter: PlayerCharacter? = null
    private var confirmed: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatsPreviewBinding.inflate(inflater, container, false)
        val root: View = binding.root
        try {
            playerCharacter = PlayerCharacter(
                CharacterClass(
                    viewModel.selectedClass.value!!,
                    viewModel.selectedSubClass.value!!
                )
            )
            displayCharacter()
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }

        binding.confirmButton.setOnClickListener {
            if (confirmed) {
                checkCostAndConfirmCharacter()
            } else {
                confirmed = true
                binding.confirmButton.text = "Cost: ${calculateCost()}"
                lifecycleScope.launch {
                    delay(4000)
                    binding.confirmButton.text = "Confirm"
                    binding.confirmButton.setTextColor(0xFFFFFFFF.toInt())
                    confirmed = false
                }
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun calculateCost(): Int {
        return pow(PlayerInventory.playerCharacters.size * 30, 2)
    }

    fun displayCharacter() {
        binding.characterStats.text = playerCharacter.toString()
    }

    fun checkCostAndConfirmCharacter() {
        if (PlayerInventory.getCoins() >= calculateCost()) {
            (requireContext() as CharacterCreationActivity).initializeCharacter(calculateCost())
        } else {
            binding.confirmButton.text = "Insufficient Funds"
            binding.confirmButton.setTextColor(0xFFFF0000.toInt())
        }
    }

}