package com.example.triptales.ui.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.triptales.data.model.Post
import com.example.triptales.ui.trip.UserAvatar

@Composable
fun PostCard(
    post: Post,
    onClick: () -> Unit,
    onLikeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            // Immagine del post (se presente)
            post.image?.let { imageUrl ->
                if (imageUrl.isNotBlank()) {  // 🔧 Verifica che non sia vuota
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Intestazione con info utente
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(user = post.user, modifier = Modifier.size(36.dp))

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = post.user.username ?: "Unknown User",  // 🔧 Gestisce null
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "Posted on ${post.createdAt ?: "Unknown Date"}",  // 🔧 Gestisce null
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Contenuto del post
                Text(
                    text = post.content ?: "",  // 🔧 QUESTA È LA RIGA 75 - Gestisce null
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                // Posizione (se presente)
                post.locationName?.let { location ->
                    if (location.isNotBlank()) {  // 🔧 Verifica che non sia vuota
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Location",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(2.dp))

                            Text(
                                text = location,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Footer con like e commenti
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pulsante like
                    IconButton(
                        onClick = onLikeClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (post.likesCount > 0) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.likesCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "${post.likesCount} likes",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Commenti
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "${post.comments.size} comments",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // AI Caption se presente
                post.smartCaption?.let { caption ->
                    if (caption.isNotBlank()) {  // 🔧 Verifica che non sia vuota
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🤖 $caption",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}