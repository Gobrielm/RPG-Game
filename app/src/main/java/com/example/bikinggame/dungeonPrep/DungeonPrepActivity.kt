package com.example.bikinggame.dungeonPrep

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.example.bikinggame.dungeon.DungeonExplorationActivity
import com.example.bikinggame.playerCharacter.PlayerCharacter
import org.json.JSONArray
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

        viewModel.selectedDungeon.observe(this, Observer {
            tryToStartDungeon()
        })

        viewModel.selectedCharacter.observe(this, Observer {
            val navController = findNavController(R.id.nav_host_fragment_dungeon_prep_overlay)
            navController.navigate(R.id.selectDungeonFragment)
        })

    }


    fun tryToStartDungeon() {
        if (viewModel.selectedCharacter.value == null || viewModel.selectedDungeon.value == null) {
            return
        }

        val playerCharacter: PlayerCharacter = viewModel.selectedCharacter.value!!
        val dungeon: Dungeon = viewModel.selectedDungeon.value!!

        val intent = Intent(this, DungeonExplorationActivity::class.java)
        intent.putExtra("CHARACTER", playerCharacter.serialize().toString())
        intent.putExtra("DUNGEON", dungeon.serialize().toString())
        startActivity(intent)
    }
}

class DungeonPrepViewModel: ViewModel() {
    private val mutableSelectedCharacter = MutableLiveData<PlayerCharacter>()
    private val mutableSelectedDungeon = MutableLiveData<Dungeon>()

    val selectedCharacter: LiveData<PlayerCharacter> get() = mutableSelectedCharacter
    val selectedDungeon: LiveData<Dungeon> get() = mutableSelectedDungeon

    fun selectCharacter(pCharacter: PlayerCharacter) {
        mutableSelectedCharacter.value = pCharacter
    }

    fun selectDungeon(pDungeon: Dungeon) {
        mutableSelectedDungeon.value = pDungeon
    }

}