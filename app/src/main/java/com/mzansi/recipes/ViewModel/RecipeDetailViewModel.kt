package com.mzansi.recipes.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mzansi.recipes.data.repo.RecipeRepository
import com.mzansi.recipes.data.repo.ShoppingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update // Required for _state.update
import kotlinx.coroutines.launch

data class RecipeDetailState(
    val loading: Boolean = false,
    val error: String? = null,
    val details: com.mzansi.recipes.data.repo.RecipeFullDetails? = null,
    val addedToShopping: Boolean = false,
    val ingredientSelection: Map<String, Boolean> = emptyMap() // To track selected ingredients
)

class RecipeDetailViewModel(
    private val recipeRepo: RecipeRepository,
    private val shoppingRepo: ShoppingRepository,
    private val recipeId: String
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeDetailState())
    val state = _state.asStateFlow()

    init {
        Log.d("RecipeDetailVM", "Initializing for recipeId: $recipeId")
        load(recipeId)
    }

    private fun load(id: String) = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null, ingredientSelection = emptyMap()) }
        Log.d("RecipeDetailVM", "Loading details for recipeId: $id")
        try {
            val detail = recipeRepo.getFullDetail(id)
            val initialSelection = detail?.ingredients?.associateWith { false } ?: emptyMap()
            _state.update {
                it.copy(
                    loading = false,
                    details = detail,
                    ingredientSelection = initialSelection
                )
            }
            Log.d("RecipeDetailVM", "Details loaded: ${detail?.title}, Ingredients count: ${detail?.ingredients?.size}")
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    loading = false,
                    error = e.message ?: "An unknown error occurred"
                )
            }
            Log.e("RecipeDetailVM", "Error loading details: ${e.message}", e)
        }
    }

    fun toggleIngredientSelection(ingredientName: String) {
        val currentSelection = _state.value.ingredientSelection.toMutableMap()
        val currentIsSelected = currentSelection[ingredientName] ?: false
        currentSelection[ingredientName] = !currentIsSelected // Toggle the selection
        _state.update { it.copy(ingredientSelection = currentSelection) }
        Log.d("RecipeDetailVM", "Toggled selection for '$ingredientName' to ${!currentIsSelected}")
    }

    fun addAllIngredientsToShopping() = viewModelScope.launch {
        Log.d("RecipeDetailVM", "addAllIngredientsToShopping called for SELECTED items")
        val currentDetails = _state.value.details
        val currentSelections = _state.value.ingredientSelection

        if (currentDetails == null) {
            Log.d("RecipeDetailVM", "Details are null. Cannot add ingredients.")
            return@launch
        }

        val selectedIngredients = currentDetails.ingredients.filter { ingredientName ->
            currentSelections[ingredientName] == true
        }

        if (selectedIngredients.isEmpty()) {
            Log.d("RecipeDetailVM", "No ingredients selected for recipe: ${currentDetails.title}.")
            _state.update { it.copy(addedToShopping = false) } // Indicate nothing was added or use a new state var
            return@launch
        }

        Log.d("RecipeDetailVM", "Attempting to add ${selectedIngredients.size} SELECTED ingredients for recipe: ${currentDetails.title}")
        selectedIngredients.forEach { item ->
            Log.d("RecipeDetailVM", "Adding SELECTED item to shopping list: '$item' from recipeId: ${currentDetails.id}")
            try {
                shoppingRepo.addItem(name = item, originRecipeId = currentDetails.id)
                Log.d("RecipeDetailVM", "Successfully called shoppingRepo.addItem for SELECTED: '$item'")
            } catch (e: Exception) {
                Log.e("RecipeDetailVM", "Error calling shoppingRepo.addItem for SELECTED '$item': ${e.message}", e)
            }
        }
        // Consider if you want to clear selections after adding them
        // val clearedSelection = currentDetails.ingredients.associateWith { false }
        _state.update {
            it.copy(
                addedToShopping = true
                // ingredientSelection = clearedSelection // Uncomment to clear selections after adding
            )
        }
        Log.d("RecipeDetailVM", "Set addedToShopping = true for SELECTED items")
    }
}
