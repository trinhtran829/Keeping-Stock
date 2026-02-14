package com.keepingstock.ui.screens.container

import android.net.Uri
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
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.keepingstock.R
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.uistates.container.ContainerDetailUiState
import com.keepingstock.ui.screens.shared.DetailRow
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
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Type icon + container name
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
                            text = uiState.containerName,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Container",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Container Image (only when present)
                // Hero Image
                if (!uiState.imageUri.isNullOrBlank()) {
                    val model: Any = when (uiState.imageUri) {
                        "demo" -> R.drawable.demo_img_cat
                        "demo2" -> R.drawable.demo_img_llama
                        else -> Uri.parse(uiState.imageUri)
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

                // Full description
                val description = uiState.description?.trim().orEmpty()
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
            /*
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
             */
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

        // Actions card
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
                    enabled = uiState.canDelete,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Delete") }

                // TODO: Use top-bar back navigation instead?
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Back") }
            }
        }
    }
}