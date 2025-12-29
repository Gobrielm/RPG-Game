package com.mainApp.rpg.characterViewer

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mainApp.rpg.databinding.ActivityCharacterViewerBinding
import com.mainApp.rpg.homepage.HomePage


class CharacterViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCharacterViewerBinding
    private val viewModel: CharacterViewerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCharacterViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val characterID = intent.getIntExtra("characterID", -1)
        if (characterID != -1) {
            viewModel.selectCharacterID(characterID)
        } else {
            goToHomePage()
        }

        binding.overlayCharacterViewer.backButton.setOnClickListener {
            goToHomePage()
        }

    }

    fun goToHomePage() {
        val intent = Intent(this, HomePage::class.java)
        intent.putExtra("currentItem", 0)
        startActivity(intent)
    }
}

class CharacterViewerViewModel(): ViewModel() {
    private val mutableSelectedCharacterID = MutableLiveData<Int>()
    val selectedCharacterID: LiveData<Int> get() = mutableSelectedCharacterID

    fun selectCharacterID(pCharacter: Int) {
        mutableSelectedCharacterID.value = pCharacter
    }

    fun getSelectedCharacterID(): Int? {
        return mutableSelectedCharacterID.value
    }
}
