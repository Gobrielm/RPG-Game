package com.mainApp.rpg

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.mainApp.rpg.characterCreation.CharacterCreationActivity
import com.mainApp.rpg.databinding.LoginScreenBinding
import com.mainApp.rpg.gameState.loadGameState
import com.mainApp.rpg.homepage.HomePage
import com.mainApp.rpg.requests.getUserJson
import com.mainApp.rpg.requests.makeGetRequest
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.mainApp.rpg.playIntegrity.PlayIntegrityToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: LoginScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = LoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showLoadingScreen()

        binding.button.setOnClickListener {
            val email = binding.editTextTextEmailAddress.text.toString()
            val password = binding.editTextTextPassword.text.toString()
            login(email, password)
        }

        binding.switchToCreateAccount.setOnClickListener {
            val intent = Intent(this, PasswordSetup::class.java)
            startActivity(intent)
        }

        PlayIntegrityToken.createTokenProvider(this, ::finishLoading)
    }

    fun finishLoading() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            showLoadingScreen()
            checkAccountHaveUsername()
        } else {
            unShowLoadingScreen()
        }
    }

    fun login(email: String, password: String) {
        showLoadingScreen()
        val auth = Firebase.auth
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmailAndPassword:success")
                    lifecycleScope.launch {
                        tryLoadGameState()
                        goToHomePage()
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmailAndPassword:failure", task.exception)
                    lifecycleScope.launch {
                        unShowLoadingScreen()
                        delay(100)
                        Toast.makeText(
                            baseContext,
                            "Wrong email or password.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }

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

    fun unShowLoadingScreen() {
        binding.fragmentContainer.visibility = View.GONE
        binding.mainContent.visibility = View.VISIBLE
    }

    suspend fun tryLoadGameState() {
        loadGameState(this)
    }

    fun goToHomePage() {
        val intent = Intent(this, HomePage::class.java)
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