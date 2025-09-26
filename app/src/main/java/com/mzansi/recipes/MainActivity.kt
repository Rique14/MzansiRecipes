package com.mzansi.recipes

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration // Added for onConfigurationChanged
import android.os.Build
import android.os.Bundle
import android.util.Log // Added for logging
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.mzansi.recipes.di.AppModules
import com.mzansi.recipes.navigation.AppNavHost
import com.mzansi.recipes.ui.theme.MzansiTheme
import com.mzansi.recipes.ViewModel.SettingsViewModel
import com.mzansi.recipes.ViewModel.SettingsViewModelFactory // Ensure this import is correct
import android.content.res.Resources
import com.mzansi.recipes.util.NetworkMonitor

class MainActivity : AppCompatActivity() {

    private lateinit var networkMonitor: NetworkMonitor

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "POST_NOTIFICATIONS permission granted.")
        } else {
            Log.w("MainActivity", "POST_NOTIFICATIONS permission denied.")

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called. Current app locale: ${this.resources.configuration.locales[0]}, System locale: ${Configuration(Resources.getSystem().configuration).locales[0]}")

        // Use the singleton NetworkMonitor from AppModules
        networkMonitor = AppModules.provideNetworkMonitor(applicationContext)
        requestNotificationPermission()

        val prefs = AppModules.provideUserPrefs(this)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            Log.d("MainActivity", "setContent recomposing. Current composable locale: ${androidx.compose.ui.platform.LocalConfiguration.current.locales[0]}")

            val settingsVm: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(prefs))
            val settings by settingsVm.settings.collectAsState(initial = null)

            val dark = when (settings?.theme ?: "system") {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            val isOnline by networkMonitor.isOnline.collectAsState()

            MzansiTheme(darkTheme = dark) {
                Column {
                    if (!isOnline) {
                        OfflineBanner()
                    }
                    val nav = rememberNavController()
                    AppNavHost(nav)
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API level 33
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("MainActivity", "POST_NOTIFICATIONS permission already granted.")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Log.i("MainActivity", "Showing rationale for POST_NOTIFICATIONS permission.")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    Log.i("MainActivity", "Requesting POST_NOTIFICATIONS permission.")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d("MainActivity", "onConfigurationChanged. New locale: ${newConfig.locales[0]}")
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}

@Composable
fun OfflineBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.offline_message), 
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}
