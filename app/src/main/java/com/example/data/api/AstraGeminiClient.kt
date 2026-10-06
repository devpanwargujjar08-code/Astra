package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class AstraGeminiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "AstraGeminiClient"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
    }

    suspend fun generateAstraResponse(
        prompt: String,
        history: List<Pair<String, String>>, // sender ("USER" / "ASTRA") to content
        customApiKey: String = "",
        modelName: String = "gemini-3.5-flash",
        toneStyle: String = "Friendly & Practical",
        languagePref: String = "Auto-detect (English / Hindi / Hinglish)"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(customApiKey)
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException(
                    "Gemini API key is not configured. Please set your Gemini API key in Astra Settings or AI Studio Secrets."
                )
            )
        }

        val systemInstructionText = buildSystemInstruction(toneStyle, languagePref)

        try {
            val responseText = executeGeminiCall(apiKey, modelName, prompt, history, systemInstructionText)
            Result.success(responseText)
        } catch (e: Exception) {
            Log.e(TAG, "Primary model $modelName failed: ${e.message}")
            // Fallback retry with gemini-2.5-flash if model wasn't found or temporary error
            if (modelName != "gemini-2.5-flash") {
                try {
                    val fallbackText = executeGeminiCall(apiKey, "gemini-2.5-flash", prompt, history, systemInstructionText)
                    return@withContext Result.success(fallbackText)
                } catch (fallbackEx: Exception) {
                    Log.e(TAG, "Fallback model also failed: ${fallbackEx.message}")
                }
            }
            Result.failure(e)
        }
    }

    private fun resolveApiKey(customApiKey: String): String {
        if (customApiKey.isNotBlank()) return customApiKey.trim()
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
        return buildKey.trim()
    }

    fun hasValidApiKey(customApiKey: String): Boolean {
        val key = resolveApiKey(customApiKey)
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    private fun buildSystemInstruction(toneStyle: String, languagePref: String): String {
        return buildString {
            append("You are Astra, an advanced, friendly, and all-in-one personal AI assistant built for an Android app. ")
            append("Your mission is to help the user with any task, query, or problem they have.\n\n")
            append("Core Capabilities & Guidelines:\n")
            append("1. Daily Problem Solving & Brainstorming: Provide step-by-step, practical solutions for everyday tasks, creative ideas, and daily planning.\n")
            append("2. Technical & Academic Support: Explain academic topics, science, coding, business, and writing clearly with practical examples.\n")
            append("3. Tone & Style: Be empathetic, respectful, encouraging, and clear. Avoid robotic fluff. Adapt dynamically to the user's language (Hindi, Hinglish, or English).\n")
            append("4. Safety & Ethics: Refuse harmful, illegal, or unethical instructions politely, while providing safe alternative guidance.\n")
            append("5. Structure: Use concise paragraphs, bullet points, and clean formatting for easy reading on mobile screens.\n\n")
            append("User Selected Preferences:\n")
            append("- Desired Tone: $toneStyle\n")
            append("- Language Preference: $languagePref (if Hindi or Hinglish is used, respond naturally in conversational Hindi/Hinglish with warmth).")
        }
    }

    private fun executeGeminiCall(
        apiKey: String,
        model: String,
        prompt: String,
        history: List<Pair<String, String>>,
        systemInstruction: String
    ): String {
        val endpoint = "$BASE_URL$model:generateContent?key=$apiKey"

        val requestJson = JSONObject().apply {
            // System instruction
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstruction) })
                })
            })

            // Contents array (Conversation History + Current Prompt)
            val contentsArray = JSONArray()

            // Keep up to last 10 turns to maintain context within budget
            val recentHistory = history.takeLast(10)
            for ((sender, text) in recentHistory) {
                val role = if (sender == "USER") "user" else "model"
                contentsArray.put(JSONObject().apply {
                    put("role", role)
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", text) })
                    })
                })
            }

            // Current prompt
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", prompt) })
                })
            })

            put("contents", contentsArray)

            // Generation config
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("topP", 0.95)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(endpoint)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    val errorObj = errJson.optJSONObject("error")
                    errorObj?.optString("message") ?: "HTTP ${response.code}: ${response.message}"
                } catch (e: Exception) {
                    "HTTP ${response.code}: ${response.message}"
                }
                throw IOException(errorMsg)
            }

            val jsonObject = JSONObject(responseBody)
            val candidates = jsonObject.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val contentObj = candidate.optJSONObject("content")
                val parts = contentObj?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val firstPart = parts.getJSONObject(0)
                    val text = firstPart.optString("text", "")
                    if (text.isNotBlank()) {
                        return text
                    }
                }
            }
            throw IOException("No response text returned from Gemini model")
        }
    }
}
