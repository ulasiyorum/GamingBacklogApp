package com.ulasiyorum.gamingbacklogapp.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ulasiyorum.gamingbacklogapp.data.models.Game
import com.ulasiyorum.gamingbacklogapp.data.repository.GameRepository
import com.ulasiyorum.gamingbacklogapp.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameDetailUiState(
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class GameDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val gameId: Int = checkNotNull(savedStateHandle["gameId"])

    val game: StateFlow<Game?> = GameRepository.observeGame(gameId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val currentUser = SessionManager.currentUser

    private val _uiState = MutableStateFlow(GameDetailUiState())
    val uiState: StateFlow<GameDetailUiState> = _uiState.asStateFlow()

    init {
        if (game.value == null) {
            viewModelScope.launch {
                GameRepository.getGames()
            }
        }
    }

    fun addToBacklog() {
        val user = currentUser.value
        val selectedGame = game.value

        if (user == null || selectedGame == null) return

        if (user.userGames.orEmpty().any { userGame -> userGame.gameId == selectedGame.id }) {
            _uiState.update { current -> current.copy(infoMessage = "Bu oyun zaten backlog'unda.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(isSubmitting = true, errorMessage = null, infoMessage = null)
            }

            GameRepository.addGameToUser(user.id, selectedGame.id)
                .onSuccess { updatedUser ->
                    SessionManager.setCurrentUser(updatedUser)
                    _uiState.update { current ->
                        current.copy(isSubmitting = false, infoMessage = "Oyun backlog'una eklendi.")
                    }
                }
                .onFailure { error ->
                    _uiState.update { current ->
                        current.copy(
                            isSubmitting = false,
                            errorMessage = error.message ?: "Oyun backlog'a eklenemedi."
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { current -> current.copy(errorMessage = null, infoMessage = null) }
    }
}
