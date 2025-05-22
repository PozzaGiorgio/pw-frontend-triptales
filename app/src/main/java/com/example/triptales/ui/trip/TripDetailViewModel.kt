package com.example.triptales.ui.trip

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

    /**
     * Carica i dettagli di un viaggio
     */
    fun getTripDetails(tripId: Int) {
        viewModelScope.launch {
            _tripState.value = TripState.Loading

            tripRepository.getTripById(tripId).fold(
                onSuccess = { trip ->
                    _tripState.value = TripState.Success(trip)
                },
                onFailure = { e ->
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

            postRepository.getPosts(tripId).fold(
                onSuccess = { posts ->
                    _postsState.value = PostsState.Success(posts)
                },
                onFailure = { e ->
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
            tripRepository.joinTrip(tripId).fold(
                onSuccess = {
                    // Aggiorna i dettagli del viaggio dopo essersi unito
                    getTripDetails(tripId)
                },
                onFailure = { e ->
                    // Gestisci l'errore se necessario
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
                    // Gestisci l'errore se necessario
                }
            )
        }
    }
}