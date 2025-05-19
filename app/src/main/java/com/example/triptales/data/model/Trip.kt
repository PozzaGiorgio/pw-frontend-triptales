package com.example.triptales.data.model

data class Trip(
    val id: Int,
    val name: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val createdBy: User,
    val members: List<User>
)