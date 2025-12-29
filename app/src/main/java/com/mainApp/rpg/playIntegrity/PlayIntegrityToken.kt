package com.mainApp.rpg.playIntegrity

import android.content.Context
import android.util.Log
import com.airbnb.lottie.network.FileExtension
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.TreeMap
import kotlin.coroutines.resumeWithException

const val TAG = "PlayIntegrity"

object PlayIntegrityToken {
    var integrityTokenProvider: StandardIntegrityManager.StandardIntegrityTokenProvider? = null
    const val CLOUD_PROJECT_NUMBER = 575785537589

    fun createTokenProvider(
        pContext: Context,
        onSuccess: () -> Unit,
        onFailure: ((Exception) -> Unit)? = null
    ) {
        val standardIntegrityManager: StandardIntegrityManager =
            IntegrityManagerFactory.createStandard(pContext)

        standardIntegrityManager.prepareIntegrityToken(
            StandardIntegrityManager.PrepareIntegrityTokenRequest.builder()
                .setCloudProjectNumber(CLOUD_PROJECT_NUMBER)
                .build()
        ).addOnSuccessListener { tokenProvider ->
            integrityTokenProvider = tokenProvider
            onSuccess()
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Failed to create token provider", exception)
            onFailure?.invoke(exception)
        }
    }

    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))

        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun canonicalJson(obj: JSONObject?): String {
        if (obj == null) return ""

        val sorted = TreeMap<String, Any?>()
        val keys = obj.keys()

        while (keys.hasNext()) {
            val key = keys.next()
            val value = obj.get(key)

            sorted[key] = when (value) {
                is JSONObject -> JSONObject(canonicalJson(value))
                is JSONArray -> canonicalJsonArray(value)
                else -> value
            }
        }

        val result = JSONObject()
        for ((key, value) in sorted) {
            result.put(key, value)
        }

        return result.toString()
    }

    fun canonicalJsonArray(array: JSONArray): JSONArray {
        val result = JSONArray()
        for (i in 0 until array.length()) {
            val value = array.get(i)
            result.put(
                when (value) {
                    is JSONObject -> JSONObject(canonicalJson(value))
                    is JSONArray -> canonicalJsonArray(value)
                    else -> value
                }
            )
        }
        return result
    }

    suspend fun getRequestToken(url: String, httpType: String, body: JSONObject? = null): String =
        suspendCancellableCoroutine { cont ->

            val url = if (url.endsWith('/')) {
                url.substring(0, url.length - 2)
            } else {
                url
            }
            val requestHash = sha256(httpType + url + canonicalJson(body))

            val request = StandardIntegrityManager
                .StandardIntegrityTokenRequest
                .builder()
                .setRequestHash(requestHash)
                .build()

            val provider = integrityTokenProvider
            if (provider == null) {
                cont.resumeWithException(
                    IllegalStateException("IntegrityTokenProvider not ready")
                )
                return@suspendCancellableCoroutine
            }

            provider.request(request)
                .addOnSuccessListener { response ->
                    cont.resume(response.token()) { cause, _, _ -> (cause) }
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        }
}