package com.mzansi.recipes.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mzansi.recipes.data.repo.RecipeFullDetails
import com.mzansi.recipes.data.repo.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecipeDetailUiState(
    val details: RecipeFullDetails? = null,
    val loading: Boolean = false,
    val error: String? = null
)

class RecipeDetailViewModel(
    private val repo: RecipeRepository,
    private val recipeId: String
) : ViewModel() {
    private val _state = MutableStateFlow(RecipeDetailUiState())
    val state = _state.asStateFlow()

    init {
        load()
    }

    private fun load() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        try {
            val details = repo.getFullDetail(recipeId)
            _state.value = _state.value.copy(loading = false, details = details)
        } catch (e: Exception) {
            _state.value = _state.value.copy(loading = false, error = e.message)
        }
    }
}

class RecipeDetailViewModelFactory(
    private val repo: RecipeRepository,
    private val recipeId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeDetailViewModel(repo, recipeId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
