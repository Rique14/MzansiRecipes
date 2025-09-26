package com.mzansi.recipes.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.GoogleAuthProvider
import com.mzansi.recipes.data.repo.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val loggedIn: Boolean = false
)

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state = _state.asStateFlow()

    fun register(name: String, email: String, password: String) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        try {
            repo.register(name, email, password)
            _state.value = _state.value.copy(loading = false, loggedIn = true)
        } catch (e: Exception) {
            _state.value = _state.value.copy(loading = false, error = e.message)
        }
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        try {
            repo.login(email, password)
            _state.value = _state.value.copy(loading = false, loggedIn = true)
        } catch (e: Exception) {
            _state.value = _state.value.copy(loading = false, error = e.message)
        }
    }

    fun forgot(email: String) = viewModelScope.launch {
        try { repo.forgot(email) } catch (_: Exception) {}
    }

    fun signInWithGoogle(idToken: String) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        try {
            Log.d("AuthViewModel", "Attempting Google Sign-In with token: $idToken")
            val credential = GoogleAuthProvider.getCredential(idToken, null)

            repo.signInWithCredentialAndManageUser(credential) 
            _state.value = _state.value.copy(loading = false, loggedIn = true)
            Log.d("AuthViewModel", "Google Sign-In successful.")
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Google Sign-In failed", e)
            _state.value = _state.value.copy(loading = false, error = e.message ?: "Google Sign-In failed")
        }
    }
}
