package com.mainApp.rpg.characterCreation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.mainApp.rpg.databinding.FragmentClassBinding
import com.mainApp.rpg.playerCharacter.CharacterMainClass

class ClassFragment : Fragment() {
    private var _binding: FragmentClassBinding? = null

    private val binding get() = _binding!!

    private val viewModel: ClassChoiceViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClassBinding.inflate(inflater, container, false)
        val root: View = binding.root
        setButtons()

        binding.backButton.setOnClickListener {
            (requireContext() as CharacterCreationActivity).goToHomePage()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setButtons() {
        binding.meleeButton.setOnClickListener {
            viewModel.selectClass(CharacterMainClass.MELEE)
        }

        binding.rangedButton.setOnClickListener {
            viewModel.selectClass(CharacterMainClass.RANGED)
        }

        binding.magicButton.setOnClickListener {
            viewModel.selectClass(CharacterMainClass.MAGIC)
        }
    }

}