package com.cropwise.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "cropwise_session")

/** Persists the JWT and a few profile values used as live AI context. */
class SessionStore(private val context: Context) {

    companion object {
        val TOKEN = stringPreferencesKey("token")
        val USER_ID = stringPreferencesKey("user_id")
        val LANGUAGE = stringPreferencesKey("language")
        val STATE = stringPreferencesKey("state")
    }

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN] }
    val language: Flow<String?> = context.dataStore.data.map { it[LANGUAGE] }
    val state: Flow<String?> = context.dataStore.data.map { it[STATE] }

    suspend fun saveSession(token: String, userId: String, language: String?) {
        context.dataStore.edit {
            it[TOKEN] = token
            it[USER_ID] = userId
            if (language != null) it[LANGUAGE] = language
        }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[LANGUAGE] = lang }
    }

    suspend fun setState(state: String) {
        context.dataStore.edit { it[STATE] = state }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
