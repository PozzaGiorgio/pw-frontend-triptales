package com.example.triptales.data.repository

import android.content.Context
import com.example.triptales.data.api.ApiService
import com.example.triptales.data.model.Comment
import com.example.triptales.data.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

class PostRepository(private val apiService: ApiService) {
    suspend fun getPosts(tripId: Int): Result<List<Post>> {
        return try {
            val posts = apiService.getPosts(tripId)
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPost(
        tripId: Int,
        content: String,
        imageFile: File,
        latitude: Double?,
        longitude: Double?,
        locationName: String?,
        ocrText: String?,
        translatedText: String?,
        objectTags: List<String>?,
        smartCaption: String?
    ): Result<Post> {
        return try {
            // Prepare data
            val tripPart = tripId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val contentPart = content.toRequestBody("text/plain".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData(
                "image",
                imageFile.name,
                imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            )

            // Optional parts
            val latitudePart = latitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val longitudePart = longitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val locationNamePart = locationName?.toRequestBody("text/plain".toMediaTypeOrNull())
            val ocrTextPart = ocrText?.toRequestBody("text/plain".toMediaTypeOrNull())
            val translatedTextPart = translatedText?.toRequestBody("text/plain".toMediaTypeOrNull())
            val objectTagsPart = objectTags?.let {
                JSONObject().put("tags", objectTags).toString().toRequestBody("application/json".toMediaTypeOrNull())
            }
            val smartCaptionPart = smartCaption?.toRequestBody("text/plain".toMediaTypeOrNull())

            // Send request
            val post = apiService.createPost(
                trip = tripPart,
                content = contentPart,
                image = imagePart,
                latitude = latitudePart,
                longitude = longitudePart,
                locationName = locationNamePart,
                ocrText = ocrTextPart,
                translatedText = translatedTextPart,
                objectTags = objectTagsPart,
                smartCaption = smartCaptionPart
            )

            Result.success(post)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun likePost(postId: Int): Result<Unit> {
        return try {
            apiService.likePost(postId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun commentPost(postId: Int, content: String): Result<Comment> {
        return try {
            val comment = apiService.commentPost(postId, mapOf("content" to content))
            Result.success(comment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}