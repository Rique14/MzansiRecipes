package com.mzansi.recipes.ui.common

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mzansi.recipes.R // Import R class for string resources
import com.mzansi.recipes.navigation.Routes

// Changed label to labelResId to hold the resource ID
data class BottomNavItem(
    val labelResId: Int,
    val icon: ImageVector,
    val route: String
)

@Composable
fun MzansiBottomNavigationBar(navController: NavController) {
    val currentConfig = LocalConfiguration.current
    Log.d("BottomNav", "Recomposing. Current config locale: ${currentConfig.locales[0]}")
    Log.d("BottomNav", "Home label from stringResource: ${stringResource(id = R.string.bottom_nav_home)}")
    Log.d("BottomNav", "Community label from stringResource: ${stringResource(id = R.string.bottom_nav_community)}")
    Log.d("BottomNav", "Shop label from stringResource: ${stringResource(id = R.string.bottom_nav_shop)}")
    Log.d("BottomNav", "Settings label from stringResource: ${stringResource(id = R.string.bottom_nav_settings)}")

    val navItems = listOf(
        BottomNavItem(labelResId = R.string.bottom_nav_home, icon = Icons.Default.Home, route = Routes.Home),
        BottomNavItem(labelResId = R.string.bottom_nav_community, icon = Icons.Default.Favorite, route = Routes.Community),
        BottomNavItem(labelResId = R.string.bottom_nav_shop, icon = Icons.Default.ShoppingCart, route = Routes.Shopping),
        BottomNavItem(labelResId = R.string.bottom_nav_settings, icon = Icons.Default.Settings, route = Routes.Settings)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        navItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(Routes.Home) {
                            // saveState = true // Optional
                        }
                        launchSingleTop = true
                        // restoreState = true // Optional
                    }
                },
                icon = { Icon(item.icon, contentDescription = stringResource(id = item.labelResId)) },
                label = { Text(stringResource(id = item.labelResId)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}
