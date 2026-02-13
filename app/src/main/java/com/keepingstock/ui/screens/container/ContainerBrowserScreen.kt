package com.keepingstock.ui.screens.container

import android.R.attr.top
import android.widget.Space
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

/**
 *
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
    /*
    // TODO: OLD PLACEHOLDER UI - DELETE AFTER REFACTOR OF SCREEN
    Column(modifier = modifier.padding(16.dp)) {
        Text("Container Browser Screen (placeholder)")
        Text("containerId = ${containerId ?: "ROOT"}")

        Button(
            onClick = { onOpenSubcontainer("02") },
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Open example subcontainer 02")
        }

        Button(
            onClick = { onOpenItem("01") },
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Open example item 01")
        }

        Button(
            onClick = { if (containerId != null) onOpenContainerInfo(containerId) },
            enabled = containerId != null,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Open container info")
        }

        Button(
            onClick = { onAddContainer(containerId) },
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text(
                if (containerId == null) {
                    "Add container"
                } else {
                    "Add subcontainer"
                }
            )
        }

        Button(
            onClick = { onAddItem(containerId) },
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text(
                if (containerId == null) {
                    "Add item (choose container later)"
                } else {
                    "Add item to this container"
                }
            )
        }

        Button(
            onClick = onGoToItemBrowser,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Go to Item Browser")
        }

        Button(
            onClick = onScanQr,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Scan QR")
        }

    }
    */
}

/**
 *
 */
@Composable
private fun LoadingContent(modifier: Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/**
 * TODO: Consult with Rich on his intended purpose of cause Throwable
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
 *
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
    // TODO: Refactor for grid-view option
    Column() {
        // Content header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

        }

        HorizontalDivider()

        // Empty state
        if (subcontainers.isEmpty() && items.isEmpty()) {
            EmptyState(
                modifier = Modifier.fillMaxSize(),
                onAddContainer = { onAddContainer(containerId) },
                onAddItem = { onAddItem(containerId) }
            )
        }

        // Populated state
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (subcontainers.isNotEmpty()) {
                item {
                    Text("Containers", style = MaterialTheme.typography.titleMedium)
                }
                items(subcontainers, key = { it.id }) { c ->

                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            if (items.isNotEmpty()) {
                item {
                    Text("Items", style = MaterialTheme.typography.titleMedium)
                }
                items(items, key = { it.id }) { i ->

                }
            }

            // breathing room above bottom bar
            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

/**
 *
 */
@Composable
private fun EmptyState(
    modifier: Modifier,
    onAddContainer: () -> Unit,
    onAddItem: () -> Unit
) {

}

/**
 *
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
            // TODO: Current placeholder for thumbnail/icon
            Box(Modifier.size(40.dp))

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
 *
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
            // TODO: Current placeholder for thumbnail/icon
            Box(Modifier.size(40.dp))

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