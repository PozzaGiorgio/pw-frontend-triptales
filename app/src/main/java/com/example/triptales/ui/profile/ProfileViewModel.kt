package com.example.triptales.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.triptales.data.api.ApiService
import com.example.triptales.data.model.Badge
import com.example.triptales.data.model.User
import com.example.triptales.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Stati per il profilo utente
sealed class UserState {
    object Loading : UserState()
    data class Success(val user: User) : UserState()
    data class Error(val message: String) : UserState()
}

// Stati per i badge dell'utente
sealed class BadgesState {
    object Loading : BadgesState()
    data class Success(val badges: List<Badge>) : BadgesState()
    data class Error(val message: String) : BadgesState()
}

class ProfileViewModel(
    private val apiService: ApiService,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState: StateFlow<UserState> = _userState

    private val _badgesState = MutableStateFlow<BadgesState>(BadgesState.Loading)
    val badgesState: StateFlow<BadgesState> = _badgesState

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val user = apiService.getCurrentUser()
                _userState.value = UserState.Success(user)
            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message ?: "Failed to load user profile")
            }
        }
    }

    fun loadUserBadges() {
        viewModelScope.launch {
            try {
                val badges = apiService.getUserBadges()
                _badgesState.value = BadgesState.Success(badges)
            } catch (e: Exception) {
                _badgesState.value = BadgesState.Error(e.message ?: "Failed to load badges")
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}