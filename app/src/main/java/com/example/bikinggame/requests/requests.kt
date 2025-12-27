package com.example.bikinggame.requests

import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resumeWithException

const val TAG = "REQUESTS"

suspend fun makePostRequest(url: String, token: String, body: RequestBody): JSONObject {
    try {
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", token)
            .build()

        return makeRequestWithResponse(request)
    } catch (e: Exception) {
        Log.d(TAG, e.toString())
    }
    return JSONObject()
}

suspend fun makePutRequest(url: String, token: String, body: RequestBody) {
    try {
        val request = Request.Builder()
            .url(url)
            .put(body)
            .addHeader("Authorization", token)
            .build()

        makeRequestWithoutResponse(request)
    } catch (e: Exception) {
        Log.d(TAG, e.toString())
    }
}

suspend fun makeDeleteRequest(url: String, token: String) {
    try {
        val request = Request.Builder()
            .url(url)
            .delete()
            .addHeader("Authorization", token)
            .build()

        makeRequestWithoutResponse(request)
    } catch (e: Exception) {
        Log.d(TAG, e.toString())
    }
}

fun makeGetRequest(url: String, token: String, callback: (JSONObject) -> Unit = ::logRes) {
    try {
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", token)
            .build()

        makeRequestTemp(request, callback)
    } catch (e: Exception) {
        Log.d(TAG, e.toString())
    }
}

suspend fun makeGetRequest(url: String, token: String): JSONObject {
    try {
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", token)
            .build()

        return makeRequestWithResponse(request)
    } catch (e: Exception) {
        Log.d(TAG, e.toString())
        return JSONObject()
    }
}

suspend fun makeRequestWithoutResponse(request: Request) =
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
                            cont.resume(body) { cause, _, _ -> (cause) }
                        }
                    }
                } catch (e: Exception) {
                    if (!cont.isCompleted) cont.resumeWithException(e)
                }
            }
        })

        cont.invokeOnCancellation { call.cancel() }
    }

suspend fun makeRequestWithResponse(request: Request): JSONObject =
    suspendCancellableCoroutine { cont ->
        val client = OkHttpClient()
        val call = client.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Log, but return an empty JSON object
                Log.e(TAG, "Request failed", e)
                if (!cont.isCompleted) cont.resume(JSONObject()) { cause, _, _ -> (cause) }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    response.use {
                        if (!response.isSuccessful) {
                            logResErr(request, response)
                            if (!cont.isCompleted) cont.resume(JSONObject()) { cause, _, _ -> (cause) }
                            return
                        }

                        val bodyStr = response.body?.string()
                        if (bodyStr == null) {
                            Log.e(TAG, "Response body is null")
                            if (!cont.isCompleted) cont.resume(JSONObject()) { cause, _, _ -> (cause) }
                            return
                        }

                        val fullJson = JSONObject(bodyStr)

                        if (!cont.isCompleted) cont.resume(fullJson) { cause, _, _ -> (cause) }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing response", e)
                    if (!cont.isCompleted) cont.resume(JSONObject()) { cause, _, _ -> (cause) }
                }
            }
        })

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
                    logResErr(request, response)
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

suspend fun getUserName(): String? {
    val token = getUserToken()
    if (token == null) return null

    val res = makeGetRequest(
        "https://bikinggamebackend.vercel.app/api/usernames/",
        token
    )

    if (res.has("data")) {
        val body = res["data"] as? String
        return body
    }
    return null
}

fun logRes(json: JSONObject) {
    Log.d("HTTPS Request", json.toString())
}

fun logResErr(request: Request, response: Response) {
    val url = request.url
    response.body?.let { body ->
        val jsonText = body.string()
        Log.d("HTTPS Request: $url", jsonText)
        try {
            val json = JSONObject(jsonText)
            if (json.has("error")) Log.e("HTTPS Request Error: ", json.get("error").toString())
            if (json.has("message")) Log.e("HTTPS Request Msg: ", json.get("message").toString())
        } catch (e: Exception) {
            Log.d("HTTPS Request", "Could not Reach Server.")
        }
    }
}