package com.example.triptales.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.triptales.data.model.Post
import com.example.triptales.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Stati per i post sulla mappa
sealed class PostsState {
    object Loading : PostsState()
    data class Success(val posts: List<Post>) : PostsState()
    data class Error(val message: String) : PostsState()
}

class TripMapViewModel(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _postsState = MutableStateFlow<PostsState>(PostsState.Loading)
    val postsState: StateFlow<PostsState> = _postsState

    fun loadPosts(tripId: Int) {
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
}