package com.mainApp.rpg.homepage

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.mainApp.rpg.MainActivity
import com.mainApp.rpg.R
import com.mainApp.rpg.characterCreation.CharacterCreationActivity
import com.mainApp.rpg.characterViewer.CharacterViewerActivity
import com.mainApp.rpg.databinding.ActivityHomePageBinding
import com.mainApp.rpg.dungeonPrep.DungeonPrepActivity
import com.mainApp.rpg.homepage.inventory.PlayerInventory
import com.google.firebase.auth.FirebaseAuth


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

    fun logOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun setPoints(points: String) {
        binding.pointsText.text = points
        PlayerInventory.setCoins(points.toInt())
    }
}

