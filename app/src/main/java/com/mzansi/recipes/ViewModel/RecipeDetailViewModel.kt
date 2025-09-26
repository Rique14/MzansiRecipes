package com.mzansi.recipes.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mzansi.recipes.data.repo.RecipeRepository
import com.mzansi.recipes.data.repo.ShoppingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update // Required for _state.update
import kotlinx.coroutines.launch

data class RecipeDetailState(
    val loading: Boolean = false,
    val error: String? = null,
    val details: com.mzansi.recipes.data.repo.RecipeFullDetails? = null,
    val addedToShopping: Boolean = false,
    val ingredientSelection: Map<String, Boolean> = emptyMap(), // To track selected ingredients
    val isSavedOffline: Boolean = false // <<< NEW FIELD
)

class RecipeDetailViewModel(
    private val recipeRepo: RecipeRepository,
    private val shoppingRepo: ShoppingRepository,
    private val recipeId: String // recipeId is passed from factory
) : ViewModel() {
    private val TAG = "RecipeDetailVM" // Logging Tag

    private val _state = MutableStateFlow(RecipeDetailState())
    val state = _state.asStateFlow()

    init {
        Log.d(TAG, "Initializing for recipeId: $recipeId")
        loadRecipeDetails(recipeId)
        observeRecipeSavedStatus(recipeId) // <<< NEW CALL
    }

    private fun loadRecipeDetails(id: String) = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null, details = null, ingredientSelection = emptyMap()) } 
        Log.d(TAG, "Loading details for recipeId: $id")
        try {
            // Fetch and cache full details, this will also update/create the RecipeEntity in DB
            val detail = recipeRepo.fetchAndCacheFullRecipeDetails(id)
            Log.d(TAG, "RecipeDetailViewModel: Received details from repo: Title: ${detail?.title}, Area: ${detail?.area}, Category: ${detail?.category}")

            val initialSelection = detail?.ingredients?.associateWith { false } ?: emptyMap()
            _state.update {
                Log.d(TAG, "RecipeDetailViewModel: Updating state with Area: ${detail?.area}, Category: ${detail?.category}")
                it.copy(
                    loading = false, // Loading for main details is done
                    details = detail,
                    ingredientSelection = initialSelection
                    // isSavedOffline will be updated by observeRecipeSavedStatus
                )
            }
            Log.d(TAG, "Details loaded: ${detail?.title}, Ingredients count: ${detail?.ingredients?.size}")
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    loading = false,
                    error = e.message ?: "An unknown error occurred"
                )
            }
            Log.e(TAG, "Error loading details: ${e.message}", e)
        }
    }

    // <<< NEW FUNCTION to observe saved status >>>
    private fun observeRecipeSavedStatus(id: String) = viewModelScope.launch {
        recipeRepo.observeRecipeById(id)
            .catch { e ->
                Log.e(TAG, "Error observing recipe saved status for $id: ${e.message}", e)
                // Optionally update state with an error specific to saved status loading
            }
            .collectLatest { recipeEntity ->
                if (recipeEntity != null) {
                    _state.update { it.copy(isSavedOffline = recipeEntity.isSavedOffline) }
                    Log.d(TAG, "Recipe $id saved status updated to: ${recipeEntity.isSavedOffline}")
                } else {
                    // This case might happen if the recipe is deleted or not yet created by fetchAndCacheFullRecipeDetails
                    // For now, assume it means not saved if entity is null after initial load attempt.
                    _state.update { it.copy(isSavedOffline = false) }
                     Log.d(TAG, "Recipe $id entity not found for saved status observation, assuming not saved.")
                }
            }
    }


    fun toggleIngredientSelection(ingredientName: String) {
        val currentSelection = _state.value.ingredientSelection.toMutableMap()
        val currentIsSelected = currentSelection[ingredientName] ?: false
        currentSelection[ingredientName] = !currentIsSelected // Toggle the selection
        _state.update { it.copy(ingredientSelection = currentSelection) }
        Log.d(TAG, "Toggled selection for '$ingredientName' to ${!currentIsSelected}")
    }

    fun addAllIngredientsToShopping() = viewModelScope.launch {
        Log.d(TAG, "addAllIngredientsToShopping called for SELECTED items")
        val currentDetails = _state.value.details
        val currentSelections = _state.value.ingredientSelection

        if (currentDetails == null) {
            Log.d(TAG, "Details are null. Cannot add ingredients.")
            return@launch
        }

        val selectedIngredients = currentDetails.ingredients.filter { ingredientName ->
            currentSelections[ingredientName] == true
        }

        if (selectedIngredients.isEmpty()) {
            Log.d(TAG, "No ingredients selected for recipe: ${currentDetails.title}.")
            _state.update { it.copy(addedToShopping = false) } // Indicate nothing was added or use a new state var
            return@launch
        }

        Log.d(TAG, "Attempting to add ${selectedIngredients.size} SELECTED ingredients for recipe: ${currentDetails.title}")
        selectedIngredients.forEach { item ->
            Log.d(TAG, "Adding SELECTED item to shopping list: '$item' from recipeId: ${currentDetails.id}")
            try {
                shoppingRepo.addItem(name = item, originRecipeId = currentDetails.id)
                Log.d(TAG, "Successfully called shoppingRepo.addItem for SELECTED: '$item'")
            } catch (e: Exception) {
                Log.e(TAG, "Error calling shoppingRepo.addItem for SELECTED '$item': ${e.message}", e)
            }
        }
        _state.update {
            it.copy(
                addedToShopping = true
            )
        }
        Log.d(TAG, "Set addedToShopping = true for SELECTED items")
    }

    // <<< NEW FUNCTION >>>
    fun toggleSaveRecipe() = viewModelScope.launch {
        val currentRecipeId = recipeId // from constructor
        val currentSavedStatus = _state.value.isSavedOffline
        Log.d(TAG, "toggleSaveRecipe called for $currentRecipeId. Current saved status: $currentSavedStatus")
        try {
            if (currentSavedStatus) {
                recipeRepo.unsaveRecipe(currentRecipeId)
                Log.d(TAG, "Unsaving recipe $currentRecipeId")
            } else {
                if (_state.value.details == null) {
                     Log.w(TAG, "Attempting to save recipe $currentRecipeId before its details are loaded. Details should be loaded by now via init.")
                    // It's unexpected for details to be null here if loadRecipeDetails was called in init.
                    // However, the saveRecipeForOffline in repo will call fetchAndCacheFullRecipeDetails again if needed.
                }
                recipeRepo.saveRecipeForOffline(currentRecipeId)
                Log.d(TAG, "Saving recipe $currentRecipeId for offline.")
            }
            // The state.isSavedOffline will automatically update due to the observeRecipeSavedStatus flow.
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling save state for recipe $currentRecipeId: ${e.message}", e)
            _state.update { it.copy(error = "Failed to update save status: ${e.message}") }
        }
    }
}