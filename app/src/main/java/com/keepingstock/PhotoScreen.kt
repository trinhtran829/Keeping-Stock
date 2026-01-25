package com.keepingstock

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Display the photo
        Image(
            painter = rememberAsyncImagePainter(uri),
            contentDescription = "Full photo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // Back button to return to the previous screen
        Button(
            modifier = Modifier.align(Alignment.BottomCenter).padding(48.dp),
            onClick = { navController.popBackStack() }
        ) {
            Text("Back")
        }
    }
}
