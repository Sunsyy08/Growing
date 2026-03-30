package com.project.growing.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val USER_ID = stringPreferencesKey("user_id")
    }

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID] = userId
        }
    }

    val userId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_ID]
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}