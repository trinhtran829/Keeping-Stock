package com.keepingstock.ui.screens.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.keepingstock.ui.theme.KeepingStockTheme

// Add callback functions for custom screens here
@Composable
fun DebugGalleryScreen(
    onOpenContainerBrowser: () -> Unit,
    onOpenItemBrowser: () -> Unit,
    onOpenQrScan: () -> Unit,
    onOpenCamera: () -> Unit,
    onOpenGallery: () -> Unit,
    onOpenPhotoDemo: () -> Unit,
    onShowSnackbarDemo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "DEBUG BUILD",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = "Switch Build Variant to RELEASE to run the normal app flow.",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "Debug Gallery",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Use these shortcuts to test screens in the emulator. " +
                    "Do not add navigation graphs or change MainActivity.",
            style = MaterialTheme.typography.bodyMedium
        )

        DebugButton("Container Browser", onOpenContainerBrowser)
        DebugButton("Item Browser", onOpenItemBrowser)
        DebugButton("QR Scan", onOpenQrScan)

        DebugButton("Camera", onOpenCamera)
        DebugButton("Gallery", onOpenGallery)
        DebugButton("Photo (demo)", onOpenPhotoDemo)

        // Add debug buttons to custom screens here
        DebugButton("Snackbar Message Demo", onShowSnackbarDemo)
    }
}

@Composable
private fun DebugButton(
    label: String,
    onClick: () -> Unit
) {
    Button (
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 16.dp)
    ) {
        Text(label)
    }
}

@Preview(showBackground = true)
@Composable
private fun DebugGalleryScreenPreview() {
    KeepingStockTheme {
        DebugGalleryScreen(
            onOpenContainerBrowser = {},
            onOpenItemBrowser = {},
            onOpenQrScan = {},
            onOpenCamera = {},
            onOpenGallery = {},
            onOpenPhotoDemo = {},
            onShowSnackbarDemo = {}
        )
    }
}