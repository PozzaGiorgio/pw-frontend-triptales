package com.example.triptales.data.model

import com.google.gson.annotations.SerializedName

data class Trip(
    val id: Int,
    val name: String,
    val description: String,
    @SerializedName("start_date")
    val startDate: String,  // Mantieni come String per evitare problemi di parsing
    @SerializedName("end_date")
    val endDate: String,    // Mantieni come String
    @SerializedName("created_by")
    val createdBy: User,
    val members: List<User>,
    @SerializedName("created_at")
    val createdAt: String? = null  // Opzionale per la creazione
)