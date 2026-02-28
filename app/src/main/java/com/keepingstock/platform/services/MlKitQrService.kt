package com.keepingstock.platform.services

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.QrService
import kotlinx.coroutines.tasks.await

class MlKitQrService(private val context: Context) : QrService {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    override suspend fun scanContainerQr(): ContainerId {
        return ContainerId(-1L)
    }

    @OptIn(ExperimentalGetImage::class)
    suspend fun analyzeImage(imageProxy: ImageProxy): ContainerId? {
        val mediaImage = imageProxy.image ?: return null
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        return try {
            val barcodes = scanner.process(image).await()
            for (barcode in barcodes) {
                val rawValue = barcode.rawValue ?: continue
                if (rawValue.startsWith("keepingstock://container/")) {
                    val idString = rawValue.substringAfter("keepingstock://container/")
                    return ContainerId(idString.toLongOrNull() ?: continue)
                }
            }
            null
        } catch (e: Exception) {
            null
        } finally {
            imageProxy.close()
        }
    }

    override fun generateContainerQr(containerId: ContainerId): String {
        return "keepingstock://container/${containerId.value}"
    }

    fun generateQrBitmap(containerId: ContainerId, size: Int = 512): Bitmap? {
        val content = generateContainerQr(containerId)
        return try {
            val bitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}
