package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AstraPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("astra_preferences", Context.MODE_PRIVATE)

    private val _customApiKey = MutableStateFlow(prefs.getString(KEY_API_KEY, "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _selectedModel = MutableStateFlow(prefs.getString(KEY_MODEL, "gemini-3.5-flash") ?: "gemini-3.5-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _languagePreference = MutableStateFlow(prefs.getString(KEY_LANG, "Auto-detect (English / Hindi / Hinglish)") ?: "Auto-detect (English / Hindi / Hinglish)")
    val languagePreference: StateFlow<String> = _languagePreference.asStateFlow()

    private val _toneStyle = MutableStateFlow(prefs.getString(KEY_TONE, "Friendly & Practical") ?: "Friendly & Practical")
    val toneStyle: StateFlow<String> = _toneStyle.asStateFlow()

    private val _ttsEnabled = MutableStateFlow(prefs.getBoolean(KEY_TTS_ENABLED, true))
    val ttsEnabled: StateFlow<Boolean> = _ttsEnabled.asStateFlow()

    fun setCustomApiKey(key: String) {
        prefs.edit().putString(KEY_API_KEY, key.trim()).apply()
        _customApiKey.value = key.trim()
    }

    fun setSelectedModel(model: String) {
        prefs.edit().putString(KEY_MODEL, model).apply()
        _selectedModel.value = model
    }

    fun setLanguagePreference(language: String) {
        prefs.edit().putString(KEY_LANG, language).apply()
        _languagePreference.value = language
    }

    fun setToneStyle(tone: String) {
        prefs.edit().putString(KEY_TONE, tone).apply()
        _toneStyle.value = tone
    }

    fun setTtsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TTS_ENABLED, enabled).apply()
        _ttsEnabled.value = enabled
    }

    companion object {
        private const val KEY_API_KEY = "custom_api_key"
        private const val KEY_MODEL = "selected_model"
        private const val KEY_LANG = "language_pref"
        private const val KEY_TONE = "tone_style"
        private const val KEY_TTS_ENABLED = "tts_enabled"
    }
}
