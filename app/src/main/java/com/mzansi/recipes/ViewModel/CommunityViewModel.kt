package com.mzansi.recipes.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mzansi.recipes.data.repo.CommunityPost
import com.mzansi.recipes.data.repo.CommunityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CommunityUiState(
    val posts: List<CommunityPost> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

class CommunityViewModel(private val repo: CommunityRepository) : ViewModel() {
    private val _state = MutableStateFlow(CommunityUiState())
    val state = _state.asStateFlow()

    fun load() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        try {
            val posts = repo.listPopular()
            _state.value = _state.value.copy(loading = false, posts = posts)
        } catch (e: Exception) {
            _state.value = _state.value.copy(loading = false, error = e.message)
        }
    }

    fun like(postId: String) = viewModelScope.launch { repo.like(postId); load() }
    fun create(title: String) = viewModelScope.launch { repo.create(title); load() }
}