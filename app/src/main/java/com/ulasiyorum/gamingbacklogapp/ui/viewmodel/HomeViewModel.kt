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
    val isLoadingMore: Boolean = false,
    val searchQuery: String = "",
    val games: List<Game> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Int? = null,
    val currentPage: Int = 0,
    val canLoadMore: Boolean = true,
    val errorMessage: String? = null
)

class HomeViewModel : ViewModel() {
    private companion object {
        const val PAGE_SIZE = 10
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    isLoadingMore = false,
                    currentPage = 0,
                    canLoadMore = true,
                    errorMessage = null
                )
            }

            GameRepository.getCategories()
                .onSuccess { categories ->
                    _uiState.update { current -> current.copy(categories = categories) }
                }

            loadGamesForCurrentState(reset = true)
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                errorMessage = null,
                currentPage = 0,
                canLoadMore = query.isBlank() && current.selectedCategoryId == null
            )
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            loadGamesForCurrentState(reset = true)
        }
    }

    fun onCategorySelected(categoryId: Int?) {
        _uiState.update { current ->
            current.copy(
                selectedCategoryId = categoryId,
                errorMessage = null,
                currentPage = 0,
                canLoadMore = current.searchQuery.isBlank() && categoryId == null
            )
        }
        if (_uiState.value.searchQuery.isBlank()) {
            viewModelScope.launch {
                loadGamesForCurrentState(reset = true)
            }
        }
    }

    fun clearError() {
        _uiState.update { current -> current.copy(errorMessage = null) }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.canLoadMore) return
        if (state.searchQuery.isNotBlank() || state.selectedCategoryId != null) return

        viewModelScope.launch {
            loadGamesForCurrentState(reset = false)
        }
    }

    private suspend fun loadGamesForCurrentState(reset: Boolean) {
        val state = _uiState.value
        val requestPage = if (reset) 1 else state.currentPage + 1

        if (!reset && (state.searchQuery.isNotBlank() || state.selectedCategoryId != null)) return

        _uiState.update { current ->
            current.copy(
                isLoading = if (reset) true else current.isLoading,
                isLoadingMore = !reset,
                errorMessage = null
            )
        }

        val result = when {
            state.searchQuery.isNotBlank() -> GameRepository.searchGames(state.searchQuery.trim())
            state.selectedCategoryId != null -> GameRepository.getByCategory(state.selectedCategoryId)
            else -> GameRepository.getGames(page = requestPage, pageSize = PAGE_SIZE)
        }

        result
            .onSuccess { games ->
                _uiState.update { current ->
                    val mergedGames = if (reset || current.searchQuery.isNotBlank() || current.selectedCategoryId != null) {
                        games
                    } else {
                        (current.games + games).distinctBy { game -> game.id }
                    }
                    val paginationEnabled = current.searchQuery.isBlank() && current.selectedCategoryId == null

                    current.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        games = mergedGames,
                        currentPage = if (paginationEnabled) requestPage else 1,
                        canLoadMore = if (paginationEnabled) games.size >= PAGE_SIZE else false,
                        errorMessage = null
                    )
                }
            }
            .onFailure { error ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        games = if (reset) emptyList() else current.games,
                        errorMessage = error.message ?: "Oyunlar yuklenemedi."
                    )
                }
            }
    }
}
