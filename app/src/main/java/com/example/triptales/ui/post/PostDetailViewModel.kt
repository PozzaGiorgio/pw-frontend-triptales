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

                    // Aggiorna lo stato del post con il nuovo commento
                    val currentPost = (_postState.value as? PostDetailState.Success)?.post ?: return@fold
                    val updatedComments = currentPost.comments + comment
                    _postState.value = PostDetailState.Success(currentPost.copy(comments = updatedComments))
                },
                onFailure = { e ->
                    _commentState.value = CommentState.Error(e.message ?: "Failed to add comment")
                }
            )
        }
    }
}