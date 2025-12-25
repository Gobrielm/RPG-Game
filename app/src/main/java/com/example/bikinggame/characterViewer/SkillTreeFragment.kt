package com.example.bikinggame.characterViewer

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.bikinggame.R
import com.example.bikinggame.databinding.FragmentSkillTreeBinding
import com.example.bikinggame.homepage.inventory.PlayerInventory
import com.example.bikinggame.homepage.inventory.saveCharacter
import com.example.bikinggame.playerCharacter.CharacterSubClass
import com.example.bikinggame.playerCharacter.Skill
import com.example.bikinggame.playerCharacter.SkillTrees
import kotlinx.coroutines.launch
import kotlin.getValue

class SkillTreeFragment: Fragment() {

    private var _binding: FragmentSkillTreeBinding? = null

    private val binding get() = _binding!!
    private val viewModel: CharacterViewerViewModel by activityViewModels()
    private var availableSkillPts: Int = 0
    private val spreadFactor = 600f
    private var skillIDSelected: Int? = null
    private val nodeButtons = mutableMapOf<Int, Button>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSkillTreeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        createSkillTreeGraphic()

        binding.closeMenuButton.setOnClickListener {
            closeSkillInfoPanel()
        }

        binding.unlockSkillButton.setOnClickListener {
            unlockSkill()
        }

        binding.backButton.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.editCharacterFragment)
        }

        updateSkillPointerCount()
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createSkillTreeGraphic() {
        if (viewModel.getSelectedCharacterID() == null) return
        val character = PlayerInventory.getCharacter(viewModel.getSelectedCharacterID()!!)
        if (character == null) return
        val skillTree = SkillTrees.getSkillTree(character.playerClass.mainClass)
        val zoomContainer = binding.zoomContainer

        val skillsUnlocked = character.skillTree.skillsUnlocked

        availableSkillPts = character.skillTree.getAvailableSkillPoints()

        for ((id, pt) in skillTree) {
            var newColor = 0xFFFF0000.toInt()
            val skill = Skill.getSkill(id)
            for (oSkill in skillsUnlocked) {
                if (oSkill.id == id) newColor = 0xFF00FF00.toInt()
            }

            val button = Button(requireContext()).apply {
                this.id = View.generateViewId()
                text = "${skill!!.name}: ${skill!!.id}" // TODO: Change to name
                background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.circle_button
                )
                (background as GradientDrawable).setColor(newColor)
                val size = (120 * resources.displayMetrics.density).toInt() // 60dp
                layoutParams = FrameLayout.LayoutParams(size, size)
                x = pt.x * spreadFactor
                y = pt.y * spreadFactor
            }
            nodeButtons[id] = button
            button.setOnClickListener { openSkillInfoPanel(id) }
            zoomContainer.addView(button)
        }

        zoomContainer.viewTreeObserver.addOnGlobalLayoutListener {
            for ((id, pt) in skillTree) {
                val button = nodeButtons[id]!!
                val skill = Skill.getSkill(id)!!
                for (preReqID in skill.prerequisites) {
                    val button2 = nodeButtons[preReqID]!!
                    zoomContainer.addLine(button to button2)
                }
            }
        }
    }

    private fun openSkillInfoPanel(skillID: Int) {
        skillIDSelected = skillID
        binding.skillMenu.visibility = View.VISIBLE
        binding.skillText.text = Skill.getSkill(skillID).toString()
    }

    private fun unlockSkill() {
        if (skillIDSelected == null) {
            return
        }
        if (availableSkillPts == 0) {
            showSkillButtonErr("Need More Skill Pts")
            return
        }

        val character = PlayerInventory.getCharacter(viewModel.getSelectedCharacterID()!!)!!
        val skill = Skill.getSkill(skillIDSelected!!)!!

        for (skill in character.skillTree.skillsUnlocked) {
            if (skill.id == skillIDSelected) {
                showSkillButtonErr("Already Unlocked")
                return
            }
        }

        for (preReqID in skill.prerequisites) {
            if (!character.hasSkill(preReqID)) {
                showSkillButtonErr("Unlock the Prerequisites")
                return
            }
        }


        character.addSkill(skillIDSelected!!)

        (nodeButtons[skillIDSelected]!!.background as GradientDrawable).setColor(0xFF00FF00.toInt())
        availableSkillPts--
        updateSkillPointerCount()
        closeSkillInfoPanel()

        lifecycleScope.launch {
            saveCharacter(character.id)
        }
    }

    private fun showSkillButtonErr(errText: String) {
        binding.unlockSkillButton.text = errText
        binding.unlockSkillButton.setTextColor(0xFFFF0000.toInt())

        Handler(Looper.getMainLooper()).postDelayed({
            unShowSkillButtonErr()
        }, 750)
    }

    private fun unShowSkillButtonErr() {
        binding.unlockSkillButton.text = "Unlock Skill"
        binding.unlockSkillButton.setTextColor(0xFFFFFFFF.toInt())
    }

    private fun updateSkillPointerCount() {
        binding.skillPointsCount.text = "Pts Avail: $availableSkillPts"
    }

    private fun closeSkillInfoPanel() {
        skillIDSelected = null
        binding.skillMenu.visibility = View.GONE
    }
}