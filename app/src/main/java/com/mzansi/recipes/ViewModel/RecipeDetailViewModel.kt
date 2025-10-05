package com.mzansi.recipes.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mzansi.recipes.data.repo.RecipeRepository
import com.mzansi.recipes.data.repo.ShoppingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.mzansi.recipes.data.repo.RecipeFullDetails

data class RecipeDetailUiState(
    val details: RecipeFullDetails? = null,
    val loading: Boolean = true,
    val error: String? = null,
    val ingredientSelection: Map<String, Boolean> = emptyMap(),
    val isSavedOffline: Boolean = false,
    val ingredientsAddedToCart: Boolean = false
)

class RecipeDetailViewModel(
    private val recipeRepo: RecipeRepository,
    private val shoppingRepo: ShoppingRepository,
    private val recipeId: String
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeDetailUiState())
    val state: StateFlow<RecipeDetailUiState> = _state.asStateFlow()

    init {
        loadRecipeDetails()
        checkIfSaved()
    }

    private fun loadRecipeDetails() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            try {
                val details = recipeRepo.fetchAndCacheFullRecipeDetails(recipeId)
                _state.update { currentState ->
                    val initialSelection = details?.ingredients?.associateWith { true } ?: emptyMap()
                    currentState.copy(
                        loading = false,
                        details = details,
                        ingredientSelection = initialSelection
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = "Failed to load recipe details: ${e.message}") }
            }
        }
    }

    private fun checkIfSaved() {
        viewModelScope.launch {
            val isSaved = recipeRepo.isRecipeSavedOffline(recipeId)
            _state.update { it.copy(isSavedOffline = isSaved) }
        }
    }

    fun toggleSaveRecipe() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState.isSavedOffline) {
                recipeRepo.removeRecipeFromOffline(recipeId)
                _state.update { it.copy(isSavedOffline = false) }
            } else {
                recipeRepo.saveRecipeForOffline(recipeId)
                _state.update { it.copy(isSavedOffline = true) }
            }
        }
    }

    fun toggleIngredientSelection(ingredient: String) {
        _state.update { currentState ->
            val updatedSelection = currentState.ingredientSelection.toMutableMap()
            updatedSelection[ingredient] = !(updatedSelection[ingredient] ?: true)
            currentState.copy(ingredientSelection = updatedSelection)
        }
    }

    fun addAllIngredientsToShopping() {
        viewModelScope.launch {
            val selectedIngredients = _state.value.ingredientSelection
                .filter { it.value }
                .keys
                .toList()

            if (selectedIngredients.isNotEmpty()) {
                shoppingRepo.addItems(selectedIngredients)
                _state.update { it.copy(ingredientsAddedToCart = true) }
            }
        }
    }

    fun onAddedToCartHandled() {
        _state.update { it.copy(ingredientsAddedToCart = false) }
    }
}