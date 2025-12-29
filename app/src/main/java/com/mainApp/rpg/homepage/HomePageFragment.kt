package com.mainApp.rpg.homepage

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mainApp.rpg.databinding.FragmentHomePageBinding
import com.mainApp.rpg.gameState.loadPoints
import kotlinx.coroutines.launch

class HomePageFragment : Fragment() {
    private lateinit var binding: FragmentHomePageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        blurBackground()

        binding.dungeonButton.setOnClickListener {
            openDungeonScreen()
        }
        binding.logOutButton.setOnClickListener {
            (requireActivity() as HomePage).logOut()
        }
        lifecycleScope.launch {
            loadPoints()
        }
    }

    fun blurBackground() {
        val backgroundImage = binding.backgroundHomePage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val radius = 10f
            backgroundImage.setRenderEffect(
                RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
            )
        }
    }

    fun openDungeonScreen() {
        (requireActivity() as HomePage).openDungeonPrepScreen()
    }
}
