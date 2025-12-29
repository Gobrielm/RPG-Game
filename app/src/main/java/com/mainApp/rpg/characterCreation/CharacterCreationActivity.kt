package com.mainApp.rpg.characterCreation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.mainApp.rpg.R
import com.mainApp.rpg.databinding.ActivityCharacterCreationBinding
import com.mainApp.rpg.gameState.SaveManager
import com.mainApp.rpg.homepage.HomePage
import com.mainApp.rpg.homepage.inventory.PlayerInventory
import com.mainApp.rpg.playerCharacter.CharacterClass
import com.mainApp.rpg.playerCharacter.CharacterMainClass
import com.mainApp.rpg.playerCharacter.CharacterSubClass
import com.mainApp.rpg.playerCharacter.PlayerCharacter
import com.mainApp.rpg.requests.getUserJson
import com.mainApp.rpg.requests.makePostRequest
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
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun initializeCharacter(cost: Int) {
        val character: PlayerCharacter? = try {
            PlayerCharacter(
                CharacterClass(
                    viewModel.selectedClass.value!!,
                    viewModel.selectedSubClass.value!!
                ), 1
            )
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
            null
        }

        if (character == null) return

        PlayerInventory.setCoins(PlayerInventory.getCoins() - cost)

        lifecycleScope.launch {

            // Creates Character
            val json: JSONObject? = getUserJson()
            if (json == null) return@launch
            val oldCharacterJSON = character.serialize()

            val inputObject = JSONObject()
            inputObject.put("characterInfo", oldCharacterJSON)

            val body = inputObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val jsonObject = makePostRequest("https://bikinggamebackend.vercel.app/api/characters/", json.get("token") as String, body)

            val newCharacterJSON = jsonObject.get("data") as JSONArray
            PlayerInventory.addCharacter(PlayerCharacter(newCharacterJSON))

            SaveManager.markDirty()
            goToHomePage()
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
    val selectedClass: LiveData<CharacterMainClass> get() = mutableSelectedClass
    val selectedSubClass: LiveData<CharacterSubClass> get() = mutableSelectedSubClass

    fun selectClass(p_class: CharacterMainClass) {
        mutableSelectedClass.value = p_class
    }

    fun selectSubClass(p_class: CharacterSubClass) {
        mutableSelectedSubClass.value = p_class
    }
}