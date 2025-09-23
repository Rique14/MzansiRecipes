package com.mzansi.recipes.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mzansi.recipes.data.repo.RecipeRepository
import com.mzansi.recipes.data.repo.ShoppingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Renamed to reflect its purpose and match the new data structure
data class RecipeDetailState(
    val loading: Boolean = false,
    val error: String? = null,
    val details: com.mzansi.recipes.data.repo.RecipeFullDetails? = null,
    val addedToShopping: Boolean = false
)

class RecipeDetailViewModel(
    private val recipeRepo: RecipeRepository,
    private val shoppingRepo: ShoppingRepository,
    private val recipeId: String
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeDetailState())
    val state = _state.asStateFlow()

    init {
        load(recipeId)
    }

    private fun load(id: String) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        try {
            val detail = recipeRepo.getFullDetail(id)
            _state.value = _state.value.copy(
                loading = false,
                details = detail
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                loading = false,
                error = e.message ?: "An unknown error occurred"
            )
        }
    }

    fun addAllIngredientsToShopping() = viewModelScope.launch {
        val s = _state.value.details
        if (s?.ingredients?.isEmpty() != false) return@launch

        s.ingredients.forEach { item ->
            shoppingRepo.addItem(name = item, originRecipeId = s.id)
        }
        _state.value = _state.value.copy(addedToShopping = true)
    }
}
