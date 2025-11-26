package com.example.bikinggame.homepage

import android.content.Context
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bikinggame.R
import com.example.bikinggame.databinding.FragmentHomePageBinding
import com.example.bikinggame.requests.getUserJson
import com.example.bikinggame.requests.makeRequest
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class HomePageFragment : Fragment() {

    private val user = Firebase.auth.currentUser
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
            openRaceScreen()
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

    fun openRaceScreen() {
        (requireActivity() as HomePage).openDungeonPrepScreen()
    }
}
