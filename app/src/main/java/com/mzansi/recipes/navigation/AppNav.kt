package com.mzansi.recipes.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mzansi.recipes.ui.auth.ForgotPasswordScreen
import com.mzansi.recipes.ui.auth.LoginScreen
import com.mzansi.recipes.ui.auth.RegisterScreen
import com.mzansi.recipes.ui.community.CommunityScreen
import com.mzansi.recipes.ui.home.HomeScreen
import com.mzansi.recipes.ui.settings.SettingsScreen
import com.mzansi.recipes.ui.settings.ProfileScreen
import com.mzansi.recipes.ui.settings.EditProfileScreen
import com.mzansi.recipes.ui.shopping.ShoppingScreen
import com.mzansi.recipes.ui.recipe.RecipeDetailScreen

object Routes {
    const val Login = "login"
    const val Register = "register"
    const val Forgot = "forgot"
    const val Home = "home"
    const val RecipeDetail = "recipe/{id}"
    const val Shopping = "shopping"
    const val Community = "community"
    const val Settings = "settings"
    const val Profile = "profile"
    const val EditProfile = "edit_profile"
}

@Composable
fun AppNavHost(nav: NavHostController) {
    NavHost(navController = nav, startDestination = Routes.Login) {
        composable(Routes.Login) { LoginScreen(nav) }
        composable(Routes.Register) { RegisterScreen(nav) }
        composable(Routes.Forgot) { ForgotPasswordScreen(nav) }
        composable(Routes.Home) { HomeScreen(nav) }
        composable(Routes.Shopping) { ShoppingScreen(nav) }
        composable(Routes.Community) { CommunityScreen(nav) }
        composable(Routes.Settings) { SettingsScreen(nav) }
        composable(Routes.Profile) { ProfileScreen(nav) }
        composable(Routes.EditProfile) { EditProfileScreen(nav) }
        composable(Routes.RecipeDetail) { backStack ->
            val id = backStack.arguments?.getString("id") ?: ""
            RecipeDetailScreen(nav, id)
        }
    }
}