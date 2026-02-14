package com.keepingstock.ui.screens.container

import android.R.attr.onClick
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.uistates.container.ContainerDetailUiState
import com.keepingstock.ui.screens.container.ReadyContent
import com.keepingstock.ui.screens.shared.ErrorContent
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

            is ContainerDetailUiState.Error -> ErrorContent(
                modifier = modifier,
                message = uiState.message
                // TODO: uiState.cause not displayed yet
            )

            is ContainerDetailUiState.Ready -> ReadyContent(
                modifier = modifier,
                uiState = uiState,
                onBack = onBack,
                onEdit = onEdit,
                onMove = onMove,
                onDelete = onDelete
            )
        }
    }
}

@Composable
private fun ReadyContent(
    modifier: Modifier,
    uiState: ContainerDetailUiState.Ready,
    onBack: () -> Unit = {},
    onEdit: (ContainerId) -> Unit = {},
    onMove: (ContainerId) -> Unit = {},
    onDelete: (ContainerId) -> Unit = {}
) {
    val containerId = uiState.containerId

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header card: thumbnail + name/description
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {

        }

        // Metadata card: parent + counts + delete rule
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {

        }

        // Actions card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
    }

    Text("Container Detail Screen (placeholder)")
    Text("containerId = ${uiState.containerId.value}")
    Text("name = ${uiState.containerName}")

    Button(
        onClick = onBack,
        modifier = Modifier.padding(top = 12.dp)
    ) { Text("Back") }

    Button(
        onClick = { onEdit(containerId) },
        modifier = Modifier.padding(top = 12.dp)
    ) { Text("Edit") }

    Button(
        onClick = { onMove(containerId) },
        modifier = Modifier.padding(top = 12.dp)
    ) { Text("Move") }

    Button(
        onClick = { onDelete(containerId) },
        modifier = Modifier.padding(top = 12.dp)
    ) { Text("Delete") }
}