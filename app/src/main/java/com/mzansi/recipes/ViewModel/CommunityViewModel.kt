package com.mzansi.recipes.ViewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mzansi.recipes.data.db.CategoryEntity
import com.mzansi.recipes.data.db.RecipeEntity
import com.mzansi.recipes.data.repo.CommunityPost
import com.mzansi.recipes.data.repo.CommunityRepository
import com.mzansi.recipes.data.repo.RecipeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CommunityUiState(
    val posts: List<CommunityPost> = emptyList(),
    val filteredPosts: List<CommunityPost> = emptyList(),
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
        viewModelScope.launch {
            _state.update { it.copy(loadingPosts = true, error = null) }
            try {
                refreshApiRecipes(listOf("Beef", "Chicken")) // Add more categories if needed
                loadAllPosts()
            } catch (e: Exception) {
                Log.e("CommunityVM", "Error loading content: ${e.message}", e)
                _state.update { it.copy(error = "Failed to load content: ${e.message}") }
            } finally {
                _state.update { it.copy(loadingPosts = false) }
            }
        }
    }

    private suspend fun refreshApiRecipes(categories: List<String>) {
        val dbRecipes: List<RecipeEntity> = recipeRepo.observeAllRecipes().first()

        for (category in categories) {
            recipeRepo.refreshRecipesForCategory(category)
            val recipes: List<RecipeEntity> = recipeRepo.observeRecipesByCategory(category).first()
            for (recipe in recipes) {
                try {
                    communityRepo.createOrGetApiRecipePost(
                        apiRecipeId = recipe.id,
                        title = recipe.title,
                        imageUrl = recipe.imageUrl,
                        category = recipe.category,
                        initialIngredients = recipe.ingredients.joinToString("\n"),
                        initialInstructions = recipe.instructions ?: "",
                        createdAt = System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    Log.e("CommunityVM", "Error creating API post: ${e.message}", e)
                }
            }
        }
    }

    private suspend fun loadAllPosts() {
        try {
            val allPosts: List<CommunityPost> = communityRepo.listPopular()
            val sortedPosts = allPosts.sortedByDescending { it.createdAt }
            _state.update { it.copy(posts = sortedPosts) }
            applyFilter()
        } catch (e: Exception) {
            Log.e("CommunityVM", "Error loading posts: ${e.message}", e)
            _state.update { it.copy(error = "Failed to load posts: ${e.message}") }
        }
    }

    private fun applyFilter() {
        _state.update { currentState ->
            val filtered = currentState.posts.filter { post ->
                currentState.selectedCategoryName?.let { selected ->
                    post.category?.trim()?.equals(selected.trim(), ignoreCase = true) ?: false
                } ?: true
            }
            currentState.copy(filteredPosts = filtered)
        }
    }

    fun onCategorySelected(categoryName: String?) {
        _state.update { currentState ->
            val newSelectedCategory = if (currentState.selectedCategoryName == categoryName) null else categoryName
            currentState.copy(selectedCategoryName = newSelectedCategory)
        }
        applyFilter()
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
            loadAllPosts()
        } catch (e: Exception) {
            _state.update { it.copy(error = "Failed to like post: ${e.message}") }
        }
    }

    fun create(
        title: String,
        imageUri: Uri?,
        ingredients: String,
        instructions: String,
        category: String?
    ) = viewModelScope.launch {
        _state.update { it.copy(loadingPosts = true, error = null) }
        try {
            communityRepo.create(title, imageUri, ingredients, instructions, category)
            loadAllPosts()
        } catch (e: Exception) {
            _state.update { it.copy(error = "Failed to create post: ${e.message}") }
        } finally {
            _state.update { it.copy(loadingPosts = false) }
        }
    }
}
