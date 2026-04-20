package com.ulasiyorum.gamingbacklogapp.data.repository

import com.ulasiyorum.gamingbacklogapp.data.models.BacklogState
import com.ulasiyorum.gamingbacklogapp.data.models.Category
import com.ulasiyorum.gamingbacklogapp.data.models.Game
import com.ulasiyorum.gamingbacklogapp.data.models.GetGamesOrder
import com.ulasiyorum.gamingbacklogapp.data.models.User
import com.ulasiyorum.gamingbacklogapp.data.network.AddGameToUserDto
import com.ulasiyorum.gamingbacklogapp.data.network.GameApiService
import com.ulasiyorum.gamingbacklogapp.data.network.LoginDto
import com.ulasiyorum.gamingbacklogapp.data.network.RegisterDto
import com.ulasiyorum.gamingbacklogapp.data.network.RetrofitClient
import com.ulasiyorum.gamingbacklogapp.data.network.UpdateUserGameDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import retrofit2.Response

object GameRepository {
    private val api: GameApiService = RetrofitClient.apiService
    private val cachedGames = MutableStateFlow<Map<Int, Game>>(emptyMap())

    fun observeGame(gameId: Int): Flow<Game?> = cachedGames.map { games -> games[gameId] }

    suspend fun login(email: String, password: String): Result<User> =
        safeApiCall { api.login(LoginDto(email, password)) }.alsoSuccess(::cacheUser)

    suspend fun register(name: String, email: String, password: String): Result<User> =
        safeApiCall { api.register(RegisterDto(name, email, password)) }.alsoSuccess(::cacheUser)

    suspend fun getGames(
        page: Int = 1,
        pageSize: Int = 20,
        order: GetGamesOrder? = GetGamesOrder.DEFAULT
    ): Result<List<Game>> = safeApiCall {
        api.getGames(page = page, pageSize = pageSize, order = order?.value)
    }.alsoSuccess(::cacheGames)

    suspend fun searchGames(query: String): Result<List<Game>> =
        safeApiCall { api.searchGames(query) }.alsoSuccess(::cacheGames)

    suspend fun getCategories(): Result<List<Category>> = safeApiCall { api.getCategories() }

    suspend fun getByCategory(categoryId: Int): Result<List<Game>> =
        safeApiCall { api.getByCategory(categoryId) }.alsoSuccess(::cacheGames)

    suspend fun getProfile(userId: Int): Result<User> =
        safeApiCall { api.getProfile(userId) }.alsoSuccess(::cacheUser)

    suspend fun addGameToUser(userId: Int, gameId: Int): Result<User> =
        safeApiCall { api.addGameToUser(AddGameToUserDto(userId, gameId)) }.alsoSuccess(::cacheUser)

    suspend fun updateUserGame(userGameId: String, state: BacklogState, notes: String?): Result<User> =
        safeApiCall {
            api.updateUserGame(UpdateUserGameDto(userGameId = userGameId, state = state.value, notes = notes))
        }.alsoSuccess(::cacheUser)

    suspend fun removeGameFromUser(userGameId: String): Result<User> =
        safeApiCall { api.removeGameFromUser(userGameId) }.alsoSuccess(::cacheUser)

    private suspend fun <T> safeApiCall(block: suspend () -> Response<T>): Result<T> {
        return try {
            val response = block()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(response.errorBody()?.string().orEmpty().ifBlank {
                    "Sunucudan gecerli bir yanit alinamadi."
                }))
            }
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: "Baglanti sirasinda bir hata olustu.", exception))
        }
    }

    private fun cacheGames(games: List<Game>) {
        val updated = cachedGames.value.toMutableMap()
        games.forEach { game -> updated[game.id] = game }
        cachedGames.value = updated
    }

    private fun cacheUser(user: User) {
        cacheGames(user.userGames.orEmpty().mapNotNull { userGame -> userGame.game })
    }
}

private fun <T> Result<T>.alsoSuccess(action: (T) -> Unit): Result<T> {
    onSuccess(action)
    return this
}
