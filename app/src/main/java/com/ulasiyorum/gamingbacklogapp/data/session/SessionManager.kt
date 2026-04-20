package com.ulasiyorum.gamingbacklogapp.data.session

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.ulasiyorum.gamingbacklogapp.data.models.BacklogState
import com.ulasiyorum.gamingbacklogapp.data.models.Game
import com.ulasiyorum.gamingbacklogapp.data.models.User
import com.ulasiyorum.gamingbacklogapp.data.models.UserGame
import com.ulasiyorum.gamingbacklogapp.data.models.stateEnum
import com.ulasiyorum.gamingbacklogapp.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object SessionManager {
    private const val PREFS_NAME = "gaming_backlog_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_GUEST_PROFILE_JSON = "guest_profile_json"
    private const val GUEST_USER_ID = -1

    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isGuestSession = MutableStateFlow(false)
    val isGuestSession: StateFlow<Boolean> = _isGuestSession.asStateFlow()

    private val _isRestored = MutableStateFlow(false)
    val isRestored: StateFlow<Boolean> = _isRestored.asStateFlow()

    fun initialize(context: Context) {
        if (::sharedPreferences.isInitialized) return
        sharedPreferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    suspend fun restoreSession() {
        if (_isRestored.value) return

        val storedUserId = sharedPreferences.getInt(KEY_USER_ID, -1)
        if (storedUserId != -1) {
            GameRepository.getProfile(storedUserId)
                .onSuccess { user ->
                    _currentUser.value = user
                    _isGuestSession.value = false
                }
                .onFailure {
                    sharedPreferences.edit().remove(KEY_USER_ID).apply()
                    restoreGuestProfile()
                }
        } else {
            restoreGuestProfile()
        }

        _isRestored.value = true
    }

    suspend fun refreshProfile(): Result<User> {
        if (_isGuestSession.value) {
            return _currentUser.value?.let { user -> Result.success(user) }
                ?: Result.failure(Exception("Aktif bir kullanici bulunamadi."))
        }

        val userId = _currentUser.value?.id
            ?: return Result.failure(Exception("Aktif bir kullanici bulunamadi."))

        return GameRepository.getProfile(userId).also { result ->
            result.onSuccess(::setCurrentUser)
        }
    }

    fun setCurrentUser(user: User) {
        _currentUser.value = user
        _isGuestSession.value = false
        sharedPreferences.edit()
            .putInt(KEY_USER_ID, user.id)
            .remove(KEY_GUEST_PROFILE_JSON)
            .apply()
    }

    fun startGuestSession() {
        val guestUser = currentGuestSnapshot() ?: createGuestUser()
        setGuestUser(guestUser)
        _isRestored.value = true
    }

    fun addGameToGuestSession(game: Game): Result<User> {
        val guestUser = currentGuestSnapshot() ?: createGuestUser()
        if (guestUser.userGames.orEmpty().any { userGame -> userGame.gameId == game.id }) {
            return Result.success(guestUser)
        }

        val updatedUser = guestUser.copy(
            userGames = guestUser.userGames.orEmpty() + UserGame(
                id = UUID.randomUUID().toString(),
                userId = GUEST_USER_ID,
                gameId = game.id,
                game = game,
                state = BacklogState.PLANNED.value,
                notes = null
            )
        )
        setGuestUser(updatedUser)
        return Result.success(updatedUser)
    }

    fun updateGuestGame(userGameId: String, state: BacklogState, notes: String?): Result<User> {
        val guestUser = currentGuestSnapshot()
            ?: return Result.failure(Exception("Misafir profili bulunamadi."))

        val updatedGames = guestUser.userGames.orEmpty().map { userGame ->
            if (userGame.id == userGameId) {
                userGame.copy(state = state.value, notes = notes)
            } else {
                userGame
            }
        }
        val updatedUser = guestUser.copy(userGames = updatedGames)
        setGuestUser(updatedUser)
        return Result.success(updatedUser)
    }

    fun removeGuestGame(userGameId: String): Result<User> {
        val guestUser = currentGuestSnapshot()
            ?: return Result.failure(Exception("Misafir profili bulunamadi."))

        val updatedUser = guestUser.copy(
            userGames = guestUser.userGames.orEmpty().filterNot { userGame -> userGame.id == userGameId }
        )
        setGuestUser(updatedUser)
        return Result.success(updatedUser)
    }

    suspend fun finalizeAuthenticatedSession(user: User): User {
        val guestSnapshot = currentGuestSnapshot()
        if (guestSnapshot == null) {
            setCurrentUser(user)
            return user
        }

        var latestUser = user
        guestSnapshot.userGames.orEmpty().forEach { guestGame ->
            latestUser = GameRepository.addGameToUser(latestUser.id, guestGame.gameId)
                .getOrElse { latestUser }

            val serverUserGame = latestUser.userGames.orEmpty()
                .firstOrNull { userGame -> userGame.gameId == guestGame.gameId }

            if (serverUserGame != null &&
                (guestGame.stateEnum() != BacklogState.PLANNED || !guestGame.notes.isNullOrBlank())
            ) {
                latestUser = GameRepository.updateUserGame(
                    userGameId = serverUserGame.id,
                    state = guestGame.stateEnum(),
                    notes = guestGame.notes
                ).getOrElse { latestUser }
            }
        }

        setCurrentUser(latestUser)
        return latestUser
    }

    fun clearSession() {
        _currentUser.value = null
        _isGuestSession.value = false
        sharedPreferences.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_GUEST_PROFILE_JSON)
            .apply()
    }

    private fun restoreGuestProfile() {
        val guestJson = sharedPreferences.getString(KEY_GUEST_PROFILE_JSON, null)
        if (guestJson.isNullOrBlank()) {
            _currentUser.value = null
            _isGuestSession.value = false
            return
        }

        val guestUser = runCatching { gson.fromJson(guestJson, User::class.java) }.getOrNull()
        if (guestUser == null) {
            sharedPreferences.edit().remove(KEY_GUEST_PROFILE_JSON).apply()
            _currentUser.value = null
            _isGuestSession.value = false
            return
        }

        _currentUser.value = guestUser
        _isGuestSession.value = true
    }

    private fun setGuestUser(user: User) {
        _currentUser.value = user
        _isGuestSession.value = true
        sharedPreferences.edit()
            .remove(KEY_USER_ID)
            .putString(KEY_GUEST_PROFILE_JSON, gson.toJson(user))
            .apply()
    }

    private fun currentGuestSnapshot(): User? {
        return if (_isGuestSession.value) {
            _currentUser.value
        } else {
            sharedPreferences.getString(KEY_GUEST_PROFILE_JSON, null)
                ?.let { guestJson -> runCatching { gson.fromJson(guestJson, User::class.java) }.getOrNull() }
        }
    }

    private fun createGuestUser(): User {
        return User(
            id = GUEST_USER_ID,
            name = "Misafir Oyuncu",
            email = null,
            userGames = emptyList()
        )
    }
}
