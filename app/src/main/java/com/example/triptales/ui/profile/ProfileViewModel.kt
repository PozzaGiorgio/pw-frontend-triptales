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
                android.util.Log.d("ProfileViewModel", "Loading user profile...")
                val user = apiService.getCurrentUser()
                android.util.Log.d("ProfileViewModel", "User loaded: ${user.username}")
                _userState.value = UserState.Success(user)
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "Failed to load user profile", e)
                _userState.value = UserState.Error(e.message ?: "Failed to load user profile")
            }
        }
    }

    fun loadUserBadges() {
        viewModelScope.launch {
            try {
                android.util.Log.d("ProfileViewModel", "Loading user badges...")

                // 🔧 FIX: Ora gestiamo la risposta paginata
                val paginatedResponse = apiService.getUserBadgesPaginated()
                val badges = paginatedResponse.results

                android.util.Log.d("ProfileViewModel", "Badges loaded: ${badges.size} badges")
                android.util.Log.d("ProfileViewModel", "Total badges count: ${paginatedResponse.count}")

                _badgesState.value = BadgesState.Success(badges)
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "Failed to load badges", e)
                _badgesState.value = BadgesState.Error(e.message ?: "Failed to load badges")
            }
        }
    }

    fun logout() {
        android.util.Log.d("ProfileViewModel", "User logging out...")
        authRepository.logout()
    }
}