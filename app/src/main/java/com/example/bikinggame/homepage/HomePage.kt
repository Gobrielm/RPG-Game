package com.example.bikinggame.homepage

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.viewpager2.widget.ViewPager2
import com.example.bikinggame.R
import com.example.bikinggame.characterViewer.CharacterViewerActivity
import com.example.bikinggame.databinding.ActivityHomePageBinding
import com.example.bikinggame.dungeonPrep.DungeonPrepActivity
import com.example.bikinggame.playerCharacter.PlayerCharacter
import kotlin.getValue


class HomePage : AppCompatActivity() {

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
}

class HomePageViewModel: ViewModel() {
    private val mutableSelectedCharacterID = MutableLiveData<Int>()

    val selectedCharacterID: LiveData<Int> get() = mutableSelectedCharacterID

    fun selectCharacter(pCharacterID: Int) {
        mutableSelectedCharacterID.value = pCharacterID
    }
}

