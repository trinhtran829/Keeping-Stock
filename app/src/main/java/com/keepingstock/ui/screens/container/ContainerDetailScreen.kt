package com.keepingstock.ui.screens.container

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.keepingstock.R
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.uistates.container.ContainerDetailUiState
import com.keepingstock.platform.services.MlKitQrService
import com.keepingstock.ui.components.screen.DetailRow
import com.keepingstock.ui.components.screen.ErrorContent
import com.keepingstock.ui.components.screen.LoadingContent

/**
 * Details screen for a container. Render based on ContainerBrowserUiState.
 */
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
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ContainerDetailHeaderCard(uiState)
        }

        item {
            ContainerQrCard(uiState.containerId)
        }

        item {
            ContainerDetailMetadataCard(uiState)
        }

        item {
            ContainerDetailActionsCard(
                containerId = uiState.container.id,
                canDelete = uiState.canDelete,
                onBack = onBack,
                onEdit = onEdit,
                onMove = onMove,
                onDelete = onDelete
            )
        }
    }
}

@Composable
private fun ContainerQrCard(containerId: ContainerId) {
    val context = LocalContext.current
    val qrService = remember { MlKitQrService(context) }
    val qrBitmap = remember(containerId) { qrService.generateQrBitmap(containerId) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCode, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Container QR Code", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(16.dp))

            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "QR Code for container ${containerId.value}",
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.White)
                        .padding(8.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Scan this to quickly open this container",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text("Failed to generate QR Code")
            }
        }
    }
}

@Composable
private fun ContainerDetailHeaderCard(
    uiState: ContainerDetailUiState.Ready
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Inventory2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = uiState.container.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Container",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val imageUri = uiState.container.imageUri
            if (!imageUri.isNullOrBlank()) {
                val model: Any = when (imageUri) {
                    "demo" -> R.drawable.demo_img_cat
                    "demo2" -> R.drawable.demo_img_llama
                    else -> imageUri.toUri()
                }

                HorizontalDivider()

                AsyncImage(
                    model = model,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            val description = uiState.container.description?.trim().orEmpty()
            if (description.isNotBlank()) {
                HorizontalDivider()
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun ContainerDetailMetadataCard(
    uiState: ContainerDetailUiState.Ready
) {
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
                value = uiState.container.id.value.toString()
            )

            DetailRow(
                label = "Parent",
                value = uiState.parentContainerName ?: "Root"
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
}

@Composable
private fun ContainerDetailActionsCard(
    containerId: ContainerId,
    canDelete: Boolean,
    onBack: () -> Unit = {},
    onEdit: (ContainerId) -> Unit = {},
    onMove: (ContainerId) -> Unit = {},
    onDelete: (ContainerId) -> Unit = {}
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onEdit(containerId) },
                    modifier = Modifier.weight(1f)
                ) { Text("Edit") }

                OutlinedButton(
                    onClick = { onMove(containerId) },
                    modifier = Modifier.weight(1f)
                ) { Text("Move") }
            }

            Button(
                onClick = { onDelete(containerId) },
                enabled = canDelete,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Delete") }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Back") }
        }
    }
}
