package com.keepingstock.core.ml

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.tasks.await

/**
 * Returns a list of top labels for a given photo URI.
 */
suspend fun getImageLabels(context: Context, photoUri: Uri): List<String> {
    val image = InputImage.fromFilePath(context, photoUri)
    val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    val labels = labeler.process(image).await()
    return labels.map { it.text } // You can also use it.confidence if needed
}

