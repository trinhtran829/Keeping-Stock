package com.keepingstock.ui.screens.item

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
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
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.uistates.item.ItemDetailUiState
import com.keepingstock.data.entities.ItemStatus
import com.keepingstock.ui.components.screen.DetailRow
import com.keepingstock.ui.components.screen.ErrorContent
import com.keepingstock.ui.components.screen.LoadingContent
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ItemDetailsScreen(
    modifier: Modifier = Modifier,
    uiState: ItemDetailUiState,
    onBack: () -> Unit,
    onEdit: (ItemId) -> Unit = {},
    onMove: (ItemId) -> Unit = {},
    onDelete: (ItemId) -> Unit = {}
) {
    Column(modifier = modifier.padding(16.dp)) {
        when (uiState) {
            ItemDetailUiState.Loading -> LoadingContent(modifier)

            is ItemDetailUiState.Error -> ErrorContent(
                modifier = modifier,
                message = uiState.message
                // TODO: uiState.cause not displayed yet
            )

            is ItemDetailUiState.Ready -> ReadyContent(
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
 * Ready-state UI for item detail
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
    uiState: ItemDetailUiState.Ready,
    onBack: () -> Unit = {},
    onEdit: (ItemId) -> Unit = {},
    onMove: (ItemId) -> Unit = {},
    onDelete: (ItemId) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ItemDetailHeaderCard(uiState)
        }

        item {
            ItemDetailMetadataCard(uiState)
        }

        item {
            ItemDetailActionsCard(
                itemId = uiState.itemId,
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
private fun ItemDetailHeaderCard(
    uiState: ItemDetailUiState.Ready
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
                    imageVector = Icons.Filled.Category,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = uiState.item.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Item",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Item Image (only when present)
            // Hero Image
            val imageUri = uiState.item.imageUri
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
            val description = uiState.item.description?.trim().orEmpty()
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
 * Metadata card for item details.
 *
 * Displays:
 * - item id
 * - parent container name (or "Root")
 * - creation date
 * - checkout status
 * - checkout date
 *
 * :param uiState: Ready state containing metadata required for display.
 */
@Composable
private fun ItemDetailMetadataCard(
    uiState: ItemDetailUiState.Ready
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
                label = "Item ID",
                value = uiState.itemId.value.toString()
            )

            DetailRow(
                label = "Parent Container",
                value = uiState.parentContainerName ?: "Root"
            )

            HorizontalDivider()

            val createdDate = uiState.item.createdDate
            val dateFormatter =
                DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm")
            val formattedCreatedDate =
                createdDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .format(dateFormatter)
            DetailRow(
                label = "Created On",
                value = formattedCreatedDate
            )

            DetailRow(
                label = "Checked Out Status",
                value = when (uiState.item.status) {
                    ItemStatus.STORED -> "Stored"
                    ItemStatus.TAKEN_OUT -> "Checked Out"
                }
            )

            if (uiState.item.status == ItemStatus.TAKEN_OUT) {
                uiState.item.checkoutDate?.let { checkedOutDate ->
                    val formattedCheckedOutDate =
                        checkedOutDate.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
                            .format(dateFormatter)

                    DetailRow(
                        label = "Checked Out On",
                        value = formattedCheckedOutDate
                    )
                }
            }
        }
    }
}

/**
 * Actions card for item details.
 *
 * Provides the primary actions for the item:
 * - Edit
 * - Move
 * - Delete (disabled when canDelete is false)
 * - Back
 *
 * TODO: Consider moving some actions to top bar?
 *
 * :param itemId: Target item for all actions.
 * :param onBack: User intent to navigate back.
 * :param onEdit: User intent to edit this item.
 * :param onMove: User intent to move/re-parent this item.
 * :param onDelete: User intent to delete this item.
 */
@Composable
private fun ItemDetailActionsCard(
    itemId: ItemId,
    onBack: () -> Unit = {},
    onEdit: (ItemId) -> Unit = {},
    onMove: (ItemId) -> Unit = {},
    onDelete: (ItemId) -> Unit = {}
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
                    onClick = { onEdit(itemId) },
                    modifier = Modifier.weight(1f)
                ) { Text("Edit") }

                OutlinedButton(
                    onClick = { onMove(itemId) },
                    modifier = Modifier.weight(1f)
                ) { Text("Move") }
            }

            Button(
                onClick = { onDelete(itemId) },
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