package com.example.triptales.ui.post

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.triptales.data.model.Comment
import com.example.triptales.data.model.Post
import org.koin.androidx.compose.koinViewModel

// Stati per il dettaglio di un post
sealed class PostDetailState {
    object Loading : PostDetailState()
    data class Success(val post: Post) : PostDetailState()
    data class Error(val message: String) : PostDetailState()
}

// Stati per l'aggiunta di un commento
sealed class CommentState {
    object Idle : CommentState()
    object Loading : CommentState()
    data class Success(val comment: Comment) : CommentState()
    data class Error(val message: String) : CommentState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(navController: NavHostController, postId: Int) {
    val viewModel: PostDetailViewModel = koinViewModel()
    val postState by viewModel.postState.collectAsState()
    val commentState by viewModel.commentState.collectAsState()

    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(postId) {
        viewModel.getPostDetails(postId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memory Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = postState) {
            is PostDetailState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is PostDetailState.Success -> {
                val post = state.post

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Contenuto del post (scrollabile)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // Immagine del post
                        item {
                            post.image?.let { imageUrl ->
                                if (imageUrl.isNotBlank()) {  // 🔧 Verifica che non sia vuota
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp)
                                            .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Info utente e data
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(
                                    text = post.user.username ?: "Unknown User",  // 🔧 Gestisce null
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = "Posted on ${post.createdAt ?: "Unknown Date"}",  // 🔧 Gestisce null
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Contenuto del post
                                Text(
                                    text = post.content ?: "No content",  // 🔧 Gestisce null
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                // Info posizione
                                post.locationName?.let { location ->
                                    if (location.isNotBlank()) {  // 🔧 Verifica che non sia vuota
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "📍 $location",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                // AI results
                                if (!post.ocrText.isNullOrBlank() || !post.translatedText.isNullOrBlank() ||
                                    !post.objectTags.isNullOrEmpty() || !post.smartCaption.isNullOrBlank()) {

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Divider()
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "AI Analysis",
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // OCR Text
                                    post.ocrText?.let {
                                        if (it.isNotBlank()) {
                                            Text(
                                                text = "Detected Text: $it",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }

                                    // Translated Text
                                    post.translatedText?.let {
                                        if (it.isNotBlank()) {
                                            Text(
                                                text = "Translation: $it",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }

                                    // Object Tags
                                    post.objectTags?.let { tags ->
                                        if (tags.isNotEmpty()) {
                                            Text(
                                                text = "Detected objects: ${tags.joinToString(", ")}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }

                                    // Smart Caption
                                    post.smartCaption?.let {
                                        if (it.isNotBlank()) {
                                            Text(
                                                text = "AI Caption: $it",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Comments (${post.comments.size})",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        // Commenti
                        if (post.comments.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No comments yet. Be the first to comment!")
                                }
                            }
                        } else {
                            items(post.comments) { comment ->
                                CommentItem(comment)
                            }
                        }
                    }

                    // Form per aggiungere commento
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            label = { Text("Add a comment") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (commentText.isNotBlank()) {
                                            viewModel.addComment(postId, commentText)
                                            commentText = ""
                                        }
                                    },
                                    enabled = commentText.isNotBlank()
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Send")
                                }
                            }
                        )

                        when (commentState) {
                            is CommentState.Loading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.CenterHorizontally)
                                )
                            }
                            is CommentState.Error -> {
                                Text(
                                    text = (commentState as CommentState.Error).message,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            else -> { /* idle or success */ }
                        }
                    }
                }
            }
            is PostDetailState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = comment.user.username ?: "Unknown User",  // 🔧 Gestisce null
            style = MaterialTheme.typography.titleSmall
        )

        Text(
            text = comment.content ?: "",  // 🔧 Gestisce null
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = comment.createdAt ?: "Unknown Date",  // 🔧 Gestisce null
            style = MaterialTheme.typography.bodySmall
        )

        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}