package com.example.triptales.data.model

data class Post(
    val id: Int,
    val trip: Int,
    val user: User,
    val content: String,
    val image: String?,
    val latitude: Double?,
    val longitude: Double?,
    val locationName: String?,
    val createdAt: String,
    val ocrText: String?,
    val translatedText: String?,
    val objectTags: List<String>?,
    val smartCaption: String?,
    val comments: List<Comment>,
    val likesCount: Int
)