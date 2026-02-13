package com.keepingstock.ui.components.thumbnail

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun ContainerThumbnail(
    modifier: Modifier = Modifier,
    imagePath: String?
) {

}

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
            val model: Any = Uri.parse(imagePath)

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