package com.example.triptales.ui.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.triptales.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostDetailViewModel(private val postRepository: PostRepository) : ViewModel() {

    private val _postState = MutableStateFlow<PostDetailState>(PostDetailState.Loading)
    val postState: StateFlow<PostDetailState> = _postState

    private val _commentState = MutableStateFlow<CommentState>(CommentState.Idle)
    val commentState: StateFlow<CommentState> = _commentState

    fun getPostDetails(postId: Int) {
        viewModelScope.launch {
            _postState.value = PostDetailState.Loading

            postRepository.getPostById(postId).fold(
                onSuccess = { post ->
                    _postState.value = PostDetailState.Success(post)
                },
                onFailure = { e ->
                    _postState.value = PostDetailState.Error(e.message ?: "Failed to load post details")
                }
            )
        }
    }

    fun addComment(postId: Int, content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _commentState.value = CommentState.Loading

            postRepository.commentPost(postId, content).fold(
                onSuccess = { comment ->
                    _commentState.value = CommentState.Success(comment)

                    // 🔧 MODIFICA: Gestisce il caso di currentPost null o con campi null
                    val currentPostState = _postState.value
                    if (currentPostState is PostDetailState.Success) {
                        val currentPost = currentPostState.post
                        try {
                            // Aggiorna lo stato del post con il nuovo commento
                            val updatedComments = currentPost.comments + comment
                            val updatedPost = currentPost.copy(comments = updatedComments)
                            _postState.value = PostDetailState.Success(updatedPost)
                        } catch (e: Exception) {
                            // Se la copia fallisce, ricarica il post dal server
                            android.util.Log.e("PostDetailViewModel", "Failed to update post with new comment, reloading", e)
                            getPostDetails(postId)
                        }
                    }
                },
                onFailure = { e ->
                    _commentState.value = CommentState.Error(e.message ?: "Failed to add comment")
                }
            )
        }
    }
}