package com.mainApp.rpg

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mainApp.rpg.characterCreation.CharacterCreationActivity
import com.mainApp.rpg.databinding.ActivityPasswordSetupBinding
import com.mainApp.rpg.requests.getUserJson
import com.mainApp.rpg.requests.makePostRequest
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
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

        binding.confirmUsernameButton.setOnClickListener {
            createUsername()
        }

        binding.goBackButton.setOnClickListener {
            goToSignInPage()
        }

        val isOnlySettingUsername = intent.getBooleanExtra("usernameSetup", false)
        if (isOnlySettingUsername) {
            binding.usernameLayout.visibility = View.VISIBLE
            binding.CreateAccount.visibility = View.GONE
            binding.CreateAccountPassword.visibility = View.GONE
            binding.CreateAccountEmail.visibility = View.GONE
            binding.goBackButton.visibility = View.GONE
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
                    binding.usernameLayout.visibility = View.VISIBLE
                    binding.CreateAccount.visibility = View.GONE
                    binding.CreateAccountPassword.visibility = View.GONE
                    binding.CreateAccountEmail.visibility = View.GONE
                    binding.goBackButton.visibility = View.GONE
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

            val emptyBody = "".toRequestBody("application/json".toMediaTypeOrNull())
            makePostRequest("https://bikinggamebackend.vercel.app/api/points", json.get("token") as String, emptyBody)
        }
    }

    fun createUsername() {
        val username = binding.usernameText.text.toString()

        // TODO: Check if username should be allowed
        if (username.length < 6) {
            lifecycleScope.launch {
                binding.usernameExplainText.text = "Username too short ( > 6 characters)"
                binding.usernameExplainText.setTextColor(0xFFFF0000.toInt())
                delay(3000)
                binding.usernameExplainText.text = "Setup Username"
                binding.usernameExplainText.setTextColor(0xFF000000.toInt())
            }
            return
        }

        lifecycleScope.launch {
            val json = getUserJson()
            if (json == null) return@launch

            val jsonObject = JSONObject()
            jsonObject.put("username", username)

            val usernameBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val res = makePostRequest("https://bikinggamebackend.vercel.app/api/usernames/", json.get("token") as String, usernameBody)

            Log.d("AAAA", res.toString())
            if (res.length() == 0) {
                lifecycleScope.launch {
                    binding.usernameExplainText.text = "Username is not unique"
                    binding.usernameExplainText.setTextColor(0xFFFF0000.toInt())
                    delay(3000)
                    binding.usernameExplainText.text = "Setup Username"
                    binding.usernameExplainText.setTextColor(0xFF000000.toInt())
                }
            } else {
                goToCharacterCreation()
            }
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