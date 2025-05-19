package com.example.triptales.ui.trip

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
                text = trip.name,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Date del viaggio
            Text(
                text = "${trip.startDate} - ${trip.endDate}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Descrizione del viaggio
            Text(
                text = trip.description,
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
                    text = "Created by: ${trip.createdBy.username}",
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
                            .align(Alignment.CenterVertically),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+${trip.members.size - 5}")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pulsante per unirsi al viaggio
            Button(
                onClick = onJoinClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Join Trip")
            }
        }
    }
}

@Composable
fun UserAvatar(user: User, modifier: Modifier = Modifier.size(40.dp)) {
    if (user.profileImage != null) {
        AsyncImage(
            model = user.profileImage,
            contentDescription = user.username,
            modifier = modifier.clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        // Placeholder per utenti senza immagine
        Box(
            modifier = modifier
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(user.username.first().toString())
        }
    }
}