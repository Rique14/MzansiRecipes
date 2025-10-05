package com.mzansi.recipes.ViewModel

import android.util.Log
import android.util.Patterns
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
    val loggedIn: Boolean = false,
    val registrationSuccess: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state = _state.asStateFlow()

    fun onRegistrationSuccessHandled() {
        _state.value = _state.value.copy(registrationSuccess = false)
    }

    fun register(name: String, email: String, password: String, confirm: String) = viewModelScope.launch {
        val nameError = if (name.isBlank()) "Name cannot be empty" else null
        val emailError = if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) "Invalid email address" else null
        val passwordError = if (password.length < 6) "Password must be at least 6 characters" else null
        val confirmPasswordError = if (password != confirm) "Passwords do not match" else null

        _state.value = _state.value.copy(
            error = null,
            nameError = nameError,
            emailError = emailError,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError,
            registrationSuccess = false
        )

        if (nameError != null || emailError != null || passwordError != null || confirmPasswordError != null) {
            return@launch
        }

        _state.value = _state.value.copy(loading = true)
        try {
            repo.register(name, email, password)
            _state.value = _state.value.copy(loading = false, registrationSuccess = true)
        } catch (e: Exception) {
            _state.value = _state.value.copy(loading = false, error = e.message)
        }
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        val emailError = if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) "Invalid email address" else null
        val passwordError = if (password.isBlank()) "Password cannot be empty" else null

        _state.value = _state.value.copy(
            error = null,
            emailError = emailError,
            passwordError = passwordError,
            nameError = null,
            confirmPasswordError = null,
            registrationSuccess = false
        )

        if (emailError != null || passwordError != null) {
            return@launch
        }

        _state.value = _state.value.copy(loading = true)
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
        _state.value = _state.value.copy(loading = true, error = null, nameError = null, emailError = null, passwordError = null, confirmPasswordError = null, registrationSuccess = false)
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
