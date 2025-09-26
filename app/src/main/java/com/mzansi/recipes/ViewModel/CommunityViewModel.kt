package com.mzansi.recipes.ViewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mzansi.recipes.data.db.CategoryEntity
import com.mzansi.recipes.data.db.RecipeEntity // Required for the combined list
import com.mzansi.recipes.data.repo.CommunityPost
import com.mzansi.recipes.data.repo.CommunityRepository
import com.mzansi.recipes.data.repo.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CommunityUiState(
    val posts: List<CommunityPost> = emptyList(), // All posts from the repository
    val filteredPosts: List<CommunityPost> = emptyList(), // Posts to display after filtering
    val categories: List<CategoryEntity> = emptyList(),
    val selectedCategoryName: String? = null,
    val loadingPosts: Boolean = false,
    val isLoadingCategoriesRefresh: Boolean = false,
    val error: String? = null
)

class CommunityViewModel(
    private val communityRepo: CommunityRepository,
    private val recipeRepo: RecipeRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CommunityUiState())
    val state: StateFlow<CommunityUiState> = _state.asStateFlow()

    init {
        observeCategories()
        loadContent()
    }

    fun loadContent() {
        // Load posts (which now includes ensuring API recipes from Beef and Chicken are present)
        loadPostsAndEnsureApiRecipes()
        // Refresh categories from the recipe repository (typically API based)
        refreshCategories(isInitial = true)
    }

    private fun applyFilter() {
        _state.update { currentState ->
            val filtered = if (currentState.selectedCategoryName == null) {
                currentState.posts
            } else {
                currentState.posts.filter { it.category == currentState.selectedCategoryName }
            }
            currentState.copy(filteredPosts = filtered)
        }
    }

    fun onCategorySelected(categoryName: String?) {
        _state.update { currentState ->
            val newSelectedCategory = if (currentState.selectedCategoryName == categoryName) {
                null // Clear filter if same category is tapped again
            } else {
                categoryName // Set new filter or switch to a different one
            }
            currentState.copy(selectedCategoryName = newSelectedCategory)
        }
        applyFilter()
    }

    private fun loadPostsAndEnsureApiRecipes() = viewModelScope.launch {
        _state.update { it.copy(loadingPosts = true, error = null) }
        try {
            // 1. Refresh and get API recipes from "Beef" and "Chicken" categories
            Log.d("CommunityVM", "Refreshing Beef and Chicken categories for featured posts")
            recipeRepo.refreshRecipesForCategory("Beef")
            recipeRepo.refreshRecipesForCategory("Chicken")
            
            val beefRecipes = recipeRepo.observeRecipesByCategory("Beef").first()
            val chickenRecipes = recipeRepo.observeRecipesByCategory("Chicken").first()
            val apiRecipes: List<RecipeEntity> = beefRecipes + chickenRecipes
            Log.d("CommunityVM", "Fetched ${beefRecipes.size} beef recipes and ${chickenRecipes.size} chicken recipes. Total: ${apiRecipes.size}")

            // 2. Ensure these API recipes have a backing CommunityPost in Firestore
            if (apiRecipes.isNotEmpty()) {
                apiRecipes.forEach { recipeEntity ->
                    try {
                        Log.d("CommunityVM", "Ensuring API recipe post for ${recipeEntity.title} (ID: ${recipeEntity.id})")
                        communityRepo.createOrGetApiRecipePost(
                            apiRecipeId = recipeEntity.id,
                            title = recipeEntity.title,
                            imageUrl = recipeEntity.imageUrl,
                            category = recipeEntity.category, // This will be "Beef" or "Chicken"
                            initialIngredients = "", 
                            initialInstructions = recipeEntity.instructions ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e("CommunityVM", "Error ensuring API recipe post for ${recipeEntity.id}: ${e.message}", e)
                    }
                }
            } else {
                Log.d("CommunityVM", "No API recipes found for Beef or Chicken categories to feature.")
            }

            // 3. Load all posts from the community repository (will include user posts and API-sourced ones)
            Log.d("CommunityVM", "Loading all community posts (user-uploaded and API-sourced)")
            val allCommunityPosts = communityRepo.listPopular()
            _state.update { it.copy(loadingPosts = false, posts = allCommunityPosts) }
            applyFilter() // Apply filter after all posts are loaded
            Log.d("CommunityVM", "Finished loading and filtering all posts. Total: ${allCommunityPosts.size}")

        } catch (e: Exception) {
            Log.e("CommunityVM", "Error in loadPostsAndEnsureApiRecipes: ${e.message}", e)
            _state.update { it.copy(loadingPosts = false, error = "Failed to load posts: ${e.message}") }
        }
    }


    private fun observeCategories() {
        viewModelScope.launch {
            recipeRepo.observeCategories()
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
                recipeRepo.refreshCategoriesIfNeeded()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to refresh categories: ${e.message}") }
            } finally {
                if (isInitial) _state.update { it.copy(isLoadingCategoriesRefresh = false) }
            }
        }
    }

    fun like(postId: String) = viewModelScope.launch {
        // Optimistically update UI or show loading indicator for the specific item if possible
        // For now, simple loading state for all posts
        // _state.update { it.copy(loadingPosts = true) } 
        try {
            communityRepo.like(postId)
            // Refresh only the affected post or reload all if simpler for now
            val posts = communityRepo.listPopular()
            _state.update { it.copy(posts = posts) } // loadingPosts can be managed more granularly
            applyFilter()
        } catch (e: Exception) {
            _state.update { it.copy(error = "Failed to like post: ${e.message}") }
        }
    }

    fun create(title: String, imageUri: Uri?, ingredients: String, instructions: String, category: String? = null) = viewModelScope.launch {
        // _state.update { it.copy(loadingPosts = true) }
        try {
            communityRepo.create(title, imageUri, ingredients, instructions, category)
            val posts = communityRepo.listPopular()
            _state.update { it.copy(posts = posts) } // loadingPosts can be managed more granularly
            applyFilter()
        } catch (e: Exception) {
            _state.update { it.copy(error = "Failed to create post: ${e.message}") }
        }
    }
}