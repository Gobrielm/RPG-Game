package com.example.bikinggame

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bikinggame.characterCreation.CharacterCreationActivity
import com.example.bikinggame.databinding.ActivityPasswordSetupBinding
import com.example.bikinggame.homepage.getUserJson
import com.example.bikinggame.homepage.makeRequest
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class PasswordSetup : AppCompatActivity() {

    private lateinit var binding: ActivityPasswordSetupBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        binding = ActivityPasswordSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.CreateAccount.setOnClickListener {
            createAccount(
                binding.CreateAccountEmail.text.toString(),
                binding.CreateAccountPassword.text.toString()
            )
        }

        binding.goBackButton.setOnClickListener {
            goToSignInPage()
        }
    }

    fun createAccount(email: String, password: String) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            binding.errorMessage.text = "You are already Signed In."
            return
        }
        val isEmailValid: Boolean = setErrorMsgForEmail(email)
        if (!isEmailValid) {
            return
        }
        val status: ERR_PASSWORD = checkPasswordStatus(password)
        setErrorMsgForUser(status)
        if (status != ERR_PASSWORD.OK) {
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    initializeUser()
                    goToCharacterCreation()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    setErrorMsgForUser(ERR_PASSWORD.SERVER_ERR)
                }
            }
    }

    /**
     * @return Returns true if there is no error, and false otherwise
     */
    fun setErrorMsgForEmail(email: String): Boolean {
        val isErr = email.length < 6 || !email.contains('@') || !email.endsWith(".com")

        if (isErr) {
            binding.errorMessage.text = "Please Enter a Valid Email Address."
            return false
        }
        return true
    }

    enum class ERR_PASSWORD {
        OK,
        TOO_SHORT,
        NO_SPECIAL_CHARACTERS,
        NO_NUMBERS,
        SERVER_ERR
    }

    fun checkPasswordStatus(password: String): ERR_PASSWORD {
        if (password.length <= 6) return ERR_PASSWORD.TOO_SHORT
        var hasSpecial = false
        var hasNumber = false

        for (c in password) {
            if (c >= '0' && c <= '9') {
                hasNumber = true
            } else if ((c >= '!' && c <= '/') || (c >= ':' && c <= '@') || (c >= '[' && c <= '`') || (c >= '{' && c <= '~')) {
                hasSpecial = true
            }
        }

        if (!hasNumber) return ERR_PASSWORD.NO_NUMBERS
        if (!hasSpecial) return ERR_PASSWORD.NO_SPECIAL_CHARACTERS

        return ERR_PASSWORD.OK
    }


    fun setErrorMsgForUser(error: ERR_PASSWORD) {

        val errorMsg: String = when(error) {
            ERR_PASSWORD.OK -> ""
            ERR_PASSWORD.TOO_SHORT -> "Password is too short."
            ERR_PASSWORD.NO_NUMBERS -> "Password has no numbers."
            ERR_PASSWORD.NO_SPECIAL_CHARACTERS -> "Password has no special characters."
            ERR_PASSWORD.SERVER_ERR -> "The server may be experiencing some issues, please try again later."
        }
        binding.errorMessage.text = errorMsg
    }


    /**
     * Initializes the database for the user
     */
    fun initializeUser() {
        lifecycleScope.launch {
            val json = getUserJson()
            if (json == null) return@launch
            val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
            makeRequest("https://bikinggamebackend.vercel.app/api/createNewUser", body)
        }
    }

    /**
     * Successfully signs in the user, so it will redirect the user,
     * to the homepage.
     */
    fun goToCharacterCreation() {
        val intent = Intent(this, CharacterCreationActivity::class.java)
        startActivity(intent)
    }

    fun goToSignInPage() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}