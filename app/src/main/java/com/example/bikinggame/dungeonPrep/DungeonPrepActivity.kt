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

    }

    fun selectCharacter(character: PlayerCharacter, imageID: Int) {
        val navController = findNavController(R.id.nav_host_fragment_dungeon_prep_overlay)
        navController.navigate(R.id.selectDungeonFragment)
        viewModel.setCharacter(character)
        binding.overlayDungeonPrep.characterButton.setImageResource(imageID)
    }

    fun tryToStartDungeon() {
        if (viewModel.getCharacter() == null) {
            // TODO: Error msg
            return
        }

        val intent = Intent(this, DungeonExplorationActivity::class.java)
        intent.putExtra("CHARACTER1", viewModel.getCharacter()!!.id)
        startActivity(intent)
    }
}

class DungeonPrepViewModel: ViewModel() {
    private val mutableSelectedCharacter = MutableLiveData<PlayerCharacter>()
    private val mutableStartDungeon = MutableLiveData<Boolean>()
    val startDungeon: LiveData<Boolean> get() = mutableStartDungeon

    fun startDungeon() {
        mutableStartDungeon.value = true
    }
    fun getCharacter(): PlayerCharacter? {
        return mutableSelectedCharacter.value
    }
    fun setCharacter(character: PlayerCharacter) {
        mutableSelectedCharacter.value = character
    }
}