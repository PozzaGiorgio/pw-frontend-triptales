package com.example.triptales.data.model

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonElement

data class Post(
    val id: Int,
    val trip: Int,
    val user: User,
    val content: String?,
    val image: String?,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("location_name")
    val locationName: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("ocr_text")
    val ocrText: String?,
    @SerializedName("translated_text")
    val translatedText: String?,
    @SerializedName("object_tags")
    private val _objectTags: JsonElement?, // 🔧 Usa JsonElement per gestire formati diversi
    @SerializedName("smart_caption")
    val smartCaption: String?,
    val comments: List<Comment>,
    @SerializedName("likes_count")
    val likesCount: Int
) {
    // 🔧 Proprietà computata per ottenere i tag come lista di stringhe
    val objectTags: List<String>?
        get() {
            return try {
                when {
                    _objectTags == null -> null
                    _objectTags.isJsonArray -> {
                        // Se è già un array
                        _objectTags.asJsonArray.map { it.asString }
                    }
                    _objectTags.isJsonObject -> {
                        // Se è un oggetto con campo "tags"
                        val tagsElement = _objectTags.asJsonObject.get("tags")
                        if (tagsElement != null && !tagsElement.isJsonNull) {
                            val tagsString = tagsElement.asString
                            // Parsing della stringa "[Wall, Pattern, Metal]"
                            tagsString
                                .removePrefix("[")
                                .removeSuffix("]")
                                .split(",")
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                        } else {
                            null
                        }
                    }
                    else -> null
                }
            } catch (e: Exception) {
                // In caso di errore, restituisce null
                null
            }
        }
}