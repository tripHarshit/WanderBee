package com.example.wanderbee.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        private val JWT_TOKEN = stringPreferencesKey("jwt_token")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_NAME = stringPreferencesKey("user_name")
    }
    
    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_FIRST_LAUNCH] ?: true
        }
    
    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH] = false
        }
    }
    
    suspend fun resetFirstLaunch() {
        context.dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH] = true
        }
    }

    // ── JWT Token ──────────────────────────────────────────────────────────

    val jwtToken: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[JWT_TOKEN] }

    suspend fun getJwtTokenOnce(): String? =
        context.dataStore.data.first()[JWT_TOKEN]

    suspend fun saveJwtToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[JWT_TOKEN] = token
        }
    }

    suspend fun clearJwtToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN)
        }
    }

    // ── User Info ──────────────────────────────────────────────────────────

    val userEmail: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[USER_EMAIL] }

    suspend fun getUserEmailOnce(): String? =
        context.dataStore.data.first()[USER_EMAIL]

    val userName: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[USER_NAME] }

    suspend fun saveUserInfo(email: String, name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_EMAIL] = email
            preferences[USER_NAME] = name
        }
    }

    suspend fun clearUserInfo() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_EMAIL)
            preferences.remove(USER_NAME)
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN)
            preferences.remove(USER_EMAIL)
            preferences.remove(USER_NAME)
        }
    }

    suspend fun isLoggedIn(): Boolean = getJwtTokenOnce() != null
} 