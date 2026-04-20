package com.ulasiyorum.gamingbacklogapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ulasiyorum.gamingbacklogapp.data.models.Category
import com.ulasiyorum.gamingbacklogapp.data.models.Game
import com.ulasiyorum.gamingbacklogapp.data.repository.GameRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val games: List<Game> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Int? = null,
    val errorMessage: String? = null
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { current -> current.copy(isLoading = true, errorMessage = null) }

            GameRepository.getCategories()
                .onSuccess { categories ->
                    _uiState.update { current -> current.copy(categories = categories) }
                }

            loadGamesForCurrentState()
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { current -> current.copy(searchQuery = query, errorMessage = null) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            loadGamesForCurrentState()
        }
    }

    fun onCategorySelected(categoryId: Int?) {
        _uiState.update { current -> current.copy(selectedCategoryId = categoryId, errorMessage = null) }
        if (_uiState.value.searchQuery.isBlank()) {
            viewModelScope.launch {
                loadGamesForCurrentState()
            }
        }
    }

    fun clearError() {
        _uiState.update { current -> current.copy(errorMessage = null) }
    }

    private suspend fun loadGamesForCurrentState() {
        val state = _uiState.value
        val result = when {
            state.searchQuery.isNotBlank() -> GameRepository.searchGames(state.searchQuery.trim())
            state.selectedCategoryId != null -> GameRepository.getByCategory(state.selectedCategoryId)
            else -> GameRepository.getGames()
        }

        result
            .onSuccess { games ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        games = games,
                        errorMessage = null
                    )
                }
            }
            .onFailure { error ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        games = emptyList(),
                        errorMessage = error.message ?: "Oyunlar yuklenemedi."
                    )
                }
            }
    }
}
