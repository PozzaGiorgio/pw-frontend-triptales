package com.example.triptales.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val posts = (postsState as? PostsState.Success)?.posts ?: emptyList()
            val filteredPosts = posts.filter { it.latitude != null && it.longitude != null }

            if (filteredPosts.isNotEmpty()) {
                val cameraPositionState = rememberCameraPositionState {
                    // Set initial camera position to the location of the first post
                    position = CameraPosition.fromLatLngZoom(
                        LatLng(
                            filteredPosts.first().latitude!!,
                            filteredPosts.first().longitude!!
                        ),
                        10f
                    )
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    filteredPosts.forEach { post ->
                        post.latitude?.let { lat ->
                            post.longitude?.let { lng ->
                                // 🔧 CORREZIONE: Gestisci il content nullable
                                val content = post.content ?: ""
                                val snippet = if (content.length > 50) {
                                    content.take(50) + "..."
                                } else {
                                    content
                                }

                                Marker(
                                    state = MarkerState(
                                        position = LatLng(lat, lng)
                                    ),
                                    title = post.locationName ?: "Memory",
                                    snippet = snippet,
                                    onClick = {
                                        navController.navigate("post/${post.id}")
                                        true
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No locations to display on the map yet.")
                }
            }

            when (postsState) {
                is PostsState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is PostsState.Error -> {
                    Text(
                        text = (postsState as PostsState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp)
                    )
                }
                else -> { /* success state handled above */ }
            }
        }
    }
}