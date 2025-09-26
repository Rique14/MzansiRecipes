package com.mzansi.recipes.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mzansi.recipes.ui.auth.ForgotPasswordScreen
import com.mzansi.recipes.ui.auth.LoginScreen
import com.mzansi.recipes.ui.auth.RegisterScreen
import com.mzansi.recipes.ui.community.CommunityScreen
import com.mzansi.recipes.ui.home.HomeScreen
import com.mzansi.recipes.ui.settings.SettingsScreen
import com.mzansi.recipes.ui.settings.ProfileScreen
import com.mzansi.recipes.ui.shopping.ShoppingScreen
import com.mzansi.recipes.ui.recipe.RecipeDetailScreen
import com.mzansi.recipes.ui.userpost.UserPostDetailScreen // Added import
import com.mzansi.recipes.ui.saved.SavedRecipesScreen // <<< ADDED IMPORT

object Routes {
    const val Login = "login"
    const val Register = "register"
    const val Forgot = "forgot"
    const val Home = "home"
    const val RecipeDetail = "recipe/{id}/{title}"
    fun recipeDetail(id: String, title: String) = "recipe/$id/$title"
    const val UserPostDetail = "user_post_detail/{postId}" // New route constant
    fun userPostDetail(postId: String) = "user_post_detail/$postId" // New route function
    const val Shopping = "shopping"
    const val Community = "community"
    const val Settings = "settings"
    const val Profile = "profile"
    const val SavedRecipes = "saved_recipes" // <<< ADDED ROUTE
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
        composable(Routes.SavedRecipes) { SavedRecipesScreen(nav) } // <<< ADDED COMPOSABLE
        composable(
            route = Routes.RecipeDetail,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStack ->
            val id = backStack.arguments?.getString("id") ?: ""
            val title = backStack.arguments?.getString("title") ?: ""
            RecipeDetailScreen(nav, id, title)
        }
        composable(
            route = Routes.UserPostDetail, // New composable entry
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            UserPostDetailScreen(navController = nav, postId = postId)
        }
    }
}