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

        binding.raceButton.setOnClickListener {
            openRaceScreen()
        }

        loadPointsLocally()
        loadPointsReq()
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

    fun loadPointsLocally() {
        val filename = "user_data"
        var points = "0"
        try {
            requireContext().openFileInput(filename).bufferedReader().useLines { lines ->
                points = (lines.elementAt(0) as String)
            }

        } catch (err: Exception) {
            Log.d("PointsStorage", err.toString())
        }

        try {
            requireActivity().runOnUiThread {
                view?.findViewById<TextView>(R.id.pointsText)?.text = points
            }
        } catch (err: Exception) {
            Log.d("PointsStorage", err.toString())
        }
    }

    fun storePointsLocally(points: Int) {
        val filename = "user_data"
        try {
            if (context == null) return
            requireContext().openFileOutput(filename, Context.MODE_PRIVATE).use {
                it.write(points.toString().toByteArray())
            }
        } catch (err: Exception) {
            Log.d("PointsStorage", err.toString())
        }
    }

    fun loadPointsReq() {
        if (user == null) return
        lifecycleScope.launch {
            val json = getUserJson()
            if (json == null) return@launch

            val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
            makeRequest("https://bikinggamebackend.vercel.app/api/getPoints", body, ::loadPointsRes)
        }
    }

    fun loadPointsRes(json: JSONObject) {
        val points = json.get("points") as Int
        requireActivity().runOnUiThread {
            view?.findViewById<TextView>(R.id.pointsText)?.text = points.toString()
        }
        storePointsLocally(points)
    }
}
