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
import com.example.bikinggame.dungeon.Dungeon
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

        viewModel.selectedDungeonID.observe(this, Observer {
            tryToStartDungeon()
        })

        viewModel.selectedCharacter.observe(this, Observer {
            val navController = findNavController(R.id.nav_host_fragment_dungeon_prep_overlay)
            navController.navigate(R.id.selectDungeonFragment)
        })

    }

    fun tryToStartDungeon() {
        if (viewModel.selectedCharacter.value == null || viewModel.selectedDungeonID.value == null) {
            return
        }

        val playerCharacter: PlayerCharacter = viewModel.selectedCharacter.value!!
        val dungeonID: Int = viewModel.selectedDungeonID.value!!

        val intent = Intent(this, DungeonExplorationActivity::class.java)
        intent.putExtra("CHARACTER", playerCharacter.id)
        intent.putExtra("DUNGEON", dungeonID)
        startActivity(intent)
    }
}

class DungeonPrepViewModel: ViewModel() {
    private val mutableSelectedCharacter = MutableLiveData<PlayerCharacter>()
    private val mutableSelectedDungeonID = MutableLiveData<Int>()

    val selectedCharacter: LiveData<PlayerCharacter> get() = mutableSelectedCharacter
    val selectedDungeonID: LiveData<Int> get() = mutableSelectedDungeonID

    fun selectCharacter(pCharacter: PlayerCharacter) {
        mutableSelectedCharacter.value = pCharacter
    }

    fun selectDungeon(pDungeonID: Int) {
        mutableSelectedDungeonID.value = pDungeonID
    }

}