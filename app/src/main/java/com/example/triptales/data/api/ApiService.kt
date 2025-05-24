package com.example.triptales.data.api

import com.example.triptales.data.model.LoginRequest
import com.example.triptales.data.model.LoginResponse
import com.example.triptales.data.model.RegisterRequest
import com.example.triptales.data.model.User
import com.example.triptales.data.model.Trip
import com.example.triptales.data.model.Post
import com.example.triptales.data.model.Comment
import com.example.triptales.data.model.Badge
import com.example.triptales.data.repository.PaginatedResponse  // 🔧 AGGIUNGI QUESTO IMPORT
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // Auth - Usa Djoser endpoints
    @POST("auth/token/login/")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @POST("auth/users/")
    suspend fun register(@Body registerRequest: RegisterRequest)

    // User info
    @GET("auth/users/me/")
    suspend fun getCurrentUser(): User

    @GET("api/user-badges/")
    suspend fun getUserBadges(): List<Badge>

    // Trips
    @GET("api/trips/")
    suspend fun getTrips(): List<Trip>

    @POST("api/trips/")
    suspend fun createTrip(@Body trip: Trip): Trip

    @GET("api/trips/{id}/")
    suspend fun getTripById(@Path("id") id: Int): Trip

    @POST("api/trips/{id}/join/")
    suspend fun joinTrip(@Path("id") id: Int)

    @POST("api/trips/{id}/leave/")
    suspend fun leaveTrip(@Path("id") id: Int)

    // Posts - 🔧 SEZIONE AGGIORNATA
    @GET("api/posts/")
    suspend fun getPostsPaginated(@Query("trip") tripId: Int? = null): PaginatedResponse<Post>

    @GET("api/posts/{id}/")
    suspend fun getPostById(@Path("id") id: Int): Post

    @Multipart
    @POST("api/posts/")
    suspend fun createPost(
        @Part("trip") trip: RequestBody,
        @Part("content") content: RequestBody,
        @Part image: MultipartBody.Part?,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part("location_name") locationName: RequestBody?,
        @Part("ocr_text") ocrText: RequestBody?,
        @Part("translated_text") translatedText: RequestBody?,
        @Part("object_tags") objectTags: RequestBody?,
        @Part("smart_caption") smartCaption: RequestBody?
    ): Post

    @POST("api/posts/{id}/like/")
    suspend fun likePost(@Path("id") id: Int)

    @POST("api/posts/{id}/comment/")
    suspend fun commentPost(
        @Path("id") id: Int,
        @Body content: Map<String, String>
    ): Comment
}