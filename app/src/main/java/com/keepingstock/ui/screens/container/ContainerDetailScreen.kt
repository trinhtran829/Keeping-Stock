package com.keepingstock.ui.screens.container

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.uistates.container.ContainerDetailUiState
import com.keepingstock.ui.screens.shared.LoadingContent

@Composable
fun ContainerDetailScreen(
    modifier: Modifier = Modifier,
    uiState: ContainerDetailUiState,
    onBack: () -> Unit = {},
    onEdit: (ContainerId) -> Unit = {},
    onMove: (ContainerId) -> Unit = {},
    onDelete: (ContainerId) -> Unit = {}
) {
    Column(modifier = modifier.padding(16.dp)) {
        when (uiState) {
            ContainerDetailUiState.Loading -> LoadingContent(modifier)

            is ContainerDetailUiState.Error -> {
                Text("Error: ${uiState.message}")
            }

            is ContainerDetailUiState.Ready -> {

            }
        }
    }

    /*
    // PLACEHOLDER SCREEN
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
     */
}