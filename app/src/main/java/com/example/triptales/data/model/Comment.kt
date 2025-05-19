package com.example.triptales.data.model

data class Comment(
    val id: Int,
    val post: Int,
    val user: User,
    val content: String,
    val createdAt: String
)