package com.example.triptales.data.model

import com.google.gson.annotations.SerializedName

data class Comment(
    val id: Int,
    val post: Int,
    val user: User,
    val content: String?, // 🔧 Nullable per gestire contenuti vuoti
    @SerializedName("created_at")
    val createdAt: String? // 🔧 Nullable per gestire date mancanti
)