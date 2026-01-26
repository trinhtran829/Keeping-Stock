package com.keepingstock.ui.media

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter

/**
 * PhotoScreen: Displays a single full-screen photo.
 */
@Composable
fun PhotoScreen(uriString: String, navController: NavHostController) {
    val uri = Uri.parse(Uri.decode(uriString)) // Decode the URI passed from navigation

    Box(modifier = Modifier.Companion.fillMaxSize().background(Color.Companion.Black)) {
        // Display the photo
        Image(
            painter = rememberAsyncImagePainter(uri),
            contentDescription = "Full photo",
            modifier = Modifier.Companion.fillMaxSize(),
            contentScale = ContentScale.Companion.Fit
        )

        // Back button to return to the previous screen
        Button(
            modifier = Modifier.Companion.align(Alignment.Companion.BottomCenter).padding(48.dp),
            onClick = { navController.popBackStack() }
        ) {
            Text("Back")
        }
    }
}