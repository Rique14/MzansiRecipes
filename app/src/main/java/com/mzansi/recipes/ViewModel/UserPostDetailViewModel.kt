package com.mzansi.recipes.ViewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mzansi.recipes.data.db.RecipeEntity
import com.mzansi.recipes.data.repo.CommunityPost
import com.mzansi.recipes.data.repo.CommunityRepository
import com.mzansi.recipes.data.repo.RecipeRepository
import com.mzansi.recipes.data.repo.ShoppingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Represents one ingredient item in the UI
data class IngredientItem(
    val name: String,
    val isSelected: Boolean = true // Default to selected for user convenience
)

data class UserPostDetailUiState(
    val post: CommunityPost? = null,
    val ingredients: List<IngredientItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val itemsAddedToCart: Boolean = false,
    val isSavedOffline: Boolean = false // Added for save state
)

class UserPostDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val communityRepo: CommunityRepository,
    private val shoppingRepo: ShoppingRepository,
    private val recipeRepo: RecipeRepository // Added recipeRepo
) : ViewModel() {

    private val postId: String = savedStateHandle.get<String>("postId")!!

    private val _uiState = MutableStateFlow(UserPostDetailUiState())
    val uiState: StateFlow<UserPostDetailUiState> = _uiState.asStateFlow()

    init {
        // The check for saved status is now inside observePostDetails to prevent race conditions
        observePostDetails()
    }

    private fun observePostDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            communityRepo.getPostById(postId)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load post details: ${e.message}") }
                }
                .collect { post ->
                    if (post != null) {
                        // Check saved status AFTER the post is loaded to ensure we have the correct ID
                        val idToCheck = post.sourceApiId ?: post.postId
                        val isSaved = recipeRepo.isRecipeSavedOffline(idToCheck)

                        val ingredientList = if (post.ingredients.isNullOrBlank()) {
                            emptyList()
                        } else {
                            post.ingredients
                                .split('\n') // Split by newline character
                                .filter { it.isNotBlank() }
                                .map { IngredientItem(name = it.trim()) }
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                post = post,
                                ingredients = ingredientList,
                                isSavedOffline = isSaved, // Update state with the correct save status
                                error = null // Clear previous errors on success
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "Post not found.") }
                    }
                }
        }
    }

    fun toggleSaveRecipe() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val post = currentState.post ?: return@launch

            val idToSave = post.sourceApiId ?: post.postId

            if (currentState.isSavedOffline) {
                recipeRepo.removeRecipeFromOffline(idToSave)
                _uiState.update { it.copy(isSavedOffline = false) }
            } else {
                if (post.isUserUploaded) {
                    val recipeEntity = RecipeEntity(
                        id = post.postId,
                        title = post.title,
                        imageUrl = post.imageUrl,
                        instructions = post.instructions,
                        category = post.category ?: "User Post",
                        isSavedOffline = true, // Mark as saved
                        trending = false, // User posts are not trending by default
                        ingredients = currentState.ingredients.map { it.name } // Pass the ingredients
                    )
                    recipeRepo.saveRecipeEntity(recipeEntity)
                } else {
                    recipeRepo.saveRecipeForOffline(idToSave)
                }
                _uiState.update { it.copy(isSavedOffline = true) }
            }
        }
    }

    fun toggleIngredientSelection(ingredientName: String) {
        _uiState.update { currentState ->
            val updatedIngredients = currentState.ingredients.map {
                if (it.name == ingredientName) {
                    it.copy(isSelected = !it.isSelected)
                } else {
                    it
                }
            }
            currentState.copy(ingredients = updatedIngredients)
        }
    }

    fun addSelectedIngredientsToCart() {
        viewModelScope.launch {
            try {
                val selectedIngredients = _uiState.value.ingredients
                    .filter { it.isSelected }
                    .map { it.name }

                if (selectedIngredients.isNotEmpty()) {
                    shoppingRepo.addItems(selectedIngredients)
                    _uiState.update { it.copy(itemsAddedToCart = true) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to add items to shopping list.") }
            }
        }
    }

    fun onAddedToCartHandled() {
        _uiState.update { it.copy(itemsAddedToCart = false) }
    }

    fun likePost() {
        viewModelScope.launch {
            try {
                communityRepo.like(postId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to like post.") }
            }
        }
    }
}
