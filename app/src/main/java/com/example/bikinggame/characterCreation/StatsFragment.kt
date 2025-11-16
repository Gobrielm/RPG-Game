package com.example.bikinggame.characterCreation

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.bikinggame.databinding.FragmentStatsPreviewBinding
import com.example.bikinggame.playerCharacter.CharacterClass
import com.example.bikinggame.playerCharacter.PlayerCharacter

private const val TAG = "StatsViewer"
class StatsFragment : Fragment() {
    private var _binding: FragmentStatsPreviewBinding? = null

    private val binding get() = _binding!!

    private val viewModel: ClassChoiceViewModel by activityViewModels()
    private var playerCharacter: PlayerCharacter? = null

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
            confirmCharacter()
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun displayCharacter() {
        binding.characterStats.text = playerCharacter.toString()
    }

    fun confirmCharacter() {
        viewModel.confirmClass()
    }

}