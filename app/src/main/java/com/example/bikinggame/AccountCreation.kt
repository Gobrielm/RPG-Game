package com.example.bikinggame

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.bikinggame.databinding.CreateAccountBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.actionCodeSettings
import com.google.firebase.auth.auth

class AccountCreation : AppCompatActivity() {
    private lateinit var binding: CreateAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createAccountButton.setOnClickListener {
            lostPassword(binding.signUpAddressBox.text.toString())
        }
    }


    fun lostPassword(email: String) {
        if (!email.contains('@')) return
        if (!email.endsWith(".com")) return

        val actionCodeSettings = actionCodeSettings {
            // TODO: URL Should be to the play store later
            url = "https://biking-game-b4a22.web.app"
            // This must be true
            handleCodeInApp = true
            setAndroidPackageName(
                "com.example.bikinggame",
                true, // installIfNotAvailable
                "16", // minimumVersion
            )
        }

        Firebase.auth.sendSignInLinkToEmail(email, actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent.")
                } else if (task.isCanceled) {
                    Log.d("1", "Email Cancelled.")
                } else {
                    Log.d("2", "Unknown")
                }
            }
    }
}