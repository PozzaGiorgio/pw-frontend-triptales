package com.example.triptales.data.model

data class LoginRequest(val username: String, val password: String)

data class LoginResponse(val auth_token: String)  // Cambiato da "token" a "auth_token"

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)