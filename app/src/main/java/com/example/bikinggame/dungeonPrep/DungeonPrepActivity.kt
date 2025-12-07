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
    private var selectedCharacterButtonInd: Int = -1
    private val partySelected: Array<PlayerCharacter?> = arrayOf(null, null, null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDungeonPrepBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.overlayDungeonPrep.characterButton1.setOnClickListener {
            if (selectedCharacterButtonInd != -1) return@setOnClickListener
            selectedCharacterButtonInd = 1
            val navController = findNavController(R.id.nav_host_fragment_dungeon_prep_overlay)
            val bundle = Bundle().apply {
                putBoolean("PICK", true)
            }
            navController.navigate(R.id.selectPlayerCharacter, bundle)
        }

        binding.overlayDungeonPrep.characterButton2.setOnClickListener {
            if (selectedCharacterButtonInd != -1) return@setOnClickListener
            selectedCharacterButtonInd = 2
            val navController = findNavController(R.id.nav_host_fragment_dungeon_prep_overlay)
            val bundle = Bundle().apply {
                putBoolean("PICK", true)
            }
            navController.navigate(R.id.selectPlayerCharacter, bundle)
        }

        binding.overlayDungeonPrep.characterButton3.setOnClickListener {
            if (selectedCharacterButtonInd != -1) return@setOnClickListener
            selectedCharacterButtonInd = 3
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
        if (selectedCharacterButtonInd == -1) return
        val navController = findNavController(R.id.nav_host_fragment_dungeon_prep_overlay)
        navController.navigate(R.id.selectDungeonFragment)
        val status = putCharacterInParty(character, selectedCharacterButtonInd - 1)

        if (!status) {
            selectedCharacterButtonInd = -1
            return
        }

        when (selectedCharacterButtonInd) {
            1 -> binding.overlayDungeonPrep.characterButton1.setImageResource(imageID)
            2 -> binding.overlayDungeonPrep.characterButton2.setImageResource(imageID)
            else -> binding.overlayDungeonPrep.characterButton3.setImageResource(imageID)
        }
        selectedCharacterButtonInd = -1
    }

    fun putCharacterInParty(character: PlayerCharacter, ind: Int): Boolean {
        partySelected.forEach { otherCharacter ->
            if (otherCharacter?.id == character.id) {
                return false
            }
        }

        partySelected[ind] = character
        return true
    }

    fun tryToStartDungeon() {
        val charactersSelected = getCharacters()
        if (charactersSelected.isEmpty()) {
            return
        }

        val intent = Intent(this, DungeonExplorationActivity::class.java)

        val slots = arrayOf("CHARACTER1", "CHARACTER2", "CHARACTER3")
        for (i in 0 until charactersSelected.size) {
            val character = charactersSelected[i]
            intent.putExtra(slots[i], character.id)
        }

        startActivity(intent)
    }

    fun getCharacters(): ArrayList<PlayerCharacter> {
        val list = ArrayList<PlayerCharacter>()
        partySelected.forEach { character ->
            if (character != null) list.add(character)
        }
        return list
    }
}

class DungeonPrepViewModel: ViewModel() {
    private val mutableStartDungeon = MutableLiveData<Boolean>()
    val startDungeon: LiveData<Boolean> get() = mutableStartDungeon

    fun startDungeon() {
        mutableStartDungeon.value = true
    }
}