package com.keepingstock.ui.screens.container

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.uistates.container.ContainerBrowserUiState
import com.keepingstock.data.entities.ItemStatus
import com.keepingstock.ui.components.thumbnail.ContainerThumbnail
import com.keepingstock.ui.components.thumbnail.ItemThumbnail

/**
 * Screen for browsing container contents. Render based on ContainerBrowserUiState.
 *
 * TODO: Add addContainer and addItem buttons
 * TODO: Search/Filter/Sort feature
 *
 * :param modifier: Optional modifier for the top-level screen container.
 * :param uiState: Current state of loading, error, or container contents.
 * :param onOpenSubcontainer: User intent to open a subcontainer.
 * :param onOpenItem: User intent to open an item detail screen.
 * :param onOpenContainerInfo: User intent to open the current container's info/detail screen.
 * :param onAddContainer: User intent to create a container under the current container.
 * :param onAddItem: User intent to create an item under the current container.
 */
@Composable
fun ContainerBrowserScreen(
    modifier: Modifier = Modifier,
    uiState: ContainerBrowserUiState,
    onOpenSubcontainer: (containerId: ContainerId) -> Unit = {},
    onOpenItem: (itemId: ItemId) -> Unit = {},
    onOpenContainerInfo: (containerId: ContainerId) -> Unit = {},
    onAddContainer: (parentContainerId: ContainerId?) -> Unit = {},
    onAddItem: (containerId: ContainerId?) -> Unit = {}
) {
    when (uiState) {
        ContainerBrowserUiState.Loading -> LoadingContent(modifier)

        is ContainerBrowserUiState.Error -> ErrorContent(
            modifier = modifier,
            message = uiState.message
            // TODO: uiState.cause not displayed yet
        )

        is ContainerBrowserUiState.Ready -> ReadyContent(
            modifier = modifier,
            containerId = uiState.containerId,
            containerName = uiState.containerName,
            subcontainers = uiState.subcontainers,
            items = uiState.items,
            onOpenSubcontainer = onOpenSubcontainer,
            onOpenItem = onOpenItem,
            onOpenContainerInfo = onOpenContainerInfo,
            onAddContainer = onAddContainer,
            onAddItem = onAddItem
        )
    }
}

/**
 * LoadingState UI for the Container Browser. Just uses a basic CircularProgressIndicator
 *
 * :param modifier: Optional modifier applied to the full-screen container.
 */
