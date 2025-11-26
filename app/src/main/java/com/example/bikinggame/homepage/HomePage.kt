package com.example.bikinggame.homepage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.bikinggame.R
import com.example.bikinggame.characterViewer.CharacterViewerActivity
import com.example.bikinggame.databinding.ActivityHomePageBinding
import com.example.bikinggame.dungeonPrep.DungeonPrepActivity
import com.example.bikinggame.playerCharacter.PlayerCharacter
import com.example.bikinggame.requests.getUserJson
import com.example.bikinggame.requests.makeGetRequest
import com.example.bikinggame.requests.makeRequest
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.getValue


class HomePage : AppCompatActivity() {

    private val user = Firebase.auth.currentUser
    private lateinit var binding: ActivityHomePageBinding
    private val viewModel: HomePageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Find the view pager that will allow the user to swipe between fragments
        val viewPager = findViewById<ViewPager2>(R.id.homePageViewPager)
        // Create an adapter that knows which fragment should be shown on each page
        val adapter = HomePageFragmentAdapter(this)
        // Set the adapter onto the view pager
        viewPager.offscreenPageLimit = 3
        viewPager.adapter = adapter

        val currentItem = intent.getIntExtra("currentItem", 1)
        viewPager.currentItem = currentItem

        viewModel.selectedCharacterID.observe(this, Observer{ id ->
            openCharacterViewer(id)
        })

        loadPointsLocally()
        loadPointsReq()
    }

    fun openDungeonPrepScreen() {
        val intent = Intent(this, DungeonPrepActivity::class.java)
        startActivity(intent)
    }

    fun openCharacterViewer(id: Int) {
        val intent = Intent(this, CharacterViewerActivity::class.java)
        intent.putExtra("characterID", id)
        startActivity(intent)
    }

    fun setPoints(points: String) {
        binding.pointsText.text = points
    }

    fun loadPointsLocally() {
        val filename = "user_data"
        var points = "0"
        try {
            openFileInput(filename).bufferedReader().useLines { lines ->
                points = (lines.elementAt(0) as String)
            }

        } catch (err: Exception) {
            Log.d("PointsStorage", err.toString())
        }

        setPoints(points)
    }

    fun storePointsLocally(points: String) {
        val filename = "user_data"
        try {
            openFileOutput(filename, Context.MODE_PRIVATE).use {
                it.write(points.toByteArray())
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
            val data = makeGetRequest("https://bikinggamebackend.vercel.app/api/points", json.get("token") as String)
            val points: String = data.get("points").toString()
            storePointsLocally(points)
            setPoints(points)
        }
    }
}

class HomePageViewModel: ViewModel() {
    private val mutableSelectedCharacterID = MutableLiveData<Int>()

    val selectedCharacterID: LiveData<Int> get() = mutableSelectedCharacterID

    fun selectCharacter(pCharacterID: Int) {
        mutableSelectedCharacterID.value = pCharacterID
    }
}

