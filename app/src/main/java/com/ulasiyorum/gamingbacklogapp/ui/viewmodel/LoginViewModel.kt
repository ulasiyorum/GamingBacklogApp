package com.ulasiyorum.gamingbacklogapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ulasiyorum.gamingbacklogapp.data.repository.GameRepository
import com.ulasiyorum.gamingbacklogapp.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun clearError() {
        _uiState.update { current -> current.copy(errorMessage = null) }
    }

    fun handleLogin(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { current -> current.copy(isLoading = true, errorMessage = null) }
            GameRepository.login(email, pass)
                .onSuccess { user ->
                    SessionManager.setCurrentUser(user)
                    _uiState.update { current -> current.copy(isLoading = false) }
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Giris sirasinda bir hata olustu."
                        )
                    }
                }
        }
    }
}
