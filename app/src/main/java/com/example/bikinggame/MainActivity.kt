package com.example.bikinggame

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bikinggame.characterCreation.CharacterCreationActivity
import com.example.bikinggame.databinding.LoginScreenBinding
import com.example.bikinggame.gameState.loadGameState
import com.example.bikinggame.homepage.HomePage
import com.example.bikinggame.requests.getUserJson
import com.example.bikinggame.requests.makeGetRequest
import com.example.bikinggame.requests.makePostRequest
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class MainActivity : AppCompatActivity() {

    private lateinit var binding: LoginScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = LoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            val email = binding.editTextTextEmailAddress.text.toString()
            val password = binding.editTextTextPassword.text.toString()
            login(email, password)
        }

        binding.switchToCreateAccount.setOnClickListener {
            val intent = Intent(this, PasswordSetup::class.java)
            startActivity(intent)
        }

        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
//            Firebase.auth.signOut()
            showLoadingScreen()
            checkAccountHaveUsername()
        }
    }

    fun login(email: String, password: String) {
        val auth = Firebase.auth
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmailAndPassword:success")
                    goToHomePage()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmailAndPassword:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Wrong email or password.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    fun checkAccountHaveUsername() {
        lifecycleScope.launch {
            val json = getUserJson()
            if (json == null) return@launch

            val res = makeGetRequest("https://bikinggamebackend.vercel.app/api/usernames", json.get("token") as String)

            // None empty res will have username
            if (res.length() == 0) {
                val intent = Intent(baseContext, PasswordSetup::class.java)
                intent.putExtra("usernameSetup", true)
                startActivity(intent)
            } else {
                tryLoadGameState()
                goToHomePage()
            }
        }
    }

    fun showLoadingScreen() {
        binding.fragmentContainer.visibility = View.VISIBLE
        binding.mainContent.visibility = View.GONE
    }

    suspend fun tryLoadGameState() {
        loadGameState(this)
    }

    fun goToHomePage() {
        val intent = Intent(this, HomePage::class.java)
        startActivity(intent)
    }

    fun goToCharacterCreation() {
        val intent = Intent(this, CharacterCreationActivity::class.java)
        startActivity(intent)
    }

    /**
     * A native method that is implemented by the 'bikinggame' native library,
     * which is packaged with this application.
     */
    companion object {
        // Used to load the 'bikinggame' library on application startup.
        init {
            System.loadLibrary("bikinggame")
        }
    }
}