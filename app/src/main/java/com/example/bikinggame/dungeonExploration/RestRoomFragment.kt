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
import androidx.lifecycle.Observer
import com.example.bikinggame.databinding.FragmentRegularRoomBinding
import com.example.bikinggame.databinding.FragmentRestRoomBinding
import kotlin.getValue

class RestRoomFragment : Fragment() {

    private var _binding: FragmentRestRoomBinding? = null

    private val binding get() = _binding!!

    private val viewModel: DungeonExplorationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRestRoomBinding.inflate(inflater, container, false)
        val root: View = binding.root

        blurBackground()

        binding.restButton.setOnClickListener {
            viewModel.getSelectedCharacter()!!.healCharacter(0.25)
            viewModel.setReadyForNextRoom()
        }

        binding.exitButton.setOnClickListener {
            viewModel.setPartyIsDone()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun blurBackground() {
        val backgroundImage = binding.background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val radius = 10f
            backgroundImage.setRenderEffect(
                RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
            )
        }
    }
}