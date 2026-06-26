package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val BYOK_API_KEY = stringPreferencesKey("byok_api_key")
        val GEMINI_MODEL = stringPreferencesKey("gemini_model")
    }

    val apiKeyFlow: Flow<String> = dataStore.data
        .map { preferences ->
            val key = preferences[PreferencesKeys.BYOK_API_KEY] ?: ""
            if (key.isBlank()) com.example.BuildConfig.GEMINI_API_KEY else key
        }

    val geminiModelFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.GEMINI_MODEL] ?: "gemini-2.5-flash"
        }

    suspend fun saveApiKey(apiKey: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BYOK_API_KEY] = apiKey
        }
    }

    suspend fun saveGeminiModel(model: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.GEMINI_MODEL] = model
        }
    }
}
