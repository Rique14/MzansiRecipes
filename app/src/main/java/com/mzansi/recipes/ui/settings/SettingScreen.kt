package com.mzansi.recipes.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mzansi.recipes.di.AppModules
import com.mzansi.recipes.navigation.Routes
import com.mzansi.recipes.ViewModel.SettingsViewModel
import com.mzansi.recipes.ViewModel.SettingsViewModelFactory
import com.mzansi.recipes.data.preferences.SettingsState
import com.mzansi.recipes.ui.common.MzansiBottomNavigationBar
import com.mzansi.recipes.util.LocaleHelper
import com.mzansi.recipes.util.restartApp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(nav: NavController) {
    val prefs = AppModules.provideUserPrefs(nav.context)
    val vm: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(prefs))
    val state by vm.settings.collectAsState(initial = SettingsState())

    var themeExpanded by remember { mutableStateOf(false) }
    var languageExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Settings",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                windowInsets = WindowInsets.safeDrawing
            )
        },
        bottomBar = { MzansiBottomNavigationBar(navController = nav) }
    ) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "GENERAL",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Profile
            SettingItemRow(
                icon = Icons.Filled.Person,
                title = "Profile",
                onClick = { nav.navigate(Routes.Profile) },
                trailingContent = {
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Navigate to Profile")
                }
            )

            // Notifications
            SettingItemRow(
                icon = Icons.Filled.Notifications,
                title = "Notifications",
                trailingContent = {
                    Switch(
                        checked = state.notificationsEnabled,
                        onCheckedChange = { vm.setNotifications(it) },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            )

            // Themes
            SettingItemRow(
                icon = Icons.Filled.Palette,
                title = "Themes",
                modifier = Modifier.clickable { themeExpanded = true },
                trailingContent = {
                    Box {
                        Text(
                            text = state.theme.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                            },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        DropdownMenu(
                            expanded = themeExpanded,
                            onDismissRequest = { themeExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("System") },
                                onClick = { vm.setTheme("system"); themeExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Light") },
                                onClick = { vm.setTheme("light"); themeExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Dark") },
                                onClick = { vm.setTheme("dark"); themeExpanded = false }
                            )
                        }
                    }
                }
            )

            // Language
            SettingItemRow(
                icon = Icons.Filled.Language,
                title = "Language",
                modifier = Modifier.clickable { languageExpanded = true },
                trailingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Text(
                            text = if (state.language == "zu") "Zulu" else "English",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        DropdownMenu(
                            expanded = languageExpanded,
                            onDismissRequest = { languageExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("English") },
                                onClick = {
                                    vm.setLanguage("en")
                                    val ctx = nav.context
                                    LocaleHelper.setLocale(ctx, "en")
                                    languageExpanded = false
                                    restartApp(ctx)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Zulu") },
                                onClick = {
                                    vm.setLanguage("zu")
                                    val ctx = nav.context
                                    LocaleHelper.setLocale(ctx, "zu")
                                    languageExpanded = false
                                    restartApp(ctx)
                                }
                            )
                        }
                    }
                }
            )

            // Logout
            SettingItemRow(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = "Logout",
                onClick = {
                    AppModules.provideAuth().signOut()
                    nav.navigate(Routes.Login) { popUpTo(Routes.Home) { inclusive = true } }
                },
                trailingContent = {
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Logout")
                }
            )
        }
    }
}

@Composable
private fun SettingItemRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable RowScope.() -> Unit)? = null
) {
    val baseModifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)

    val finalModifier = if (onClick != null && trailingContent == null) {
        baseModifier.clickable(onClick = onClick)
    } else if (onClick != null && trailingContent != null && title != "Notifications") {
        baseModifier.clickable(onClick = onClick)
    } else {
        baseModifier
    }

    Row(
        modifier = finalModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (trailingContent != null) {
            trailingContent()
        }
    }
}
