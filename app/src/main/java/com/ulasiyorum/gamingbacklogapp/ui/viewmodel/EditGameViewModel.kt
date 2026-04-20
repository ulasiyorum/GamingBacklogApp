package com.ulasiyorum.gamingbacklogapp.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ulasiyorum.gamingbacklogapp.data.models.BacklogState
import com.ulasiyorum.gamingbacklogapp.data.repository.GameRepository
import com.ulasiyorum.gamingbacklogapp.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditGameUiState(
    val isSaving: Boolean = false,
    val isRemoving: Boolean = false,
    val errorMessage: String? = null
)

class EditGameViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    val userGameId: String = checkNotNull(savedStateHandle["userGameId"])
    val currentUser = SessionManager.currentUser

    private val _uiState = MutableStateFlow(EditGameUiState())
    val uiState: StateFlow<EditGameUiState> = _uiState.asStateFlow()

    fun saveChanges(state: BacklogState, notes: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { current -> current.copy(isSaving = true, errorMessage = null) }
            val result = if (SessionManager.isGuestSession.value) {
                SessionManager.updateGuestGame(userGameId, state, notes.ifBlank { null })
            } else {
                GameRepository.updateUserGame(userGameId, state, notes.ifBlank { null })
            }

            result
                .onSuccess { user ->
                    if (!SessionManager.isGuestSession.value) {
                        SessionManager.setCurrentUser(user)
                    }
                    _uiState.update { current -> current.copy(isSaving = false) }
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update { current ->
                        current.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Oyun guncellenemedi."
                        )
                    }
                }
        }
    }

    fun removeGame(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { current -> current.copy(isRemoving = true, errorMessage = null) }
            val result = if (SessionManager.isGuestSession.value) {
                SessionManager.removeGuestGame(userGameId)
            } else {
                GameRepository.removeGameFromUser(userGameId)
            }

            result
                .onSuccess { user ->
                    if (!SessionManager.isGuestSession.value) {
                        SessionManager.setCurrentUser(user)
                    }
                    _uiState.update { current -> current.copy(isRemoving = false) }
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update { current ->
                        current.copy(
                            isRemoving = false,
                            errorMessage = error.message ?: "Oyun kaldirilamadi."
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { current -> current.copy(errorMessage = null) }
    }
}
