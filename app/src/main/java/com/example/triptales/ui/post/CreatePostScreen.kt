package com.example.triptales.ui.post

import android.Manifest
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.triptales.ui.components.CameraView
import com.example.triptales.ui.components.Chip
import com.example.triptales.ui.components.FlowRow
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CreatePostScreen(navController: NavHostController, tripId: Int) {
    val viewModel: CreatePostViewModel = koinViewModel()
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val locationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)

    var content by remember { mutableStateOf("") }
    var showCamera by remember { mutableStateOf(false) }
    val capturedImageUri = remember { mutableStateOf<Uri?>(null) }
    val mlProcessingState by viewModel.mlProcessingState.collectAsState()
    val createPostState by viewModel.createPostState.collectAsState()

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
        viewModel.getCurrentLocation(locationPermissionState.status.isGranted)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Memory") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (showCamera && cameraPermissionState.status.isGranted) {
            CameraView(
                onImageCaptured = { uri ->
                    capturedImageUri.value = uri
                    showCamera = false
                    // Process image with ML Kit
                    viewModel.processImage(uri)
                },
                onError = { /* Handle error */ }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Image preview
                capturedImageUri.value?.let { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = { showCamera = true }) {
                            Text("Retake")
                        }

                        Button(
                            onClick = { capturedImageUri.value = null },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Remove")
                        }
                    }
                } ?: run {
                    Button(
                        onClick = { showCamera = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Camera, contentDescription = "Camera")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Take Photo")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ML Kit results
                when (val state = mlProcessingState) {
                    is MlProcessingState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Text(
                                    "Processing image...",
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                    is MlProcessingState.Success -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "AI Analysis Results",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                state.result.ocrText?.let {
                                    if (it.isNotBlank()) {
                                        Text(
                                            "OCR Text:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(it)
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }

                                state.result.translatedText?.let {
                                    if (it.isNotBlank()) {
                                        Text(
                                            "Translation:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(it)
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }

                                state.result.objectTags?.let { tags ->
                                    if (tags.isNotEmpty()) {
                                        Text(
                                            "Objects Detected:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        FlowRow {
                                            tags.forEach { tag ->
                                                Chip(
                                                    label = { Text(tag) },
                                                    modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }

                                state.result.smartCaption?.let {
                                    if (it.isNotBlank()) {
                                        Text(
                                            "AI Caption:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(it)
                                    }
                                }
                            }
                        }
                    }
                    else -> { /* idle state */ }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content input
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Tell your story...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Location info
                viewModel.locationName.value?.let { locationName ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Location")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(locationName)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Create post button
                Button(
                    onClick = {
                        capturedImageUri.value?.let { uri ->
                            viewModel.createPost(
                                tripId = tripId,
                                content = content,
                                imageUri = uri
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = content.isNotBlank() && capturedImageUri.value != null
                ) {
                    Text("Share Memory")
                }

                // Handle create post state
                when (val state = createPostState) {
                    is CreatePostState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                    is CreatePostState.Success -> {
                        LaunchedEffect(Unit) {
                            navController.navigateUp()
                        }
                    }
                    is CreatePostState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    else -> { /* idle state */ }
                }
            }
        }
    }
}