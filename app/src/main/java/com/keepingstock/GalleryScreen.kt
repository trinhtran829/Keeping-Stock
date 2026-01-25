package com.keepingstock

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter

/**
 * GalleryScreen: Displays all photos captured by the app in a grid layout.
 */
@Composable
fun GalleryScreen(navController: NavHostController) {
    val context = LocalContext.current

    // A list to store the URIs of the photos
    val photos = remember { mutableStateListOf<Uri>() }

    // Load photos from the "KeepingStock" folder in MediaStore
    LaunchedEffect(Unit) {
        loadPhotos(context, photos)
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = 32.dp)) {
        // Screen title
        Text(
            text = "Gallery",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.headlineMedium
        )

        // Photo grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(2.dp)
        ) {
            items(photos) { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = null,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .clickable {
                            // Navigate to full-screen photo when clicked
                            val encoded = Uri.encode(uri.toString())
                            navController.navigate("photo/$encoded")
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Back button to return to CameraScreen
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text("Back to Camera")
        }
    }
}

/**
 * Helper function to load photos from the app's folder in MediaStore.
 */
private fun loadPhotos(context: Context, photos: MutableList<Uri>) {
    val projection = arrayOf(MediaStore.Images.Media._ID)
    val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
    } else null
    val selectionArgs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf("%KeepingStock%")
    } else null

    val cursor = context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        "${MediaStore.Images.Media.DATE_ADDED} DESC"
    )

    cursor?.use {
        val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        while (it.moveToNext()) {
            val id = it.getLong(idColumn)
            val contentUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
            photos.add(contentUri)
        }
    }
}
