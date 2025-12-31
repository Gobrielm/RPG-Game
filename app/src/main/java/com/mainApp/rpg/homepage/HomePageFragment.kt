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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomePageFragment : Fragment() {
    private lateinit var binding: FragmentHomePageBinding
    private var deleteAccountClicked: Boolean = false

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
            openLogOutPanel()
        }
        binding.logOutConfirmButton.setOnClickListener {
            (requireActivity() as HomePage).logOut()
        }
        binding.deleteAccountButton.setOnClickListener {
            clickDeleteAccount()
        }
        binding.backButton.setOnClickListener {
            closeLogOutPanel()
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

    fun openLogOutPanel() {
        binding.logOutPanel.visibility = View.VISIBLE
    }

    fun closeLogOutPanel() {
        binding.logOutPanel.visibility = View.GONE
    }

    fun clickDeleteAccount() {
        if (deleteAccountClicked) {
            (requireActivity() as HomePage).deleteAccount()
        } else {
            deleteAccountClicked = true
            binding.logOutConfirmButton.text = "Confirm Deletion"
            lifecycleScope.launch {
                delay(1000)
                deleteAccountClicked = false
                binding.logOutConfirmButton.text = "Delete Account"
            }
        }
    }
}
