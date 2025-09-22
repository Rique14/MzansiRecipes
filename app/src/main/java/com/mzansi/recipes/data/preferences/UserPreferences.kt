package com.mzansi.recipes.data.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

data class SettingsState(
    val theme: String = "system", // light|dark|system
    val language: String = "en",  // en|zu
    val notificationsEnabled: Boolean = true
)

class UserPreferences(private val store: DataStore<Preferences>) {
    private val THEME = stringPreferencesKey("theme")
    private val LANG = stringPreferencesKey("lang")
    private val NOTIFS = booleanPreferencesKey("notifs")

    val settings: Flow<SettingsState> = store.data.map { p ->
        SettingsState(
            theme = p[THEME] ?: "system",
            language = p[LANG] ?: "en",
            notificationsEnabled = p[NOTIFS] ?: true
        )
    }

    suspend fun setTheme(theme: String) = store.edit { it[THEME] = theme }
    suspend fun setLanguage(lang: String) = store.edit { it[LANG] = lang }
    suspend fun setNotifications(enabled: Boolean) = store.edit { it[NOTIFS] = enabled }
}