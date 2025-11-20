package com.example.bikinggame.homepage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.bikinggame.R
import com.example.bikinggame.databinding.ActivityHomePageBinding
import com.example.bikinggame.dungeon.DungeonExplorationActivity
import com.example.bikinggame.dungeonPrep.DungeonPrepActivity
import com.example.bikinggame.playerCharacter.PlayerCharacter
import com.example.bikinggame.playerCharacter.createEquipment
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resumeWithException


class HomePage : AppCompatActivity() {

    private lateinit var binding: ActivityHomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Find the view pager that will allow the user to swipe between fragments
        val viewPager = findViewById<ViewPager2>(R.id.homePageViewPager)
        // Create an adapter that knows which fragment should be shown on each page
        val adapter = HomePageFragmentAdapter(this)
        // Set the adapter onto the view pager
        viewPager.offscreenPageLimit = 3
        viewPager.adapter = adapter
        viewPager.currentItem = 1
    }

    fun openDungeonScreen() {
        val intent = Intent(this, DungeonExplorationActivity::class.java)
        intent.putExtra("CHARACTER", PlayerCharacter(JSONArray(intArrayOf(1, 2, 3))).serialize().toString())
        startActivity(intent)
    }

    fun openDungeonPrepScreen() {
        val intent = Intent(this, DungeonPrepActivity::class.java)
        startActivity(intent)
    }

}

fun makePostRequest(url: String, token: String, body: RequestBody, callback: (JSONObject) -> Unit = ::logRes) {
    val request = Request.Builder()
        .url(url)
        .post(body)
        .addHeader("Authorization", token)
        .build()

    makeRequestTemp(request, callback)
}

fun makeGetRequest(url: String, token: String, callback: (JSONObject) -> Unit = ::logRes) {
    val request = Request.Builder()
        .url(url)
        .get()
        .addHeader("Authorization", token)
        .build()

    makeRequestTemp(request, callback)
}

suspend fun makeGetRequest(url: String, token: String): JSONArray {
    val request = Request.Builder()
        .url(url)
        .get()
        .addHeader("Authorization", token)
        .build()

    return makeRequestTemp(request)
}

suspend fun makeRequestTemp(request: Request): JSONArray =
    suspendCancellableCoroutine { cont ->
        val client = OkHttpClient()
        val call = client.newCall(request)

        // Enqueue the async call
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Resume coroutine with exception
                if (!cont.isCompleted) cont.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    response.use {
                        if (!response.isSuccessful) {
                            // You can throw an exception or handle error as needed
                            if (!cont.isCompleted) cont.resumeWithException(IOException("Unexpected code $response"))
                        } else {
                            val body = response.body ?: throw IOException("Empty response body")
                            val json = JSONObject(body.string())
                            val jsonArray: JSONArray = json.get("data") as JSONArray
                            if (!cont.isCompleted) cont.resume(jsonArray) { cause, _, _ -> (cause) }
                        }
                    }
                } catch (e: Exception) {
                    if (!cont.isCompleted) cont.resumeWithException(e)
                }
            }
        })

        // Optional: handle cancellation
        cont.invokeOnCancellation { call.cancel() }
    }


fun makeRequestTemp(request: Request, callback: (JSONObject) -> Unit) {
    val client = OkHttpClient()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            // Handle error
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                if (!response.isSuccessful) {
                    logResErr(response)
                } else {
                    if (response.body == null) return
                    val body: ResponseBody = (response.body) as ResponseBody
                    val msg = body.string()
                    val json = JSONObject(msg)
                    callback.invoke(json)
                }
            }
        }
    })
}

fun makeRequest(url: String, body: RequestBody, callback: (JSONObject) -> Unit = ::logRes) {
    val request = Request.Builder()
        .url(url)
        .post(body)
        .build()

    val client = OkHttpClient()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            // Handle error
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                if (!response.isSuccessful) {
                    logResErr(response)
                } else {
                    if (response.body == null) return
                    val body: ResponseBody = (response.body) as ResponseBody
                    val msg = body.string()
                    val json = JSONObject(msg)
                    callback.invoke(json)
                }
            }
        }
    })
}


suspend fun getUserToken(): String? {
    val mUser = Firebase.auth.currentUser ?: return null
    return try {
        val result = mUser.getIdToken(true).await()
        Log.d("AAA", result.token.toString())
        result.token
    } catch (e: Exception) {
        Log.e("Getting Token", "Failed to get token", e)
        null
    }
}

suspend fun getUserJson(): JSONObject? {
    val user = Firebase.auth.currentUser ?: return null
    return try {
        val json = JSONObject()
        json.put("email", user.email.toString())
        val result = user.getIdToken(false).await()
        json.put("token", result.token)
        json
    } catch (e: Exception) {
        Log.e("Getting User Json", "Failed...", e)
        null
    }
}

fun logRes(json: JSONObject) {
    Log.d("HTTPS Request", json.toString())
}

fun logResErr(response: Response) {
    response.body?.let { body ->
        val jsonText = body.string()
        Log.d("HTTPS Request", jsonText)
        try {
            val json = JSONObject(jsonText)
            Log.d("HTTPS Request Error: ", json.get("error").toString())
            Log.d("HTTPS Request Msg: ", json.get("message").toString())
        } catch (e: Exception) {
            Log.d("HTTPS Request", "Could not Reach Server.")
        }
    }
}