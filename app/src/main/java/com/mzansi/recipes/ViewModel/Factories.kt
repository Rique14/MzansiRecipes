package com.mzansi.recipes.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mzansi.recipes.data.preferences.UserPreferences
import com.mzansi.recipes.data.repo.AuthRepository
import com.mzansi.recipes.data.repo.CommunityRepository
import com.mzansi.recipes.data.repo.RecipeRepository
import com.mzansi.recipes.data.repo.ShoppingRepository

class AuthViewModelFactory(
    private val repo: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(repo) as T
    }
}

class RecipeViewModelFactory(private val repo: RecipeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for RecipeViewModel")
    }
}

class RecipeDetailViewModelFactory(
    private val recipeRepo: RecipeRepository,
    private val shoppingRepo: ShoppingRepository,
    private val recipeId: String,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeDetailViewModel(recipeRepo, shoppingRepo, recipeId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for RecipeDetailViewModel")
    }
}

class ShoppingViewModelFactory(
    private val repo: ShoppingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShoppingViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for ShoppingViewModel")
    }
}

class CommunityViewModelFactory(
    private val communityRepo: CommunityRepository,
    private val recipeRepo: RecipeRepository // Added RecipeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommunityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CommunityViewModel(communityRepo, recipeRepo) as T // Pass recipeRepo
        }
        throw IllegalArgumentException("Unknown ViewModel class for CommunityViewModel")
    }
}

class SettingsViewModelFactory(
    private val prefs: UserPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for SettingsViewModel")
    }
}
