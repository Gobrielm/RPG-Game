package com.example.bikinggame

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bikinggame.databinding.LoginScreenBinding
import com.example.bikinggame.homepage.HomePage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: LoginScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
//            Firebase.auth.signOut()
            goToHomePage()
        }

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