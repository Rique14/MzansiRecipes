package com.mzansi.recipes.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mzansi.recipes.data.api.CategorySummary
import com.mzansi.recipes.data.repo.CommunityPost
import com.mzansi.recipes.data.repo.CommunityRepository
import com.mzansi.recipes.data.repo.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CommunityUiState(
    val posts: List<CommunityPost> = emptyList(),
    val categories: List<CategorySummary> = emptyList(),
    val loadingPosts: Boolean = false,
    val isLoadingCategories: Boolean = false,
    val error: String? = null // Shared error state for now
)

class CommunityViewModel(
    private val communityRepo: CommunityRepository,
    private val recipeRepo: RecipeRepository // Added RecipeRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CommunityUiState())
    val state = _state.asStateFlow()

    // Renamed load to loadContent to be more descriptive
    fun loadContent() {
        loadPosts()
        loadCategories()
    }

    private fun loadPosts() = viewModelScope.launch {
        _state.update { it.copy(loadingPosts = true, error = null) }
        try {
            val posts = communityRepo.listPopular() // Assuming this is the default load
            _state.update { it.copy(loadingPosts = false, posts = posts) }
        } catch (e: Exception) {
            _state.update { it.copy(loadingPosts = false, error = e.message) }
        }
    }

    private fun loadCategories() = viewModelScope.launch {
        _state.update { it.copy(isLoadingCategories = true, error = null) } // Clear previous errors specifically for categories if needed
        try {
            val categories = recipeRepo.getCategories()
            _state.update { it.copy(isLoadingCategories = false, categories = categories) }
        } catch (e: Exception) {
            _state.update { it.copy(isLoadingCategories = false, error = e.message) } // Use shared error state
        }
    }

    fun like(postId: String) = viewModelScope.launch {
        communityRepo.like(postId)
        loadPosts() // Refresh posts after liking
    }

    fun create(title: String) = viewModelScope.launch {
        communityRepo.create(title)
        loadPosts() // Refresh posts after creating
    }
}