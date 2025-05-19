package com.example.triptales.ui.post



import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.triptales.data.repository.PostRepository
import com.example.triptales.util.MLProcessingResult
import com.example.triptales.util.MlKitService
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

// Stati per l'elaborazione ML
sealed class MlProcessingState {
    object Idle : MlProcessingState()
    object Loading : MlProcessingState()
    data class Success(val result: MLProcessingResult) : MlProcessingState()
    data  class Error(val message: String) : MlProcessingState()
}

// Stati per la creazione di un post
sealed class CreatePostState {
    object Idle : CreatePostState()
    object Loading : CreatePostState()
    object Success : CreatePostState()
    data class Error(val message: String) : CreatePostState()
}

class CreatePostViewModel(
    private val postRepository: PostRepository,
    private val mlKitService: MlKitService,
    private val context: Context
) : ViewModel() {

    // Stati per ML Kit e creazione post
    private val _mlProcessingState = MutableStateFlow<MlProcessingState>(MlProcessingState.Idle)
    val mlProcessingState: StateFlow<MlProcessingState> = _mlProcessingState

    private val _createPostState = MutableStateFlow<CreatePostState>(CreatePostState.Idle)
    val createPostState: StateFlow<CreatePostState> = _createPostState

    // Info di localizzazione
    val locationName = mutableStateOf<String?>(null)
    private var latitude: Double? = null
    private var longitude: Double? = null

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // Variabili per i risultati ML
    private var ocrText: String? = null
    private var translatedText: String? = null
    private var objectTags: List<String>? = null
    private var smartCaption: String? = null

    // Ottiene la posizione attuale
    fun getCurrentLocation(hasPermission: Boolean) {
        if (!hasPermission) return

        viewModelScope.launch {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        latitude = it.latitude
                        longitude = it.longitude

                        // Ottieni nome località (geocoding)
                        // Implementazione geocoding qui...
                        locationName.value = "Current Location" // Placeholder
                    }
                }
            } catch (e: Exception) {
                // Gestione errori
            }
        }
    }

    // Elabora l'immagine con ML Kit
    fun processImage(uri: Uri) {
        viewModelScope.launch {
            _mlProcessingState.value = MlProcessingState.Loading

            try {
                // Converti Uri in Bitmap
                val bitmap = context.contentResolver.openInputStream(uri)?.use { input ->
                    android.graphics.BitmapFactory.decodeStream(input)
                } ?: throw Exception("Failed to decode image")

                // Usa MLKitService per elaborare l'immagine
                val result = mlKitService.processImageBitmap(bitmap)

                // Salva i risultati
                ocrText = result.ocrText
                translatedText = result.translatedText
                objectTags = result.objectTags
                smartCaption = result.smartCaption

                _mlProcessingState.value = MlProcessingState.Success(result)

            } catch (e: Exception) {
                _mlProcessingState.value = MlProcessingState.Error(e.message ?: "Failed to process image")
            }
        }
    }

    // Crea un post
    fun createPost(tripId: Int, content: String, imageUri: Uri) {
        viewModelScope.launch {
            _createPostState.value = CreatePostState.Loading

            try {
                // Crea file temporaneo dall'Uri
                val imageFile = createTempFileFromUri(imageUri)

                // Crea post tramite repository
                postRepository.createPost(
                    tripId = tripId,
                    content = content,
                    imageFile = imageFile,
                    latitude = latitude,
                    longitude = longitude,
                    locationName = locationName.value,
                    ocrText = ocrText,
                    translatedText = translatedText,
                    objectTags = objectTags,
                    smartCaption = smartCaption
                ).fold(
                    onSuccess = {
                        _createPostState.value = CreatePostState.Success
                    },
                    onFailure = { e ->
                        _createPostState.value = CreatePostState.Error(e.message ?: "Failed to create post")
                    }
                )

            } catch (e: Exception) {
                _createPostState.value = CreatePostState.Error(e.message ?: "Failed to create post")
            }
        }
    }

    // Utility per creare un file temporaneo da Uri
    private suspend fun createTempFileFromUri(uri: Uri): File {
        return File.createTempFile("post_image", ".jpg", context.cacheDir).apply {
            context.contentResolver.openInputStream(uri)?.use { input ->
                outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}