@Composable
private fun LoadingContent(modifier: Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/**
 * ErrorState UI for the Container Browser. Currently just displays an error message.
 *
 * TODO: Decide whether the UiState's throwable cause should be:
 *  - logged only (ViewModel/repository responsibility), or
 *  - shown in UI for debugging, or
 *  - Other?
 *
 * :param modifier: Optional modifier applied to the full-screen container.
 * :param message: Error message shown to user.
 * :param cause: Optional Throwable (not currently displayed).
 */
@Composable
private fun ErrorContent(
    modifier: Modifier,
    message: String,
    cause: Throwable? = null
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}

/**
 * Ready-state UI for the Container Browser. Does the heavy-lifting
 *
 * - Shows a small header with counts and an optional "Info" action.
 * - If both lists are empty, shows the empty-state call-to-action.
 * - Otherwise, renders subcontainers and items in a single scrolling list.
 * - Container name shows in the global top bar right now, so owned by destination.
 *
 * TODO(FUTURE): Add a grid/tile layout option. Keep row composables reusable by both layouts.
 *
 * :param modifier: Optional modifier for the screen container.
 * :param containerId: The currently displayed container (null = root).
 * :param containerName: The current container display name (currently not used/used by topbar in
 *                 destination).
 * :param subcontainers: List of subcontainers.
 * :param items: List of items in this container.
 * :param onOpenSubcontainer: User intent to open a subcontainer.
 * :param onOpenItem: User intent to open an item detail view.
 * :param onOpenContainerInfo: User intent to open container info/detail for containerId.
 * :param onAddContainer: User intent to add a subcontainer under containerId.
 * :param onAddItem: User intent to add an item under containerId.
 */
@Composable
private fun ReadyContent(
    modifier: Modifier,
    containerId: ContainerId?,
    containerName: String,
    subcontainers: List<Container>,
    items: List<Item>,
    onOpenSubcontainer: (containerId: ContainerId) -> Unit = {},
    onOpenItem: (itemId: ItemId) -> Unit = {},
    onOpenContainerInfo: (containerId: ContainerId) -> Unit = {},
    onAddContainer: (parentContainerId: ContainerId?) -> Unit = {},
    onAddItem: (containerId: ContainerId?) -> Unit = {}
) {
    Column() {
        // Content header; mainly counts and info button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val counts = "${subcontainers.size} containers • ${items.size} items"
            Text(
                text = counts,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            if (containerId != null) {
                TextButton(onClick = { onOpenContainerInfo(containerId) }) {
                    Text("Info") // TODO: info Icon?
                }
            }
        }

        HorizontalDivider()

        // Empty state; not it's own state variant
        if (subcontainers.isEmpty() && items.isEmpty()) {
            EmptyState(
                modifier = Modifier.fillMaxSize(),
                onAddContainer = { onAddContainer(containerId) },
                onAddItem = { onAddItem(containerId) }
            )
            return
        }

        // Populated state; scrolling list with individual sections for subcontainers/items
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (subcontainers.isNotEmpty()) {
                item {
                    Text("Containers", style = MaterialTheme.typography.titleMedium)
                }
                items(subcontainers, key = { it.id.value }) { c ->
                    ContainerSummaryRow(
                        modifier = Modifier,
                        container = c,
                        onClick = { onOpenSubcontainer(c.id) }
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            if (items.isNotEmpty()) {
                item {
                    Text("Items", style = MaterialTheme.typography.titleMedium)
                }
                items(items, key = { it.id.value }) { i ->
                    ItemSummaryRow(
                        modifier = Modifier,
                        item = i,
                        onClick = { onOpenItem(i.id) }
                    )
                }
            }

            // breathing room above bottom bar
            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

/**
 * Empty-state UI shown when a container has no subcontainers and no items.
 *
 * :param modifier Modifier applied to the full-size empty-state container.
 * :param onAddContainer Invoked when user chooses to add a container.
 * :param onAddItem Invoked when user chooses to add an item.
 */
@Composable
private fun EmptyState(
    modifier: Modifier,
    onAddContainer: () -> Unit,
    onAddItem: () -> Unit
) {
    Box(
        modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Nothing here yet",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Add a container or item to get started.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onAddContainer) { Text("Add container") }

                OutlinedButton(onClick = onAddItem) { Text("Add item") }
            }
        }
    }
}

/**
 * Row UI for a subcontainer entry in the Container Browser list. Uses a thumbnail (image when
 * available, icon fallback otherwise) and basic text fields.
 *
 * TODO: thumbnail needs testing.
 *
 * TODO(FUTURE): Consider adding a overflow menu for actions like rename, move, delete
 *
 * :param modifier: Modifier applied to the card container.
 * :param container: The container to display.
 * :param onClick: Invoked when user selects this container.
 */
@Composable
private fun ContainerSummaryRow(
    modifier: Modifier,
    container: Container,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ContainerThumbnail handles image vs fallback icon
            ContainerThumbnail(imagePath = container.imageUri)

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = container.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                container.description?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Row UI for an item entry in the Container Browser list.
 *
 * Displays a thumbnail, name, and a subtitle built from item status and description.
 * If tags exist, they're supposed to be displayed in a hashtag-like format. Not tested.
 *
 * :param modifier: Modifier applied to the card container.
 * :param item: The item to display.
 * :param onClick: Called when user selects this item.
 */
@Composable
private fun ItemSummaryRow(
    modifier: Modifier,
    item: Item,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ItemThumbnail handles image vs fallback icon internally.
            ItemThumbnail(imagePath = item.imagePath)

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val subtitle = buildString {
                    append(item.status.name)
                    if (!item.description.isNullOrBlank()) {
                        if (isNotEmpty()) append(" • ")
                        append(item.description)
                    }
                }

                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (item.tags.isNotEmpty()) {
                    Text(
                        text = item.tags.joinToString(prefix = "#", separator = " #"),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Preview of ContainerSummaryRow using sample container data.
 */
@Preview(showBackground = true)
@Composable
private fun Preview_ContainerSummaryRow() {
    ContainerSummaryRow(
        modifier = Modifier,
        onClick = {},
        container = Container(
            id = ContainerId(1L),
            name = "Garage",
            description = "Garage Description goes here"
        )
    )
}

/**
 * Preview of ItemSummaryRow using sample item data.
 */
@Preview(showBackground = true)
@Composable
private fun Preview_ItemSummaryRow() {
    ItemSummaryRow(
        modifier = Modifier,
        onClick = {},
        item = Item(
            id = ItemId(100L),
            name = "Impact Driver",
            description = "DeWalt Brand 18V brushless",
            imagePath = null,
            status = ItemStatus.STORED,
            containerId = ContainerId(1L)
        )
    )
}