package com.keepingstock.ui.screens.container

import android.R.attr.onClick
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.uistates.container.ContainerDetailUiState
import com.keepingstock.ui.components.thumbnail.ContainerThumbnail
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
        // TODO: Copied from ContainerBrowserScreen; needs updating
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ContainerThumbnail(imagePath = uiState.imageUri)

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = uiState.containerName,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1
                    )

                    uiState.description
                        ?.takeIf { it.isNotBlank() }
                        ?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 3
                            )
                        }
                }
            }
        }

        // Metadata card: parent + counts + delete rule
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailRow(
                    label = "Container ID",
                    value = containerId.value.toString()
                )

                DetailRow(
                    label = "Parent",
                    value = uiState.parentContainerId?.value?.toString() ?: "Root"
                )

                HorizontalDivider()

                DetailRow(
                    label = "Subcontainers",
                    value = uiState.subcontainerCount.toString()
                )

                DetailRow(
                    label = "Items",
                    value = uiState.itemCount.toString()
                )

                if (!uiState.canDelete) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = uiState.deleteBlockedReason ?: "This container cannot be deleted.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Actions card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {

        }
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

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}