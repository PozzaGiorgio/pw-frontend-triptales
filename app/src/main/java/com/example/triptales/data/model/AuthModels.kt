package com.example.triptales.data.model

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String)
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)