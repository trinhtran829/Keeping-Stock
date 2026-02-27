package com.keepingstock.ui.screens.item

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keepingstock.core.contracts.BrowserEmptyState
import com.keepingstock.core.contracts.BrowserLayout
import com.keepingstock.core.contracts.BrowserSort
import com.keepingstock.core.contracts.ContainerBrowserFilter
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemBrowserFilter
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.intents.item.ItemBrowserIntent
import com.keepingstock.core.contracts.uistates.item.ItemBrowserUiState
import com.keepingstock.ui.components.screen.EmptyState
import com.keepingstock.ui.components.screen.ErrorContent
import com.keepingstock.ui.components.screen.ItemStatusPickerChip
import com.keepingstock.ui.components.screen.ItemSummaryRow
import com.keepingstock.ui.components.screen.LoadingContent
import com.keepingstock.ui.components.screen.NoResultsState
import com.keepingstock.ui.components.screen.SearchField
import com.keepingstock.ui.components.screen.SortAndLayoutRow
import com.keepingstock.ui.screens.item.ReadyContent
import com.keepingstock.viewmodel.item.ItemBrowserUiData

/**
 * Screen for browsing all items.
 *
 * @param modifier: Optional modifier applied to the top-level container.
 * @param uiState: Current UI state for loading, error, or ready content.
 * @param onIntent: Callback for user intents (query, filter, sort, layout).
 * @param onOpenItem: Invoked when the user selects an item.
 * @param onAddItem: Invoked when the user chooses to add a new item.
 */
@Composable
fun ItemBrowserScreen(
    modifier: Modifier = Modifier,
    uiState: ItemBrowserUiState, // UiState<ItemBrowserUiData>,
    onIntent: (ItemBrowserIntent) -> Unit,
    onOpenItem: (itemId: ItemId) -> Unit = {},
    onAddItem: () -> Unit = {}
) {

    when (uiState) {
        is ItemBrowserUiState.Loading -> LoadingContent(modifier)

        is ItemBrowserUiState.Error -> ErrorContent(
            modifier = modifier.fillMaxSize(),
            message = uiState.message
        )

        is ItemBrowserUiState.Success -> ReadyContent(
            modifier = modifier.fillMaxSize(),
            uiData = uiState,
            onIntent = onIntent,
            onOpenItem = onOpenItem,
            onAddItem = onAddItem
        )
    }
}

/**
 * Ready-state UI for the Item Browser.
 *
 * - Shows a header row with item count and an Add action.
 * - Shows shared browser controls (search + status filter + sort + layout).
 * - Shows empty states based on [BrowserEmptyState].
 * - Renders results using the selected [BrowserLayout].
 *
 * TODO(FUTURE): Add a grid/tile layout option. Keep row composables reusable by both layouts.
 *
 * @param modifier: Optional modifier for the screen container.
 * @param items: List of items in this container.
 * @param onOpenItem: User intent to open an item detail view.
 * @param onAddItem: User intent to add an item under containerId.
 */
