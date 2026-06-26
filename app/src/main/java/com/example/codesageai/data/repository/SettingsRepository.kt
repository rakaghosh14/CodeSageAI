package com.example.codesageai.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("codesage_settings", Context.MODE_PRIVATE)

    private val _geminiApiKey = MutableStateFlow(prefs.getString("gemini_api_key", "") ?: "")
    val geminiApiKey: StateFlow<String> = _geminiApiKey

    private val _judge0ApiKey = MutableStateFlow(prefs.getString("judge0_api_key", "") ?: "")
    val judge0ApiKey: StateFlow<String> = _judge0ApiKey

    private val _useMockMode = MutableStateFlow(prefs.getBoolean("use_mock_mode", true))
    val useMockMode: StateFlow<Boolean> = _useMockMode

    private val _themePreference = MutableStateFlow(prefs.getString("theme_preference", "Dark") ?: "Dark")
    val themePreference: StateFlow<String> = _themePreference

    fun saveGeminiApiKey(key: String) {
        prefs.edit().putString("gemini_api_key", key).apply()
        _geminiApiKey.value = key
    }

    fun saveJudge0ApiKey(key: String) {
        prefs.edit().putString("judge0_api_key", key).apply()
        _judge0ApiKey.value = key
    }

    fun saveUseMockMode(mock: Boolean) {
        prefs.edit().putBoolean("use_mock_mode", mock).apply()
        _useMockMode.value = mock
    }

    fun saveThemePreference(theme: String) {
        prefs.edit().putString("theme_preference", theme).apply()
        _themePreference.value = theme
    }
}
