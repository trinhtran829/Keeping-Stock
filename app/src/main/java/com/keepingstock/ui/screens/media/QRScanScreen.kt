package com.keepingstock.ui.screens.media

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.UiState
import com.keepingstock.ui.viewmodel.media.QrScanUiData

@Composable
fun QRScanScreen(
    uiState: UiState<QrScanUiData>,
    modifier: Modifier = Modifier,
    onScan: () -> Unit = {},
    onOpenScannedContainer: (containerId: ContainerId) -> Unit = {},
    onCancel: () -> Unit = {}
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text("QR Scan")

        when (val state = uiState) {
            UiState.Loading -> {
                Row(modifier = Modifier.padding(top = 12.dp)) {
                    CircularProgressIndicator()
                    Text(
                        text = "Scanning...",
                        modifier = Modifier.padding(start = 12.dp, top = 8.dp)
                    )
                }
            }

            is UiState.Success -> {
                val response = state.data.response
                if (response == null) {
                    Text("Tap scan to detect a container QR code.")
                } else {
                    Text(
                        text = "Request: ${response.requestId}",
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Text(
                        text = "Detected: ${response.containerName} " +
                                "(id=${response.containerId})",
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Button(
                        onClick = { onOpenScannedContainer(ContainerId(response.containerId)) },
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text("Open Container")
                    }
                }
            }

            is UiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }

        Button(
            onClick = onScan,
            enabled = uiState !is UiState.Loading,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Scan QR")
        }

        Button(
            onClick = onCancel,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Cancel")
        }
    }
}
