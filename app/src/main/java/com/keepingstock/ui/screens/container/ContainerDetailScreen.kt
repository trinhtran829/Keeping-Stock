package com.keepingstock.ui.screens.container

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ContainerDetailScreen(
    containerId: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text("Container Detail Screen (placeholder)")
        Text("containerId = $containerId")

        Button(
            onClick = onBack,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Back")
        }
    }
}