package com.example.triptales.ui.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.triptales.data.model.Trip
import com.example.triptales.data.model.User

@Composable
fun TripHeader(trip: Trip, onJoinClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Titolo del viaggio
            Text(
                text = trip.name ?: "Untitled Trip",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Date del viaggio
            Text(
                text = "${trip.startDate ?: "Unknown"} - ${trip.endDate ?: "Unknown"}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Descrizione del viaggio
            Text(
                text = trip.description ?: "No description available",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Creatore del viaggio
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(user = trip.createdBy)

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = "Created by: ${trip.createdBy.username ?: "Unknown User"}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Membri del viaggio
            Text(
                text = "Members (${trip.members.size}):",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Lista dei membri (mostra solo i primi 5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy((-8).dp)
            ) {
                trip.members.take(5).forEach { member ->
                    UserAvatar(
                        user = member,
                        modifier = Modifier.size(32.dp)
                    )
                }

                if (trip.members.size > 5) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            .align(Alignment.CenterVertically),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+${trip.members.size - 5}")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🔧 MIGLIORAMENTO: Pulsante più informativo
            Button(
                onClick = onJoinClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Join This Trip")
            }

            // 🔧 AGGIUNTO: Testo informativo
            Text(
                text = "Click 'Join This Trip' to become a member and see posts from this trip",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun UserAvatar(user: User, modifier: Modifier = Modifier.size(40.dp)) {
    if (user.profileImage != null && user.profileImage.isNotBlank()) {  // 🔧 Verifica che non sia vuota
        AsyncImage(
            model = user.profileImage,
            contentDescription = user.username ?: "User Avatar",  // 🔧 Gestisce null
            modifier = modifier.clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        // Placeholder per utenti senza immagine
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),  // 🔧 Aggiunto background
            contentAlignment = Alignment.Center
        ) {
            val username = user.username ?: "U"  // 🔧 Gestisce null
            val firstChar = if (username.isNotEmpty()) username.first().toString() else "U"  // 🔧 Gestisce stringa vuota
            Text(
                text = firstChar,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}