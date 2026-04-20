package com.ulasiyorum.gamingbacklogapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ulasiyorum.gamingbacklogapp.data.models.User
import com.ulasiyorum.gamingbacklogapp.data.session.SessionManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppViewModel : ViewModel() {
    val currentUser: StateFlow<User?> = SessionManager.currentUser
    val isRestored: StateFlow<Boolean> = SessionManager.isRestored

    init {
        viewModelScope.launch {
            SessionManager.restoreSession()
        }
    }
}
