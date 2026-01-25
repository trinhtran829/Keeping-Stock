package com.example.keepingstock.core.contracts

data class CapturedImage(val localPath: String)

data class ImageLabelResult(
    val suggestedName: String? = null,
    val suggestedTags: List<Tag> = emptyList(),
    val rawLabels: List<String> = emptyList()
)

interface CameraService {
    suspend fun captureImage(): CapturedImage
}

interface QrService {
    suspend fun scanContainerQr(): ContainerId
    suspend fun generateContainerQr(containerId: ContainerId): String
}

interface ImageLabelService {
    suspend fun labelImage(imagePath: String): ImageLabelResult
}