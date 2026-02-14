package com.keepingstock.ui.screens.container

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keepingstock.core.contracts.ContainerId

@Composable
fun ContainerDetailScreen(
    containerId: ContainerId,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onEdit: (containerId: ContainerId) -> Unit = {}
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
        Button(
            onClick = { onEdit(containerId) }, modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Edit")
        }
    }
}