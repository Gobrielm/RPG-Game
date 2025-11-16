package com.example.bikinggame.racing

import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.example.bikinggame.databinding.ActivityRaceBinding
import com.example.bikinggame.playerCharacter.PlayerCharacter
import com.example.bikinggame.homepage.inventory.InventoryFragment
import org.json.JSONArray

class RaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRaceBinding
    var chosenCharacter: PlayerCharacter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityRaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        changeLayout(false)

        binding.cyclistButton.setOnClickListener {
            chooseCyclist()
        }

        binding.raceButton.setOnClickListener {
            startRace()
        }
    }

    fun changeLayout(isInFragmentContainer: Boolean) {
        binding.mainLayout.visibility = if (!isInFragmentContainer) VISIBLE else GONE
        binding.fragmentContainer.visibility = if (isInFragmentContainer) VISIBLE else GONE
    }

    fun chooseCyclist() {
        supportFragmentManager.setFragmentResultListener("raceRequestKey", this) { requestKey, bundle ->
            changeLayout(false)
            val result = bundle.getString("raceResult")
            if (result != null) {
                val characterInfo = JSONArray(result)
                chosenCharacter = PlayerCharacter(characterInfo)
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, InventoryFragment(InventoryFragment.InventoryMode.PICK))
            .addToBackStack(null)
            .commit()
        changeLayout(true)
    }

    fun startRace() {

    }
}