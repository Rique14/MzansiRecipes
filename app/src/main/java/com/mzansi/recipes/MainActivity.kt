package com.mzansi.recipes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState // Added this import
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.mzansi.recipes.di.AppModules
import com.mzansi.recipes.navigation.AppNavHost
import com.mzansi.recipes.ui.theme.MzansiTheme
import com.mzansi.recipes.ViewModel.SettingsViewModel
import com.mzansi.recipes.ViewModel.SettingsViewModelFactory
import androidx.core.view.WindowCompat


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() // Added this line for splash screen
        super.onCreate(savedInstanceState)
        val prefs = AppModules.provideUserPrefs(this)
        setContent {
            val settingsVm: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(prefs))
            // Corrected this line:
            val settings = settingsVm.settings.collectAsState(initial = null).value 
            val dark = when (settings?.theme ?: "system") {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            MzansiTheme(darkTheme = dark) {
                val nav = rememberNavController()
                AppNavHost(nav)
            }
            WindowCompat.setDecorFitsSystemWindows(window, false)

        }
    }
}