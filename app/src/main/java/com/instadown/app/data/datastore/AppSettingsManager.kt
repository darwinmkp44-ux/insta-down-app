package com.instadown.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Delegate to create a single instance of DataStore per application
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "instadown_settings")

/**
 * Handles persistent storage of app settings such as the FastSaver API Key and the download subfolder.
 */
class AppSettingsManager(private val context: Context) {

    companion object {
        private val API_KEY = stringPreferencesKey("api_key")
        private val SUBFOLDER = stringPreferencesKey("subfolder")
    }

    /**
     * Emits the current API Key. Emits empty string if not configured yet.
     */
    val apiKeyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[API_KEY] ?: ""
    }

    /**
     * Emits the current subfolder name under the public /Downloads folder. Defaults to "InstaDown".
     */
    val subfolderFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SUBFOLDER] ?: "InstaDown"
    }

    /**
     * Updates the FastSaver API Key securely.
     */
    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = apiKey.trim()
        }
    }

    /**
     * Updates the download subfolder name.
     */
    suspend fun saveSubfolder(subfolder: String) {
        context.dataStore.edit { preferences ->
            val folder = subfolder.trim().replace(Regex("[\\\\/:*?\"<>|]"), "")
            preferences[SUBFOLDER] = if (folder.isEmpty()) "InstaDown" else folder
        }
    }
}
