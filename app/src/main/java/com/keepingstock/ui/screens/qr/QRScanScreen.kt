package com.keepingstock.ui.screens.qr

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QRScanScreen(
    modifier: Modifier = Modifier,
    onScannedContainer: (containerId: String) -> Unit = {},
    onCancel: () -> Unit = {}
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text("QR Scan Screen (placeholder)")

        Button(
            onClick = { onScannedContainer("02") },
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Simulate scan: container 02")
        }

        Button(
            onClick = onCancel,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Cancel")
        }
    }
}
