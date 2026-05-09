package com.example.networkintelligence.data.remote

import android.util.Log
import com.example.networkintelligence.BuildConfig
import com.example.networkintelligence.util.APP_TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class GeminiApi @Inject constructor(
    @Named("gemini") private val client: OkHttpClient,
) {

    suspend fun generate(systemContext: String, conversationHistory: List<Pair<String, String>>): String =
        withContext(Dispatchers.IO) {
            val contents = JSONArray()

            // System context as first user turn
            contents.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", systemContext)))
            })
            contents.put(JSONObject().apply {
                put("role", "model")
                put("parts", JSONArray().put(JSONObject().put("text", "Understood. I'll use this network context to answer your questions.")))
            })

            // Conversation history: list of (userMsg, modelMsg) pairs
            conversationHistory.dropLast(1).forEach { (user, model) ->
                contents.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", user)))
                })
                contents.put(JSONObject().apply {
                    put("role", "model")
                    put("parts", JSONArray().put(JSONObject().put("text", model)))
                })
            }
            // Last item is the pending user message (no model reply yet)
            conversationHistory.lastOrNull()?.let { (user, _) ->
                contents.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", user)))
                })
            }

            val payload = JSONObject().apply {
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("maxOutputTokens", 512)
                    put("temperature", 0.7)
                })
            }.toString()

            val url = "$BASE_URL?key=${BuildConfig.GEMINI_API_KEY}"
            val request = Request.Builder()
                .url(url)
                .post(payload.toRequestBody("application/json".toMediaType()))
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string() ?: return@withContext ERROR_RESPONSE
                    if (!response.isSuccessful) {
                        Log.w(APP_TAG, "Gemini API error ${response.code}: $body")
                        return@withContext "Error ${response.code}: Unable to get a response. Please try again."
                    }
                    parseResponse(body)
                }
            } catch (t: Throwable) {
                Log.e(APP_TAG, "Gemini API exception: ${t.message}")
                "Network error: ${t.message ?: "Unknown error"}. Please check your connection and try again."
            }
        }

    private fun parseResponse(body: String): String {
        return try {
            val json = JSONObject(body)
            json
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
                .trim()
        } catch (t: Throwable) {
            Log.e(APP_TAG, "Gemini parse error: ${t.message}")
            ERROR_RESPONSE
        }
    }

    companion object {
        private const val BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"
        private const val ERROR_RESPONSE =
            "Sorry, I couldn't parse the response. Please try again."
    }
}
