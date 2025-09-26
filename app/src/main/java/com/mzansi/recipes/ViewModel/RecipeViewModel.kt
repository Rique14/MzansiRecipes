package com.mzansi.recipes.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mzansi.recipes.data.db.CategoryEntity // Changed from CategorySummary
import com.mzansi.recipes.data.db.RecipeEntity
import com.mzansi.recipes.data.repo.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DisplayMode {
    TRENDING_ONLY,
    SEARCH_RESULTS,
    CATEGORY_RECIPES
}

data class HomeState(
    val trendingRecipes: List<RecipeEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val searchResults: List<RecipeEntity> = emptyList(),
    val recipesForCategory: List<RecipeEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryName: String? = null,
    val activeDisplayMode: DisplayMode = DisplayMode.TRENDING_ONLY,
    val isLoadingTrendingRefresh: Boolean = false,
    val isLoadingCategoriesRefresh: Boolean = false,
    val isLoadingSearchResults: Boolean = false,
    val isLoadingCategoryRecipesRefresh: Boolean = false,
    val error: String? = null
)

class RecipeViewModel(private val repo: RecipeRepository) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        observeCategories()
        observeTrendingRecipes()

        // Initial refresh attempts
        refreshCategories(isInitial = true)
        refreshTrendingRecipes(isInitial = true)
    }

    private fun observeCategories() {
        viewModelScope.launch {
            repo.observeCategories()
                .catch { e -> _state.update { it.copy(error = "Error observing categories: ${e.message}") } }
                .collectLatest { categories ->
                    _state.update { it.copy(categories = categories) }
                }
        }
    }

    fun refreshCategories(isInitial: Boolean = false) {
        viewModelScope.launch {
            if (isInitial) _state.update { it.copy(isLoadingCategoriesRefresh = true, error = null) }
            try {
                repo.refreshCategoriesIfNeeded() // Fetches from network if cache is empty & online
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to refresh categories: ${e.message}") }
            } finally {
                if (isInitial) _state.update { it.copy(isLoadingCategoriesRefresh = false) }
            }
        }
    }

    private fun observeTrendingRecipes() {
        viewModelScope.launch {
            repo.observeTrendingRecipes()
                .catch { e -> _state.update { it.copy(error = "Error observing trending recipes: ${e.message}") } }
                .collectLatest { trending ->
                    _state.update { it.copy(trendingRecipes = trending) }
                }
        }
    }

    fun refreshTrendingRecipes(isInitial: Boolean = false) {
        viewModelScope.launch {
            if (isInitial) _state.update { it.copy(isLoadingTrendingRefresh = true, error = null) }
            try {
                repo.triggerTrendingRecipesRefresh()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to refresh trending recipes: ${e.message}") }
            } finally {
                if (isInitial) _state.update { it.copy(isLoadingTrendingRefresh = false) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.isNotBlank() && query.length > 2) {
            performSearch()
        } else {
            _state.update {
                it.copy(
                    searchResults = emptyList(),
                    activeDisplayMode = if (it.selectedCategoryName != null) DisplayMode.CATEGORY_RECIPES else DisplayMode.TRENDING_ONLY
                )
            }
        }
    }

    fun performSearch() = viewModelScope.launch {
        if (_state.value.searchQuery.isBlank()) return@launch
        _state.update {
            it.copy(
                isLoadingSearchResults = true,
                error = null,
                activeDisplayMode = DisplayMode.SEARCH_RESULTS
            )
        }
        try {
            val results = repo.searchRecipesByName(_state.value.searchQuery)
            _state.update { it.copy(isLoadingSearchResults = false, searchResults = results) }
        } catch (e: Exception) {
            _state.update { it.copy(isLoadingSearchResults = false, error = e.message, searchResults = emptyList()) }
        }
    }

    fun loadRecipesForCategory(categoryName: String) {
        _state.update {
            it.copy(
                selectedCategoryName = categoryName,
                activeDisplayMode = DisplayMode.CATEGORY_RECIPES,
                searchQuery = "",
                searchResults = emptyList(),
                isLoadingCategoryRecipesRefresh = true, // For the initial refresh call
                error = null
            )
        }

        viewModelScope.launch {
            // Start observing recipes for this category from the database
            repo.observeRecipesByCategory(categoryName)
                .catch { e -> _state.update { it.copy(error = "Error observing $categoryName recipes: ${e.message}", isLoadingCategoryRecipesRefresh = false) } }
                .collectLatest { recipes ->
                    _state.update { it.copy(recipesForCategory = recipes, isLoadingCategoryRecipesRefresh = false) } // Stop loading once data flows
                }
        }
        // Trigger a network refresh for this category
        viewModelScope.launch {
            try {
                repo.refreshRecipesForCategory(categoryName)
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to refresh $categoryName recipes: ${e.message}", isLoadingCategoryRecipesRefresh = false) }
            }

        }
    }

    fun showTrendingOnly() {
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