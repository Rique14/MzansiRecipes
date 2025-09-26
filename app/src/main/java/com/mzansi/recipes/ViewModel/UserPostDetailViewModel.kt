package com.mzansi.recipes.ViewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mzansi.recipes.data.repo.CommunityPost
import com.mzansi.recipes.data.repo.CommunityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserPostDetailUiState(
    val isLoading: Boolean = false,
    val post: CommunityPost? = null,
    val error: String? = null
)

class UserPostDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val communityRepository: CommunityRepository
) : ViewModel() {

    private val postId: String = checkNotNull(savedStateHandle["postId"]) // Get postId from navigation args

    private val _uiState = MutableStateFlow(UserPostDetailUiState())
    val uiState: StateFlow<UserPostDetailUiState> = _uiState.asStateFlow()

    init {
        loadPostDetails()
    }

    private fun loadPostDetails() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            communityRepository.getPostById(postId)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load post details: ${e.message}"
                        )
                    }
                }
                .collect { post ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            post = post,
                            error = if (post == null) "Post not found" else null
                        )
                    }
                }
        }
    }

    fun likePost() {

        _uiState.value.post?.let {
            viewModelScope.launch {
                try {
                    communityRepository.like(it.postId)

                } catch (e: Exception) {
                    _uiState.update { state ->
                        state.copy(error = "Failed to like post: ${e.message}")
                    }
                }
            }
        }
    }
}