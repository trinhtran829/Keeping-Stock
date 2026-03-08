package com.keepingstock.platform.services

import android.content.Context
import android.net.Uri
import com.keepingstock.core.contracts.ImageLabelResult
import com.keepingstock.core.contracts.ImageLabelService
import com.keepingstock.ui.screens.media.getEnhancedLabels

class MlKitImageLabelService(
    private val context: Context
) : ImageLabelService {

    override suspend fun labelImage(imagePath: String): ImageLabelResult {
        val labels = try {
            getEnhancedLabels(context, Uri.parse(imagePath))
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinctBy { it.lowercase() }
        } catch (_: Exception) {
            emptyList()
        }

        return ImageLabelResult(
            suggestedName = labels.firstOrNull(),
            suggestedTags = emptyList(),
            rawLabels = labels
        )
    }
}