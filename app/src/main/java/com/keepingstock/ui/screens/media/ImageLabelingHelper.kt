package com.keepingstock.core.ml

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.tasks.await

/**
 * Returns a list of top labels for a given photo URI using standard Image Labeling.
 * Standard labeling is good for general scenes and high-level categories.
 */
suspend fun getImageLabels(
    context: Context,
    photoUri: Uri,
    confidenceThreshold: Float = 0.4f
): List<String> {
    val image = InputImage.fromFilePath(context, photoUri)
    val options = ImageLabelerOptions.Builder()
        .setConfidenceThreshold(confidenceThreshold)
        .build()
    val labeler = ImageLabeling.getClient(options)
    val labels = labeler.process(image).await()
    return labels.map { it.text }
}

/**
 * Returns labels for objects detected in the image.
 * Object detection is better for identifying specific items (like scissors, tools, etc.)
 * because it first localizes the object before classifying it.
 */
suspend fun getObjectLabels(
    context: Context,
    photoUri: Uri,
    confidenceThreshold: Float = 0.4f
): List<String> {
    val image = InputImage.fromFilePath(context, photoUri)

    // Use Single Image Mode with Classification enabled
    val options = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
        .enableMultipleObjects()
        .enableClassification()
        .build()

    val objectDetector = ObjectDetection.getClient(options)
    val detectedObjects = objectDetector.process(image).await()

    return detectedObjects.flatMap { detectedObject ->
        detectedObject.labels
            .filter { it.confidence >= confidenceThreshold }
            .map { it.text }
    }.distinct()
}

/**
 * Combined approach: Tries object detection first (more precise for items),
 * and falls back to or supplements with general image labeling.
 */
suspend fun getEnhancedLabels(context: Context, photoUri: Uri): List<String> {
    val objectLabels = getObjectLabels(context, photoUri)
    if (objectLabels.isNotEmpty()) {
        return objectLabels
    }
    return getImageLabels(context, photoUri)
}
