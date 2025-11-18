package com.example.bikinggame.characterCreation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.navigation.findNavController
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.bikinggame.databinding.ActivityCharacterCreationBinding
import com.example.bikinggame.R
import com.example.bikinggame.homepage.HomePage
import com.example.bikinggame.homepage.getUserJson
import com.example.bikinggame.homepage.makePostRequest
import com.example.bikinggame.playerCharacter.CharacterClass
import com.example.bikinggame.playerCharacter.CharacterMainClass
import com.example.bikinggame.playerCharacter.CharacterSubClass
import com.example.bikinggame.playerCharacter.PlayerCharacter
import com.example.bikinggame.homepage.makeRequest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "Character Creation"

class CharacterCreationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCharacterCreationBinding
    private val viewModel: ClassChoiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCharacterCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        setupActionBarWithNavController(navController)

        viewModel.selectedClass.observe(this, Observer { pClass ->
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            navController.navigate(R.id.nav_skills_picker)
        })

        viewModel.selectedSubClass.observe(this, Observer { pClass ->
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            navController.navigate(R.id.nav_stats_preview)
        })

        viewModel.confirmedClass.observe(this, Observer {
            try {
                val playerCharacter = PlayerCharacter(
                    CharacterClass(
                        viewModel.selectedClass.value!!,
                        viewModel.selectedSubClass.value!!
                    ), 1
                )
                initializeFirstCharacter(playerCharacter)
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


    fun initializeFirstCharacter(character: PlayerCharacter) {
        lifecycleScope.launch {
            val json: JSONObject? = getUserJson()
            if (json == null) return@launch
            val characterJSON = character.serialize()

            val body = characterJSON.toString().toRequestBody("application/json".toMediaTypeOrNull())
            makePostRequest("https://bikinggamebackend.vercel.app/api/characters", json.get("token") as String, body, ::finishCharacterCreation)
        }
    }

    fun finishCharacterCreation(json: JSONObject) {
        val characterJson = json.get("data") as JSONArray

        val filename = "characters_data"
        val data = characterJson.toString() + '\n'

        try {
            openFileOutput(filename, MODE_PRIVATE).use {
                it.write(data.toByteArray())
            }

        } catch (err: Exception) {
            Log.d("PlayerCharacterStorage", err.toString())
        }
        goToHomePage()
    }

    fun goToHomePage() {
        val intent = Intent(this, HomePage::class.java)
        startActivity(intent)
    }

}

class ClassChoiceViewModel: ViewModel() {
    private val mutableSelectedClass = MutableLiveData<CharacterMainClass>()
    private val mutableSelectedSubClass = MutableLiveData<CharacterSubClass>()
    private val mutableConfirmed = MutableLiveData<Boolean>()
    val selectedClass: LiveData<CharacterMainClass> get() = mutableSelectedClass
    val selectedSubClass: LiveData<CharacterSubClass> get() = mutableSelectedSubClass
    val confirmedClass: LiveData<Boolean> get() = mutableConfirmed

    fun selectClass(p_class: CharacterMainClass) {
        mutableSelectedClass.value = p_class
    }

    fun selectSubClass(p_class: CharacterSubClass) {
        mutableSelectedSubClass.value = p_class
    }

    fun confirmClass() {
        mutableConfirmed.value = true
    }
}