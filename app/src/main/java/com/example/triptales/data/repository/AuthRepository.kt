package com.example.triptales.data.repository

import android.content.Context
import com.example.triptales.data.api.ApiService
import com.example.triptales.data.model.LoginRequest
import com.example.triptales.data.model.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val apiService: ApiService,
    private val context: Context
) {
    private val tokenPrefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

    suspend fun login(username: String, password: String): Result<Unit> {
        return try {
            val response = apiService.login(LoginRequest(username, password))
            tokenPrefs.edit().putString("token", response.token).apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(username: String, email: String, password: String): Result<Unit> {
        return try {
            apiService.register(RegisterRequest(username, email, password))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        tokenPrefs.edit().remove("token").apply()
    }

    fun isLoggedIn(): Boolean {
        return tokenPrefs.getString("token", null) != null
    }
}