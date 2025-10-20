package com.mzansi.recipes.ui.settings

import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mzansi.recipes.R
import com.mzansi.recipes.di.AppModules
import com.mzansi.recipes.navigation.Routes
import com.mzansi.recipes.ViewModel.SettingsViewModel
import com.mzansi.recipes.ViewModel.SettingsViewModelFactory
import com.mzansi.recipes.data.preferences.SettingsState
import com.mzansi.recipes.ui.common.MzansiBottomNavigationBar
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
                        stringResource(id = R.string.settings),
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
                text = stringResource(id = R.string.general),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Profile
            SettingItemRow(
                icon = Icons.Filled.Person,
                title = stringResource(id = R.string.profile),
                onClick = { nav.navigate(Routes.Profile) },
                trailingContent = {
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = stringResource(id = R.string.profile))
                }
            )

            // Notifications
            SettingItemRow(
                icon = Icons.Filled.Notifications,
                title = stringResource(id = R.string.notifications),
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
                title = stringResource(id = R.string.themes),
                modifier = Modifier.clickable { themeExpanded = true },
                trailingContent = {
                    Box {
                        Text(
                            text = state.theme.replaceFirstChar { it.titlecase(Locale.getDefault()) },
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
                title = stringResource(id = R.string.language),
                modifier = Modifier.clickable { languageExpanded = true },
                trailingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Text(
                            text = when (state.language) {
                                "zu" -> "isiZulu"
                                "af" -> "Afrikaans"
                                "es" -> "Español"
                                "fr" -> "Français"
                                "de" -> "Deutsch"
                                else -> "English"
                            },
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
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                                    languageExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("isiZulu") },
                                onClick = {
                                    vm.setLanguage("zu")
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("zu"))
                                    languageExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Afrikaans") },
                                onClick = {
                                    vm.setLanguage("af")
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("af"))
                                    languageExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Español") },
                                onClick = {
                                    vm.setLanguage("es")
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es"))
                                    languageExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Français") },
                                onClick = {
                                    vm.setLanguage("fr")
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("fr"))
                                    languageExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Deutsch") },
                                onClick = {
                                    vm.setLanguage("de")
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("de"))
                                    languageExpanded = false
                                }
                            )
                        }
                    }
                }
            )

            // Logout
            SettingItemRow(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = stringResource(id = R.string.logout),
                onClick = {
                    AppModules.provideAuth().signOut()
                    nav.navigate(Routes.Login) { popUpTo(Routes.Home) { inclusive = true } }
                },
                trailingContent = {
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = stringResource(id = R.string.logout))
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
    } else if (onClick != null && trailingContent != null && title != stringResource(id = R.string.notifications)) {
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
