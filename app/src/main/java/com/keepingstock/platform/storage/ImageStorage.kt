package com.keepingstock.platform.storage

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * Copies an externally selected image into app-private storage and returns a stable file [Uri].
 *
 * The returned URI points to a file owned by the app, so it remains readable across process death
 * and device restarts without requiring continued access to the original content URI.
 *
 * @param context: Application or activity context used to access content resolver and files dir.
 * @param sourceUri: External image URI returned by the system picker.
 * @return A file URI pointing to the copied image in app-private storage.
 * @throws IOException If the source cannot be opened or the copy fails.
 *
 * ---
 * GenAI usage citation:
 * Image Storage solution to image persistence bug generated with the assistance of ChatGPT.
 */
@Throws(IOException::class)
fun copyImageToAppStorage(
    context: Context,
    sourceUri: Uri
): Uri {
    val imagesDir = File(context.filesDir, "images").apply { mkdirs() }

    val extension = guessImageExtension(context, sourceUri)
    val fileName = "img_${UUID.randomUUID()}.$extension"
    val destinationFile = File(imagesDir, fileName)

    context.contentResolver.openInputStream(sourceUri).use { input ->
        requireNotNull(input) { "Unable to open input stream for URI: $sourceUri" }

        destinationFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    return Uri.fromFile(destinationFile)
}

/**
 * Best-effort file extension resolution for an image URI.
 *
 * @param context: Context used to query the content resolver.
 * @param uri: Source image URI.
 * @return A lowercase extension string without the leading dot.
 */
private fun guessImageExtension(
    context: Context,
    uri: Uri
): String {
    val mimeType = context.contentResolver.getType(uri)

    return when (mimeType) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/gif" -> "gif"
        else -> "jpg"
    }
}