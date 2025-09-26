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

        loadPostsAndEnsureApiRecipes()

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
                null
            } else {
                categoryName
            }
            currentState.copy(selectedCategoryName = newSelectedCategory)
        }
        applyFilter()
    }

    private fun loadPostsAndEnsureApiRecipes() = viewModelScope.launch {
        _state.update { it.copy(loadingPosts = true, error = null) }
        try {

            Log.d("CommunityVM", "Refreshing Beef and Chicken categories for featured posts")
            recipeRepo.refreshRecipesForCategory("Beef")
            recipeRepo.refreshRecipesForCategory("Chicken")
            
            val beefRecipes = recipeRepo.observeRecipesByCategory("Beef").first()
            val chickenRecipes = recipeRepo.observeRecipesByCategory("Chicken").first()
            val apiRecipes: List<RecipeEntity> = beefRecipes + chickenRecipes
            Log.d("CommunityVM", "Fetched ${beefRecipes.size} beef recipes and ${chickenRecipes.size} chicken recipes. Total: ${apiRecipes.size}")

            // 2. Ensure API recipes have a backing CommunityPost in Firestore
            if (apiRecipes.isNotEmpty()) {
                apiRecipes.forEach { recipeEntity ->
                    try {
                        Log.d("CommunityVM", "Ensuring API recipe post for ${recipeEntity.title} (ID: ${recipeEntity.id})")
                        communityRepo.createOrGetApiRecipePost(
                            apiRecipeId = recipeEntity.id,
                            title = recipeEntity.title,
                            imageUrl = recipeEntity.imageUrl,
                            category = recipeEntity.category, // Will be "Beef" or "Chicken"
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


            Log.d("CommunityVM", "Loading all community posts (user-uploaded and API-sourced)")
            val allCommunityPosts = communityRepo.listPopular()
            _state.update { it.copy(loadingPosts = false, posts = allCommunityPosts) }
            applyFilter()
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

        try {
            communityRepo.like(postId)

            val posts = communityRepo.listPopular()
            _state.update { it.copy(posts = posts) }
            applyFilter()
        } catch (e: Exception) {
            _state.update { it.copy(error = "Failed to like post: ${e.message}") }
        }
    }

    fun create(title: String, imageUri: Uri?, ingredients: String, instructions: String, category: String? = null) = viewModelScope.launch {

        try {
            communityRepo.create(title, imageUri, ingredients, instructions, category)
            val posts = communityRepo.listPopular()
            _state.update { it.copy(posts = posts) }
            applyFilter()
        } catch (e: Exception) {
            _state.update { it.copy(error = "Failed to create post: ${e.message}") }
        }
    }
}