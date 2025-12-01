package com.example.bikinggame.dungeonPrep

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.example.bikinggame.R
import com.example.bikinggame.databinding.ActivityDungeonPrepBinding
import com.example.bikinggame.dungeonExploration.DungeonExplorationActivity
import com.example.bikinggame.playerCharacter.PlayerCharacter
import kotlin.getValue

class DungeonPrepActivity: AppCompatActivity() {

    private lateinit var binding: ActivityDungeonPrepBinding

    private val viewModel: DungeonPrepViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDungeonPrepBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.overlayDungeonPrep.characterButton.setOnClickListener {
            val navController = findNavController(R.id.nav_host_fragment_dungeon_prep_overlay)
            val bundle = Bundle().apply {
                putBoolean("PICK", true)
            }
            navController.navigate(R.id.selectPlayerCharacter, bundle)
        }

        viewModel.startDungeon.observe(this, Observer {
            tryToStartDungeon()
        })

        viewModel.selectedCharacter.observe(this, Observer {
            val navController = findNavController(R.id.nav_host_fragment_dungeon_prep_overlay)
            navController.navigate(R.id.selectDungeonFragment)
        })

    }

    fun tryToStartDungeon() {
        if (viewModel.selectedCharacter.value == null) {
            return
        }

        val playerCharacter: PlayerCharacter = viewModel.selectedCharacter.value!!

        val intent = Intent(this, DungeonExplorationActivity::class.java)
        intent.putExtra("CHARACTER", playerCharacter.id)
        startActivity(intent)
    }
}

class DungeonPrepViewModel: ViewModel() {
    private val mutableSelectedCharacter = MutableLiveData<PlayerCharacter>()
    private val mutableStartDungeon = MutableLiveData<Boolean>()

    val selectedCharacter: LiveData<PlayerCharacter> get() = mutableSelectedCharacter
    val startDungeon: LiveData<Boolean> get() = mutableStartDungeon

    fun selectCharacter(pCharacter: PlayerCharacter) {
        mutableSelectedCharacter.value = pCharacter
    }

    fun startDungeon() {
        mutableStartDungeon.value = true
    }

}