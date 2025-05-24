package com.example.triptales.ui.trip

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.triptales.data.model.Post
import com.example.triptales.data.model.Trip
import com.example.triptales.data.repository.PostRepository
import com.example.triptales.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Stati per i dettagli del viaggio
sealed class TripState {
    object Loading : TripState()
    data class Success(val trip: Trip) : TripState()
    data class Error(val message: String) : TripState()
}

// Stati per i post del viaggio
sealed class PostsState {
    object Loading : PostsState()
    data class Success(val posts: List<Post>) : PostsState()
    data class Error(val message: String) : PostsState()
}

// Stati per l'azione di join
sealed class JoinTripState {
    object Idle : JoinTripState()
    object Loading : JoinTripState()
    object Success : JoinTripState()
    data class Error(val message: String) : JoinTripState()
}

/**
 * ViewModel per la schermata di dettaglio di un viaggio
 */
class TripDetailViewModel(
    private val tripRepository: TripRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    // Stato per i dettagli del viaggio
    private val _tripState = MutableStateFlow<TripState>(TripState.Loading)
    val tripState: StateFlow<TripState> = _tripState

    // Stato per i post del viaggio
    private val _postsState = MutableStateFlow<PostsState>(PostsState.Loading)
    val postsState: StateFlow<PostsState> = _postsState

    // Stato per l'azione di join
    private val _joinTripState = MutableStateFlow<JoinTripState>(JoinTripState.Idle)
    val joinTripState: StateFlow<JoinTripState> = _joinTripState

    /**
     * Carica i dettagli di un viaggio
     */
    fun getTripDetails(tripId: Int) {
        viewModelScope.launch {
            _tripState.value = TripState.Loading
            Log.d("TripDetailViewModel", "Fetching trip details for ID: $tripId")

            tripRepository.getTripById(tripId).fold(
                onSuccess = { trip ->
                    Log.d("TripDetailViewModel", "Trip fetched successfully: ${trip.name}")
                    Log.d("TripDetailViewModel", "Trip members: ${trip.members.size}")
                    _tripState.value = TripState.Success(trip)
                },
                onFailure = { e ->
                    Log.e("TripDetailViewModel", "Failed to fetch trip", e)
                    _tripState.value = TripState.Error(e.message ?: "Failed to load trip details")
                }
            )
        }
    }

    /**
     * Carica i post di un viaggio
     */
    fun getTripPosts(tripId: Int) {
        viewModelScope.launch {
            _postsState.value = PostsState.Loading
            Log.d("TripDetailViewModel", "Fetching posts for trip ID: $tripId")

            postRepository.getPosts(tripId).fold(
                onSuccess = { posts ->
                    Log.d("TripDetailViewModel", "Posts fetched successfully: ${posts.size} posts")
                    _postsState.value = PostsState.Success(posts)
                },
                onFailure = { e ->
                    Log.e("TripDetailViewModel", "Failed to fetch posts", e)
                    _postsState.value = PostsState.Error(e.message ?: "Failed to load posts")
                }
            )
        }
    }

    /**
     * Partecipa a un viaggio
     */
    fun joinTrip(tripId: Int) {
        viewModelScope.launch {
            _joinTripState.value = JoinTripState.Loading
            Log.d("TripDetailViewModel", "Joining trip: $tripId")

            tripRepository.joinTrip(tripId).fold(
                onSuccess = {
                    Log.d("TripDetailViewModel", "Successfully joined trip: $tripId")
                    _joinTripState.value = JoinTripState.Success

                    // 🔧 MODIFICA: Ricarica sia i dettagli del viaggio che i post
                    getTripDetails(tripId)
                    getTripPosts(tripId)
                },
                onFailure = { e ->
                    Log.e("TripDetailViewModel", "Failed to join trip", e)
                    _joinTripState.value = JoinTripState.Error(e.message ?: "Failed to join trip")
                }
            )
        }
    }

    /**
     * Mette like a un post
     */
    fun likePost(postId: Int) {
        viewModelScope.launch {
            postRepository.likePost(postId).fold(
                onSuccess = {
                    Log.d("TripDetailViewModel", "Successfully liked post: $postId")
                    // Aggiorna il post con il like
                    val currentPosts = (_postsState.value as? PostsState.Success)?.posts ?: return@fold
                    val updatedPosts = currentPosts.map { post ->
                        if (post.id == postId) {
                            // Incrementa il conteggio dei like (questo è un approccio ottimistico)
                            post.copy(likesCount = post.likesCount + 1)
                        } else {
                            post
                        }
                    }
                    _postsState.value = PostsState.Success(updatedPosts)
                },
                onFailure = { e ->
                    Log.e("TripDetailViewModel", "Failed to like post", e)
                    // Gestisci l'errore se necessario
                }
            )
        }
    }

    /**
     * Ricarica tutto (utile per pull-to-refresh)
     */
    fun refreshAll(tripId: Int) {
        getTripDetails(tripId)
        getTripPosts(tripId)
    }
}