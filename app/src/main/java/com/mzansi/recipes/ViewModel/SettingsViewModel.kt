package com.mzansi.recipes.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mzansi.recipes.data.preferences.UserPreferences
import kotlinx.coroutines.launch

class SettingsViewModel(private val prefs: UserPreferences) : ViewModel() {
    val settings = prefs.settings
    fun setTheme(t: String) = viewModelScope.launch { prefs.setTheme(t) }
    fun setLanguage(l: String) = viewModelScope.launch { prefs.setLanguage(l) }
    fun setNotifications(b: Boolean) = viewModelScope.launch { prefs.setNotifications(b) }
}