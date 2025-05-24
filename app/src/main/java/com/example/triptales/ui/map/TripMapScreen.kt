package com.example.triptales.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.triptales.ui.map.TripMapViewModel
import com.example.triptales.ui.map.PostsState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripMapScreen(navController: NavHostController, tripId: Int) {
    val viewModel: TripMapViewModel = koinViewModel()
    val postsState by viewModel.postsState.collectAsState()

    LaunchedEffect(tripId) {
        android.util.Log.d("TripMapScreen", "Loading posts for trip: $tripId")
        viewModel.loadPosts(tripId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip Map") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                // 🔧 AGGIUNTO: Pulsante refresh nella top bar
                actions = {
                    IconButton(
                        onClick = {
                            android.util.Log.d("TripMapScreen", "Refreshing posts...")
                            viewModel.loadPosts(tripId)
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = postsState) {
                is PostsState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Loading memories...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                is PostsState.Success -> {
                    val posts = state.posts
                    val filteredPosts = posts.filter { it.latitude != null && it.longitude != null }

                    // 🔧 DEBUG: Log informazioni sui post con più dettagli
                    LaunchedEffect(posts) {
                        android.util.Log.d("TripMapScreen", "=== TRIP MAP DEBUG ===")
                        android.util.Log.d("TripMapScreen", "Total posts: ${posts.size}")
                        android.util.Log.d("TripMapScreen", "Posts with location: ${filteredPosts.size}")
                        android.util.Log.d("TripMapScreen", "Posts without location: ${posts.size - filteredPosts.size}")

                        posts.forEachIndexed { index, post ->
                            android.util.Log.d("TripMapScreen",
                                "Post $index - ID: ${post.id}, lat: ${post.latitude}, lng: ${post.longitude}, location: '${post.locationName}', content: '${post.content?.take(30)}...'")
                        }
                        android.util.Log.d("TripMapScreen", "=====================")
                    }

                    if (filteredPosts.isNotEmpty()) {
                        // 🔧 MIGLIORATO: Gestione migliore della camera con bounds
                        val cameraPositionState = rememberCameraPositionState {
                            // Calcola il centro delle coordinate se ci sono più post
                            val avgLat = filteredPosts.map { it.latitude!! }.average()
                            val avgLng = filteredPosts.map { it.longitude!! }.average()

                            position = CameraPosition.fromLatLngZoom(
                                LatLng(avgLat, avgLng),
                                if (filteredPosts.size == 1) 15f else 10f // Zoom più vicino per un singolo post
                            )
                        }

                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState
                        ) {
                            filteredPosts.forEachIndexed { index, post ->
                                post.latitude?.let { lat ->
                                    post.longitude?.let { lng ->
                                        val content = post.content ?: ""
                                        val snippet = if (content.length > 50) {
                                            content.take(50) + "..."
                                        } else {
                                            content.ifBlank { "No description" }
                                        }

                                        Marker(
                                            state = MarkerState(
                                                position = LatLng(lat, lng)
                                            ),
                                            title = post.locationName ?: "Memory ${index + 1}",
                                            snippet = snippet,
                                            onClick = {
                                                android.util.Log.d("TripMapScreen", "Marker clicked for post: ${post.id}")
                                                navController.navigate("post/${post.id}")
                                                true
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // 🔧 AGGIUNTO: Badge con numero di locations nella mappa
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${filteredPosts.size} locations",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    } else {
                        // Messaggio quando non ci sono locations
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "No locations to display",
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (posts.isEmpty()) {
                                Text(
                                    "This trip has no memories yet.\nCreate your first memory to see it on the map!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            } else {
                                Text(
                                    "This trip has ${posts.size} memories, but none have location data.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    "To see memories on the map:",
                                    style = MaterialTheme.typography.titleSmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "1. Enable location permission\n2. Create new memories with GPS enabled",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    android.util.Log.d("TripMapScreen", "Navigate to create post for trip: $tripId")
                                    navController.navigate("create_post/$tripId")
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create Memory")
                            }

                            // 🔧 AGGIUNTO: Pulsante refresh anche qui
                            if (posts.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.loadPosts(tripId) },
                                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Refresh")
                                }
                            }
                        }
                    }
                }
                is PostsState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Error loading memories",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                android.util.Log.d("TripMapScreen", "Retrying to load posts...")
                                viewModel.loadPosts(tripId)
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}