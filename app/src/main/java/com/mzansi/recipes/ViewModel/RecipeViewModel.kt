package com.mzansi.recipes.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mzansi.recipes.data.api.TastyRecipeDetail
import com.mzansi.recipes.data.api.TastyService
import com.mzansi.recipes.data.repo.ShoppingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecipeDetailUi(
    val id: String = "",
    val title: String = "",
    val imageUrl: String? = null,
    val readyInText: String? = null,
    val serves: Int? = null,
    val ingredients: List<String> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val addedToShopping: Boolean = false
)

class RecipeViewModel(
    private val tasty: TastyService,
    private val shoppingRepo: ShoppingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeDetailUi())
    val state = _state.asStateFlow()

    fun load(id: String) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null, addedToShopping = false)
        try {
            val detail = tasty.getRecipeDetail(id)
            _state.value = _state.value.copy(
                loading = false,
                id = id,
                title = detail.name ?: "Recipe",
                imageUrl = detail.thumbnailUrl,
                readyInText = detail.cook_time_minutes?.let { "Ready in: $it min" },
                serves = detail.num_servings,
                ingredients = extractIngredients(detail)
            )
        } catch (e: Exception) {
            // Fallback to a static list (matches your mock) so the screen still works offline or if API fails
            _state.value = _state.value.copy(
                loading = false,
                id = id,
                title = "Chicken Curry",
                readyInText = "Ready in: 60 min",
                serves = 4,
                ingredients = listOf(
                    "6 pieces chicken",
                    "Potato, cubed",
                    "2 teaspoons curry powder",
                    "1 1/2 Cup rice",
                    "2 teaspoons salt",
                    "1/2 teaspoon ginger",
                    "1/2 teaspoon turmeric",
                    "1 onion, chopped",
                    "1 1/2 teaspoons garlic",
                    "3 ripe tomatoes, chopped"
                ),
                error = e.message
            )
        }
    }

    fun addAllIngredientsToShopping() = viewModelScope.launch {
        val s = _state.value
        if (s.ingredients.isEmpty() || s.id.isBlank()) return@launch
        s.ingredients.forEach { item ->
            shoppingRepo.addItem(name = item, originRecipeId = s.id)
        }
        _state.value = _state.value.copy(addedToShopping = true)
    }

    private fun extractIngredients(detail: TastyRecipeDetail): List<String> {
        // Tasty detail structure: sections -> components -> raw_text
        val list = detail.sections
            ?.flatMap { it.components.orEmpty() }
            ?.mapNotNull { it.raw_text?.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty()
        return if (list.isNotEmpty()) list else emptyList()
    }
}