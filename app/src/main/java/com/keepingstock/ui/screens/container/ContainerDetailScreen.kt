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
import com.keepingstock.ui.components.screen.DetailRow
import com.keepingstock.ui.components.screen.ErrorContent
import com.keepingstock.ui.components.screen.LoadingContent

/**
 * Details screen for a container. Render based on ContainerBrowserUiState.
 *
 * The image render section supports demo drawable resources for previews/testing. Use "demo" as
 * the imageUri
 *
 * :param modifier: Modifier applied to the screen.
 * :param uiState: Current UI state for the container details.
 * :param onBack: User intent to navigate back.
 * :param onEdit: User intent to edit this container.
 * :param onMove: User intent to move/re-parent this container.
 * :param onDelete: User intent to delete this container (if allowed).
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

/**
 * Ready-state UI for container detail
 *
 * Uses a LazyColumn to ensure content is scrollable if needed.
 *
 * Layout (separate cards):
 * - header (type + name + image + full description)
 * - metadata (details)
 * - actions (edit/move/delete/back)
 *
 * :param modifier: Modifier applied to the scroll container.
 * :param uiState: Ready state containing all container details required for display.
 * :param onBack: User intent to navigate back.
 * :param onEdit: User intent to edit this container.
 * :param onMove: User intent to move/re-parent this container.
 * :param onDelete: User intent to delete this container.
 */
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
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ContainerDetailHeaderCard(uiState)
        }

        item {
            ContainerDetailMetadataCard(uiState)
        }

        item {
            ContainerDetailActionsCard(
                containerId = uiState.containerId,
                canDelete = uiState.canDelete,
                onBack = onBack,
                onEdit = onEdit,
                onMove = onMove,
                onDelete = onDelete
            )
        }
    }
}

/**
 * Header card for container details.
 *
 * Layout:
 * - Top row: container type icon + container name + small type label
 * - Image: shown only when imageUri is present
 * - Description
 *
 * Special-case imageUri values "demo"/"demo2/demox" to load demo images. This is used
 * for preview/demo builds where URIs or file paths may not resolve.
 *
 * TODO: Remove "demo"/"demo2" special-casing once a proper image pipeline exists
 *  (or add debug-only flag).
 *
 * :param uiState: Ready state used as the source of truth for display.
 */
@Composable
private fun ContainerDetailHeaderCard(
    uiState: ContainerDetailUiState.Ready
) {
    // Header card: thumbnail + name/description
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

            // Container Image (only when present)
            // Hero Image
            val imageUri = uiState.container.imageUri
            if (!imageUri.isNullOrBlank()) {
                val model: Any = when (imageUri) {
                    "demo" -> R.drawable.demo_img_cat
                    "demo2" -> R.drawable.demo_img_llama
                    else -> Uri.parse(imageUri)
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

/**
 * Metadata card for container details.
 *
 * Displays:
 * - container id
 * - parent container name (or "Root")
 * - counts for subcontainers and items
 * - deletion restriction message when canDelete is false
 *
 * :param uiState: Ready state containing metadata required for display.
 */
@Composable
private fun ContainerDetailMetadataCard(
    uiState: ContainerDetailUiState.Ready
) {
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
                value = uiState.containerId.value.toString()
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

/**
 * Actions card for container details.
 *
 * Provides the primary actions for the container:
 * - Edit
 * - Move
 * - Delete (disabled when canDelete is false)
 * - Back
 *
 * TODO: Consider moving some actions to top bar?
 *
 * :param containerId: Target container for all actions.
 * :param canDelete: Whether Delete should be enabled.
 * :param onBack: User intent to navigate back.
 * :param onEdit: User intent to edit this container.
 * :param onMove: User intent to move/re-parent this container.
 * :param onDelete: User intent to delete this container.
 */
@Composable
private fun ContainerDetailActionsCard(
    containerId: ContainerId,
    canDelete: Boolean,
    onBack: () -> Unit = {},
    onEdit: (ContainerId) -> Unit = {},
    onMove: (ContainerId) -> Unit = {},
    onDelete: (ContainerId) -> Unit = {}
) {
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
                enabled = canDelete,
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