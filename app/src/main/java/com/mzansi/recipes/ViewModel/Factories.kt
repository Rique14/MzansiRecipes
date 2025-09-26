package com.mzansi.recipes.ViewModel

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.mzansi.recipes.data.preferences.UserPreferences
import com.mzansi.recipes.data.repo.AuthRepository
import com.mzansi.recipes.data.repo.CommunityRepository
import com.mzansi.recipes.data.repo.RecipeRepository
import com.mzansi.recipes.data.repo.ShoppingRepository

// Factory for AuthViewModel
class AuthViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for AuthViewModelFactory")
    }
}

// Factory for RecipeViewModel
class RecipeViewModelFactory(private val repo: RecipeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Factory for RecipeDetailViewModel
class RecipeDetailViewModelFactory(
    private val recipeRepo: RecipeRepository,
    private val shoppingRepo: ShoppingRepository,
    private val recipeId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeDetailViewModel(recipeRepo, shoppingRepo, recipeId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Factory for ShoppingViewModel
class ShoppingViewModelFactory(private val repo: ShoppingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShoppingViewModel(repo) as T 
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Factory for CommunityViewModel
class CommunityViewModelFactory(
    private val communityRepo: CommunityRepository,
    private val recipeRepo: RecipeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommunityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CommunityViewModel(communityRepo, recipeRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Factory for UserPostDetailViewModel (Using CreationExtras)
class UserPostDetailViewModelFactory(
    private val communityRepository: CommunityRepository
) : AbstractSavedStateViewModelFactory() {
    override fun <T : ViewModel> create( 
        key: String, // key is still here from the AbstractSavedStateViewModelFactory's create method we override
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(UserPostDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserPostDetailViewModel(handle, communityRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for UserPostDetailViewModelFactory")
    }
}

// Factory for SettingsViewModel
class SettingsViewModelFactory(private val userPrefs: UserPreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(userPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

// <<< NEW FACTORY >>>
// Factory for SavedRecipesViewModel
class SavedRecipesViewModelFactory(private val recipeRepository: RecipeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SavedRecipesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SavedRecipesViewModel(recipeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for SavedRecipesViewModelFactory")
    }
}