@Composable
private fun ReadyContent(
    modifier: Modifier,
    uiData: ItemBrowserUiState.Success,
    onIntent: (ItemBrowserIntent) -> Unit,
    onOpenItem: (itemId: ItemId) -> Unit = {},
    onAddItem: () -> Unit = {}
) {
    Column() {
        // Content header; mainly counts
        ItemBrowserHeader(
            itemCount = uiData.items.size,
            onAddItem = onAddItem
        )

        ControlsBar(
            query = uiData.query,
            filter = uiData.filter,
            sort = uiData.sort,
            layout = uiData.layout,
            onIntent = onIntent
        )

        HorizontalDivider()

        // Empty state; not it's own state variant
        when (uiData.emptyState) {
            BrowserEmptyState.EMPTY -> {
                EmptyState(
                    modifier = Modifier.fillMaxSize(),
                    onAddContainer = { },
                    onAddItem = onAddItem,
                    isItemBrowser = true
                )
                return
            }

            BrowserEmptyState.NO_RESULTS -> {
                NoResultsState(
                    modifier = Modifier.fillMaxWidth(),
                    onClearQuery = { onIntent(ItemBrowserIntent.ClearQuery) },
                    onResetFilters = {
                        onIntent(ItemBrowserIntent.FilterChange(ItemBrowserFilter()))
                    }
                )
            }

            BrowserEmptyState.NONE -> Unit
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
            items(uiData.items, key = { it.id.value }) { i ->
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
 * Header row for the Item Browser.
 *
 * Displays an item count and an Add button.
 *
 * @param itemCount Total number of items available (unfiltered).
 * @param onAddItem Invoked when the user taps the Add button.
 */
@Composable
private fun ItemBrowserHeader(
    itemCount: Int,
    onAddItem: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$itemCount items",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        TextButton(onClick = onAddItem) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add item"
            )
            Spacer(Modifier.width(4.dp))
            Text("Add")
        }
    }
}

/**
 * Control bar for Item Browser that wires shared control components to [ItemBrowserIntent].
 *
 * Includes:
 * - Search field
 * - Item status filter
 * - Sort and layout controls
 *
 * @param query Current search query.
 * @param filter Current filter configuration.
 * @param sort Current sort selection.
 * @param layout Current layout selection.
 * @param onIntent Callback for user intents.
 */
@Composable
private fun ControlsBar(
    query: String,
    filter: ItemBrowserFilter,
    sort: BrowserSort,
    layout: BrowserLayout,
    onIntent: (ItemBrowserIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SearchField(
            query = query,
            onQueryChange = { onIntent(ItemBrowserIntent.QueryChange(it)) },
            onClearQuery = { onIntent(ItemBrowserIntent.ClearQuery) }
        )

        FiltersRow(
            filter = filter,
            onFilterChange = { onIntent(ItemBrowserIntent.FilterChange(it)) }
        )

        SortAndLayoutRow(
            sort = sort,
            layout = layout,
            onSortChange = { onIntent(ItemBrowserIntent.SortChange(it)) },
            onLayoutChange = { onIntent(ItemBrowserIntent.LayoutChange(it)) }
        )
    }
}

/**
 * Displays a set of available filters for the search results
 *
 * @param filter: The data class of current filter settings.
 * @param onFilterChange: Invokes to intended user action callback for changing the filter settings.
 */
@Composable
private fun FiltersRow(
    filter: ItemBrowserFilter,
    onFilterChange: (ItemBrowserFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemStatusPickerChip(
            status = filter.itemStatus,
            onStatusChange = { onFilterChange(filter.copy(itemStatus = it)) }
        )
    }
}


/**
 * Renders visible items in the selected [BrowserLayout].
 *
 * @param layout Selected layout for displaying results.
 * @param visibleItems Items to display (already derived by ViewModel/reducer).
 * @param onOpenItem Invoked when an item is selected.
 */
@Composable
private fun ItemResults(
    layout: BrowserLayout,
    visibleItems: List<Item>,
    onOpenItem: (ItemId) -> Unit
) {
    when (layout) {
        BrowserLayout.LIST ->
            ListContents(visibleItems = visibleItems, onOpenItem = onOpenItem)

        BrowserLayout.GRID ->
            GridContents(visibleItems = visibleItems, onOpenItem = onOpenItem)

        BrowserLayout.COMPACT ->
            CompactContents(visibleItems = visibleItems, onOpenItem = onOpenItem)
    }
}


/**
 * List layout for Item Browser results.
 *
 * @param visibleItems Items to display.
 * @param onOpenItem Invoked when an item row is selected.
 */
@Composable
private fun ListContents(
    visibleItems: List<Item>,
    onOpenItem: (ItemId) -> Unit
) {

}

/**
 * Grid layout for Item Browser results.
 *
 * Uses a fixed column count consistent with ContainerBrowser’s grid MVP.
 *
 * @param visibleItems Items to display.
 * @param onOpenItem Invoked when an item tile is selected.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridContents(
    visibleItems: List<Item>,
    onOpenItem: (ItemId) -> Unit
) {

}

/**
 * Compact layout for Item Browser results.
 *
 * MVP implementation reuses [ItemSummaryRow]. If you have an ItemCompactRow in the container
 * package (or shared), swap it here.
 *
 * @param visibleItems Items to display.
 * @param onOpenItem Invoked when an item row is selected.
 */
@Composable
private fun CompactContents(
    visibleItems: List<Item>,
    onOpenItem: (ItemId) -> Unit
) {
    
}