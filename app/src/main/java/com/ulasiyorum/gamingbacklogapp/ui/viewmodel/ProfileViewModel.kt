package com.ulasiyorum.gamingbacklogapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ulasiyorum.gamingbacklogapp.data.models.User
import com.ulasiyorum.gamingbacklogapp.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

class ProfileViewModel : ViewModel() {
    val currentUser: StateFlow<User?> = SessionManager.currentUser
    val isGuestSession: StateFlow<Boolean> = SessionManager.isGuestSession

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun refreshProfile() {
        if (currentUser.value == null) return

        viewModelScope.launch {
            _uiState.update { current -> current.copy(isRefreshing = true, errorMessage = null) }
            SessionManager.refreshProfile()
                .onSuccess {
                    _uiState.update { current -> current.copy(isRefreshing = false) }
                }
                .onFailure { error ->
                    _uiState.update { current ->
                        current.copy(
                            isRefreshing = false,
                            errorMessage = error.message ?: "Profil yenilenemedi."
                        )
                    }
                }
        }
    }

    fun logout() {
        SessionManager.clearSession()
    }

    fun clearError() {
        _uiState.update { current -> current.copy(errorMessage = null) }
    }
}
