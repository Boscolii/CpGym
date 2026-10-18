package com.example.cpgym.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "gymrats_preferences")
private val USER_NAME = stringPreferencesKey("user_name")

class UserPreferences(private val context: Context) {
    val userName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME].orEmpty()
    }

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME] = name.trim()
        }
    }
}
