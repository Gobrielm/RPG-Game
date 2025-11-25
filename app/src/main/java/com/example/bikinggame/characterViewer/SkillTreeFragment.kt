package com.example.bikinggame.characterViewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.bikinggame.R
import com.example.bikinggame.databinding.FragmentSkillTreeBinding
import com.example.bikinggame.playerCharacter.CharacterSubClass
import com.example.bikinggame.playerCharacter.Skill
import com.example.bikinggame.playerCharacter.SkillTrees

class SkillTreeFragment: Fragment() {

    private var _binding: FragmentSkillTreeBinding? = null

    private val binding get() = _binding!!
    private var skillIDSelected: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSkillTreeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val spreadFactor = 300f

        val skillTree = SkillTrees.skillTrees[CharacterSubClass.Knight]!!
        val nodeButtons = mutableMapOf<Int, Button>()
        val zoomContainer = binding.zoomContainer

        for ((id, pt) in skillTree) {
            val button = Button(requireContext()).apply {
                this.id = View.generateViewId()
                text = "Node $id"
                background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.circle_button
                )
                val size = (60 * resources.displayMetrics.density).toInt() // 60dp
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

        binding.closeMenuButton.setOnClickListener {
            closeSkillInfoPanel()
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openSkillInfoPanel(skillID: Int) {
        skillIDSelected = skillID
        binding.skillMenu.visibility = View.VISIBLE
        binding.skillText.text = Skill.getSkill(skillID).toString()

    }

    private fun closeSkillInfoPanel() {
        skillIDSelected = null
        binding.skillMenu.visibility = View.GONE
    }
}