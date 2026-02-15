package com.keepingstock.ui.screens.item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
    onAddItem: () -> Unit
) {

}

@Preview(showSystemUi = false, showBackground = true)
@Composable
private fun ItemBrowserScreenPreview() {
    // Preview without ViewModel
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Item Browser Screen (placeholder)")

        Button(
            onClick = {},
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Open Example Item 01")
        }

        Button(
            onClick = {},
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Go to Container Browser (last open)")
        }
    }
}