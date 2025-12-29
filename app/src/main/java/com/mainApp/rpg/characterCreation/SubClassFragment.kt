package com.mainApp.rpg.characterCreation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.mainApp.rpg.databinding.FragmentMagicSubClassesBinding
import com.mainApp.rpg.databinding.FragmentMeleeSubClassesBinding
import com.mainApp.rpg.databinding.FragmentRangedSubClassesBinding
import com.mainApp.rpg.playerCharacter.CharacterMainClass
import com.mainApp.rpg.playerCharacter.CharacterSubClass
import kotlin.getValue

class SubClassFragment : Fragment() {
    private var _magicBinding: FragmentMagicSubClassesBinding? = null
    private var _meleeBinding: FragmentMeleeSubClassesBinding? = null
    private var _rangedBinding: FragmentRangedSubClassesBinding? = null

    private val viewModel: ClassChoiceViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val playerClass = viewModel.selectedClass.value

        return when (playerClass) {
            CharacterMainClass.MELEE -> {
                _meleeBinding = FragmentMeleeSubClassesBinding.inflate(inflater, container, false)
                setupMeleeButtons()
                _meleeBinding!!.root
            }
            CharacterMainClass.RANGED -> {
                _rangedBinding = FragmentRangedSubClassesBinding.inflate(inflater, container, false)
                setupRangedButtons()
                _rangedBinding!!.root
            }
            else -> {
                _magicBinding = FragmentMagicSubClassesBinding.inflate(inflater, container, false)
                setupMagicButtons()
                _magicBinding!!.root
            }
        }
    }

    private fun setupMeleeButtons() {
        _meleeBinding?.apply {
            northButton.setOnClickListener {
                viewModel.selectSubClass(CharacterSubClass.North)
            }
            knightButton.setOnClickListener {
                viewModel.selectSubClass(CharacterSubClass.Knight)
            }
        }
    }

    private fun setupRangedButtons() {
        _rangedBinding?.apply {
            traditionalButton.setOnClickListener {
                viewModel.selectSubClass(CharacterSubClass.TraditionalRanged)
            }
            nonTraditionalButton.setOnClickListener {
                viewModel.selectSubClass(CharacterSubClass.NonTraditionalRanged)
            }
        }
    }

    private fun setupMagicButtons() {
        _magicBinding?.apply {
            traditionalButton.setOnClickListener {
                viewModel.selectSubClass(CharacterSubClass.TraditionalMagic)
            }
            ritualButton.setOnClickListener {
                viewModel.selectSubClass(CharacterSubClass.RitualMagic)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _magicBinding = null
        _meleeBinding = null
        _rangedBinding = null
    }

}
