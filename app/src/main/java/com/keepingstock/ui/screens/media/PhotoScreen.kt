package com.keepingstock.ui.screens.media

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.keepingstock.core.ml.getImageLabels

/**
 * PhotoScreen: Displays a single full-screen photo with ML Kit labels.
 */
@Composable
fun PhotoScreen(
    photoUri: Uri,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var labels by remember { mutableStateOf<List<String>>(emptyList()) }

    // Fetch labels when the photo URI is available
    LaunchedEffect(photoUri) {
        labels = getImageLabels(context, photoUri)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Display the photo
        Image(
            painter = rememberAsyncImagePainter(photoUri),
            contentDescription = "Full photo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // Labels/tags display overlay
        if (labels.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                labels.forEach { label ->
                    Text(
                        text = label,
                        color = Color.White,
                        modifier = Modifier
                            .background(Color.DarkGray, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Back button to return to the previous screen
        Button(
            modifier = Modifier.align(Alignment.BottomCenter).padding(48.dp),
            onClick = onBack
        ) {
            Text("Back")
        }
    }
}