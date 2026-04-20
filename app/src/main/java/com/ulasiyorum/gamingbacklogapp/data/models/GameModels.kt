package com.ulasiyorum.gamingbacklogapp.data.models

enum class BacklogState(val value: Int, val label: String) {
    PLANNED(0, "Planlandi"),
    PLAYING(1, "Oynaniyor"),
    COMPLETED(2, "Tamamlandi");

    companion object {
        fun fromValue(value: Int): BacklogState = entries.firstOrNull { it.value == value } ?: PLANNED
    }
}

enum class GetGamesOrder(val value: Int) {
    DEFAULT(0),
    RATING_DESC(1)
}

data class User(
    val id: Int,
    val name: String?,
    val email: String?,
    val password: String? = null,
    val userGames: List<UserGame>?
)

data class UserGame(
    val id: String,
    val userId: Int,
    val gameId: Int,
    val game: Game?,
    val state: Int,
    val notes: String?
)

data class Game(
    val id: Int,
    val name: String?,
    val description: String?,
    val rating: Float,
    val gameCategories: List<GameCategory>?
)

data class GameCategory(
    val id: String? = null,
    val gameId: Int? = null,
    val categoryId: Int,
    val category: Category?
)

data class Category(val id: Int, val name: String?)

fun UserGame.stateEnum(): BacklogState = BacklogState.fromValue(state)
