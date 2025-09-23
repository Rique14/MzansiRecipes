package com.mzansi.recipes.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mzansi.recipes.data.api.CategorySummary
import com.mzansi.recipes.data.db.RecipeEntity
import com.mzansi.recipes.data.repo.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DisplayMode {
    TRENDING_ONLY, // Initial state, or when no search/category is active
    SEARCH_RESULTS,
    CATEGORY_RECIPES
}

data class HomeState(
    val trendingRecipes: List<RecipeEntity> = emptyList(),
    val categories: List<CategorySummary> = emptyList(),
    val searchResults: List<RecipeEntity> = emptyList(),
    val recipesForCategory: List<RecipeEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryName: String? = null,
    val activeDisplayMode: DisplayMode = DisplayMode.TRENDING_ONLY,
    val isLoadingTrending: Boolean = false,
    val isLoadingCategories: Boolean = false,
    val isLoadingSearchResults: Boolean = false,
    val isLoadingRecipesForCategory: Boolean = false,
    val error: String? = null
)

class RecipeViewModel(private val repo: RecipeRepository) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        loadTrendingRecipes()
        loadCategories()
    }

    fun loadTrendingRecipes() = viewModelScope.launch {
        _state.update { it.copy(isLoadingTrending = true, error = null) }
        try {
            val trending = repo.refreshTrending()
            _state.update { it.copy(isLoadingTrending = false, trendingRecipes = trending) }
        } catch (e: Exception) {
            _state.update { it.copy(isLoadingTrending = false, error = e.message) }
        }
    }

    private fun loadCategories() = viewModelScope.launch {
        _state.update { it.copy(isLoadingCategories = true, error = null) }
        try {
            val categories = repo.getCategories()
            _state.update { it.copy(isLoadingCategories = false, categories = categories) }
        } catch (e: Exception) {
            _state.update { it.copy(isLoadingCategories = false, error = e.message) }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.isNotBlank() && query.length > 2) {
            performSearch()
        } else {
            // Clear search results and revert display mode if query is too short or blank
            _state.update {
                it.copy(
                    searchResults = emptyList(),
                    activeDisplayMode = DisplayMode.TRENDING_ONLY,
                    selectedCategoryName = null, // also clear category selection
                    recipesForCategory = emptyList()
                )
            }
        }
    }

    fun performSearch() = viewModelScope.launch {
        if (_state.value.searchQuery.isBlank()) {
             // Should not happen if called from onSearchQueryChanged, but as a safeguard
            _state.update { it.copy(searchResults = emptyList(), activeDisplayMode = DisplayMode.TRENDING_ONLY) }
            return@launch
        }
        _state.update {
            it.copy(
                isLoadingSearchResults = true,
                error = null,
                activeDisplayMode = DisplayMode.SEARCH_RESULTS,
                selectedCategoryName = null, // Clear category selection
                recipesForCategory = emptyList() // Clear category recipes
            )
        }
        try {
            val results = repo.searchRecipesByName(_state.value.searchQuery)
            _state.update {
                it.copy(isLoadingSearchResults = false, searchResults = results)
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(isLoadingSearchResults = false, error = e.message, searchResults = emptyList())
            }
        }
    }

    fun loadRecipesForCategory(categoryName: String) = viewModelScope.launch {
        _state.update {
            it.copy(
                isLoadingRecipesForCategory = true,
                error = null,
                recipesForCategory = emptyList(),
                selectedCategoryName = categoryName,
                activeDisplayMode = DisplayMode.CATEGORY_RECIPES,
                searchQuery = "", // Clear search query
                searchResults = emptyList() // Clear search results
            )
        }
        try {
            val recipes = repo.getRecipesByCategory(categoryName)
            _state.update {
                it.copy(isLoadingRecipesForCategory = false, recipesForCategory = recipes)
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(isLoadingRecipesForCategory = false, error = e.message, recipesForCategory = emptyList())
            }
        }
    }

    // Call this if user explicitly clears search or category selection
    fun showTrendingOnly(){
        _state.update {
            it.copy(
                searchQuery = "",
                searchResults = emptyList(),
                selectedCategoryName = null,
                recipesForCategory = emptyList(),
                activeDisplayMode = DisplayMode.TRENDING_ONLY
            )
        }
    }
}