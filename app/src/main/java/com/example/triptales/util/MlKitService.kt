package com.example.triptales.util

import android.graphics.Bitmap
import android.media.Image
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import android.annotation.SuppressLint

class MlKitService {
    // Inizializza i servizi ML Kit
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val imageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .build()
    )
    private val translator = Translation.getClient(
        TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.ITALIAN)
            .build()
    )

    @SuppressLint("UnsafeOptInUsageError")
    suspend fun recognizeText(imageProxy: ImageProxy): String? {
        return suspendCancellableCoroutine { continuation ->
            val mediaImage = imageProxy.image ?: run {
                continuation.resume(null)
                imageProxy.close()
                return@suspendCancellableCoroutine
            }

            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    continuation.resume(visionText.text)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    suspend fun translateText(text: String): String? {
        return suspendCancellableCoroutine { continuation ->
            translator.translate(text)
                .addOnSuccessListener { translatedText ->
                    continuation.resume(translatedText)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    suspend fun labelImage(imageProxy: ImageProxy): List<String>? {
        return suspendCancellableCoroutine { continuation ->
            val mediaImage = imageProxy.image ?: run {
                continuation.resume(null)
                imageProxy.close()
                return@suspendCancellableCoroutine
            }

            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            imageLabeler.process(image)
                .addOnSuccessListener { labels ->
                    val labelTexts = labels.map { it.text }
                    continuation.resume(labelTexts)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    suspend fun detectFaces(imageProxy: ImageProxy): Int {
        return suspendCancellableCoroutine { continuation ->
            val mediaImage = imageProxy.image ?: run {
                continuation.resume(0)
                imageProxy.close()
                return@suspendCancellableCoroutine
            }

            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            faceDetector.process(image)
                .addOnSuccessListener { faces ->
                    continuation.resume(faces.size)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    // Metodo di utilità per processare un'immagine bitmap
    suspend fun processImageBitmap(bitmap: Bitmap): MLProcessingResult {
        val image = InputImage.fromBitmap(bitmap, 0)

        // Riconoscimento testo
        val ocrText = suspendCancellableCoroutine<String?> { continuation ->
            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    continuation.resume(visionText.text)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }

        // Traduzione (se necessario)
        val translatedText = if (!ocrText.isNullOrBlank()) {
            translateText(ocrText)
        } else null

        // Etichettatura oggetti
        val objectTags = suspendCancellableCoroutine<List<String>?> { continuation ->
            imageLabeler.process(image)
                .addOnSuccessListener { labels ->
                    val labelTexts = labels.map { it.text }
                    continuation.resume(labelTexts)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }

        // Genera una didascalia smart basata sugli oggetti riconosciuti
        val smartCaption = objectTags?.take(3)?.joinToString(", ")?.let { tags ->
            "This image appears to contain $tags"
        }

        return MLProcessingResult(
            ocrText = ocrText,
            translatedText = translatedText,
            objectTags = objectTags,
            smartCaption = smartCaption
        )
    }
}

// Classe per raccogliere i risultati del processing ML
data class MLProcessingResult(
    val ocrText: String?,
    val translatedText: String?,
    val objectTags: List<String>?,
    val smartCaption: String?
)