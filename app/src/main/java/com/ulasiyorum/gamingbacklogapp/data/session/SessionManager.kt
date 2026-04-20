package com.ulasiyorum.gamingbacklogapp.data.session

import android.content.Context
import android.content.SharedPreferences
import com.ulasiyorum.gamingbacklogapp.data.models.User
import com.ulasiyorum.gamingbacklogapp.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {
    private const val PREFS_NAME = "gaming_backlog_session"
    private const val KEY_USER_ID = "user_id"

    private lateinit var sharedPreferences: SharedPreferences

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isRestored = MutableStateFlow(false)
    val isRestored: StateFlow<Boolean> = _isRestored.asStateFlow()

    fun initialize(context: Context) {
        if (::sharedPreferences.isInitialized) return
        sharedPreferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    suspend fun restoreSession() {
        if (_isRestored.value) return

        val storedUserId = sharedPreferences.getInt(KEY_USER_ID, -1)
        if (storedUserId == -1) {
            _currentUser.value = null
            _isRestored.value = true
            return
        }

        GameRepository.getProfile(storedUserId)
            .onSuccess { user -> _currentUser.value = user }
            .onFailure {
                sharedPreferences.edit().remove(KEY_USER_ID).apply()
                _currentUser.value = null
            }

        _isRestored.value = true
    }

    suspend fun refreshProfile(): Result<User> {
        val userId = _currentUser.value?.id
            ?: return Result.failure(Exception("Aktif bir kullanici bulunamadi."))

        return GameRepository.getProfile(userId).also { result ->
            result.onSuccess(::setCurrentUser)
        }
    }

    fun setCurrentUser(user: User) {
        _currentUser.value = user
        sharedPreferences.edit().putInt(KEY_USER_ID, user.id).apply()
    }

    fun clearSession() {
        _currentUser.value = null
        sharedPreferences.edit().remove(KEY_USER_ID).apply()
    }
}
