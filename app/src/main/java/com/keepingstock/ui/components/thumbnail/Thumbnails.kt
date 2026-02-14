package com.keepingstock.ui.components.thumbnail

import com.keepingstock.R
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Thumbnail renderer for a container summary row/tile. If imagePath is null, renders a fallback
 * icon, otherwise, attempts to load the referenced image.
 *
 * Notes:
 * - For best reliability, imagePath should be a URI string (content:// or file://). If we want
 *      to store raw filesystem paths, we should consider converting to a File model instead.
 * - Doesn't request permissions or persist URI grants.
 *
 * :param modifier: Optional modifier applied before internal sizing/shape.
 * :param imagePath: Optional image reference string; expected to parse as a Uri.
 */
@Composable
fun ContainerThumbnail(
    modifier: Modifier = Modifier,
    imagePath: String?
) {
    ThumbnailBase(
        modifier = modifier,
        imagePath = imagePath,
        fallbackIcon = {
            Icon(
                imageVector = Icons.Filled.Inventory,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

/**
 * Thumbnail renderer for an item summary row/tile.
 *
 * Sames notes as container thumbnail
 *
 * :param imagePath: Optional image reference string; expected to parse as a Uri.
 * :param modifier: Optional modifier applied before internal sizing/shape.
 */
@Composable
fun ItemThumbnail(
    modifier: Modifier = Modifier,
    imagePath: String?,
) {
    ThumbnailBase(
        modifier = modifier,
        imagePath = imagePath,
        fallbackIcon = {
            Icon(
                imageVector = Icons.Filled.Grass, // TODO: not the best Icon to use...
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

/**
 * Shared thumbnail implementation used by both container and item thumbnails.
 *
 * Renders a fixed-size rounded rectangle surface (40dp) that either shows a centered fallback
 * icon, or displays a loaded image.
 *
 * TODO: If we decide to store raw file paths instead of Uri strings, we'll need to update
 *
 * :param modifier: Optional modifier applied before internal sizing/shape.
 * :param imagePath: Optional image reference string; expected to parse as a Uri.
 * :param fallbackIcon: Composable shown when imagePath is null/blank.
 */
@Composable
private fun ThumbnailBase(
    modifier: Modifier = Modifier,
    imagePath: String?,
    fallbackIcon: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    Surface (
        modifier = modifier
            .size(40.dp)
            .clip(shape),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        if (imagePath.isNullOrBlank()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                fallbackIcon()
            }
        } else {
            val model: Any = when (imagePath) {
                "demo" -> R.drawable.demo_img_cat
                "demo2" -> R.drawable.demo_img_llama
                else -> Uri.parse(imagePath)
            }

            AsyncImage(
                model = model,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = null,
                error = null
            )
        }
    }
}