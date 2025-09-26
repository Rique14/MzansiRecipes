package com.mzansi.recipes.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mzansi.recipes.data.db.RecipeEntity
import com.mzansi.recipes.data.repo.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SavedRecipesUiState(
    val savedRecipes: List<RecipeEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SavedRecipesViewModel(private val recipeRepository: RecipeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedRecipesUiState())
    val uiState: StateFlow<SavedRecipesUiState> = _uiState.asStateFlow()

    init {
        observeSavedRecipes()
    }

    private fun observeSavedRecipes() {
        viewModelScope.launch {
            recipeRepository.observeSavedRecipes()
                .onStart { _uiState.update { it.copy(isLoading = true, error = null) } }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load saved recipes: ${e.message}") }
                }
                .collectLatest { recipes ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            savedRecipes = recipes
                        )
                    }
                }
        }
    }
}