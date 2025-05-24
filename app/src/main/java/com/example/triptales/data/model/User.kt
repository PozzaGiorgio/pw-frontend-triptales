package com.example.triptales.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val username: String?, // 🔧 Nullable per gestire usernames mancanti
    val email: String?,
    @SerializedName("profile_image")
    val profileImage: String?
)