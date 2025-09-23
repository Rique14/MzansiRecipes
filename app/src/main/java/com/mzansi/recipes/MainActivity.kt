package com.mzansi.recipes

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.mzansi.recipes.di.AppModules
import com.mzansi.recipes.navigation.AppNavHost
import com.mzansi.recipes.ui.theme.MzansiTheme
import com.mzansi.recipes.ViewModel.SettingsViewModel
import com.mzansi.recipes.ViewModel.SettingsViewModelFactory

// Changed from ComponentActivity to AppCompatActivity to support automatic locale handling
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val prefs = AppModules.provideUserPrefs(this)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val settingsVm: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(prefs))
            val settings by settingsVm.settings.collectAsState(initial = null)

            val dark = when (settings?.theme ?: "system") {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            MzansiTheme(darkTheme = dark) {
                val nav = rememberNavController()
                AppNavHost(nav)
            }
        }
    }
}
