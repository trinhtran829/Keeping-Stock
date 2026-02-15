package com.keepingstock.ui.screens.item

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.UiState
import com.keepingstock.ui.components.screen.ErrorContent
import com.keepingstock.ui.components.screen.ItemSummaryRow
import com.keepingstock.ui.components.screen.LoadingContent
import com.keepingstock.viewmodel.item.ItemBrowserUiData

@Composable
fun ItemBrowserScreen(
    modifier: Modifier = Modifier,
    uiState: UiState<ItemBrowserUiData>,
    onOpenItem: (itemId: ItemId) -> Unit = {},
    onAddItem: () -> Unit = {}
) {
    when (uiState) {
        is UiState.Loading -> LoadingContent(modifier)

        is UiState.Error -> ErrorContent(
            modifier = modifier,
            message = uiState.message
        )

        is UiState.Success ->
            Text("Item Browser Screen (${uiState.data.items.size} items)")
    }
}

/**
 * Ready-state UI for the Item Browser. Does the heavy-lifting
 *
 * - If item list is empty, shows the empty-state.
 * - Otherwise, renders items in a single scrolling list.
 *
 * TODO(FUTURE): Add a grid/tile layout option. Keep row composables reusable by both layouts.
 *
 * :param modifier: Optional modifier for the screen container.
 * :param items: List of items in this container.
 * :param onOpenItem: User intent to open an item detail view.
 * :param onAddItem: User intent to add an item under containerId.
 */
@Composable
private fun ReadyContent(
    modifier: Modifier,
    items: List<Item>,
    onOpenItem: (itemId: ItemId) -> Unit = {},
    onAddItem: () -> Unit = {}
) {
    Column() {
        // Content header; mainly counts and info button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val counts = "${items.size} items"
            Text(
                text = counts,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider()

        // Empty state; not it's own state variant
        if (items.isEmpty()) {
            EmptyState(
                modifier = Modifier.fillMaxSize(),
                onAddItem = onAddItem
            )
            return
        }

        // Populated state; scrolling list with individual sections for subcontainers/items
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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

            // breathing room above bottom bar
            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

/**
 * Empty-state UI shown when there are no items.
 *
 * :param modifier Modifier applied to the full-size empty-state container.
 * :param onAddContainer Invoked when user chooses to add a container.
 * :param onAddItem Invoked when user chooses to add an item.
 */
@Composable
private fun EmptyState(
    modifier: Modifier,
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
                text = "Add an item to get started.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(16.dp))

            OutlinedButton(onClick = onAddItem) { Text("Add item") }
        }
    }
}