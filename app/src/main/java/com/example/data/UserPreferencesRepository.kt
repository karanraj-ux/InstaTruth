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
    }

    val apiKeyFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.BYOK_API_KEY] ?: ""
        }

    suspend fun saveApiKey(apiKey: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BYOK_API_KEY] = apiKey
        }
    }
}
