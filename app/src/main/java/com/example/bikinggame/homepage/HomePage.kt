package com.example.bikinggame.homepage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.bikinggame.R
import com.example.bikinggame.characterCreation.CharacterCreationActivity
import com.example.bikinggame.characterViewer.CharacterViewerActivity
import com.example.bikinggame.databinding.ActivityHomePageBinding
import com.example.bikinggame.dungeonPrep.DungeonPrepActivity
import com.example.bikinggame.homepage.inventory.PlayerInventory
import com.example.bikinggame.requests.getUserJson
import com.example.bikinggame.requests.makeGetRequest
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch


class HomePage : AppCompatActivity() {
    private lateinit var binding: ActivityHomePageBinding

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

        setPoints(PlayerInventory.getCoins().toString())

        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Do nothing
                }
            }
        )
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

    fun openCharacterCreator() {
        val intent = Intent(this, CharacterCreationActivity::class.java)
        startActivity(intent)
    }

    fun setPoints(points: String) {
        binding.pointsText.text = points
        PlayerInventory.setCoins(points.toInt())
    }
}

