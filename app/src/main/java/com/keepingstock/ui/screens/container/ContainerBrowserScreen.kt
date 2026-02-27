package com.keepingstock.ui.screens.container

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.keepingstock.core.contracts.BrowserEmptyState
import com.keepingstock.core.contracts.BrowserLayout
import com.keepingstock.core.contracts.BrowserSort
import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerBrowserFilter
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.intents.container.ContainerBrowserIntent
import com.keepingstock.core.contracts.uistates.container.ContainerBrowserUiState
import com.keepingstock.data.entities.ItemStatus
import com.keepingstock.ui.components.screen.ContainerCompactRow
import com.keepingstock.ui.components.screen.ContainerSummaryRow
import com.keepingstock.ui.components.screen.ContainerTile
import com.keepingstock.ui.components.screen.ErrorContent
import com.keepingstock.ui.components.screen.ItemCompactRow
import com.keepingstock.ui.components.screen.ItemSummaryRow
import com.keepingstock.ui.components.screen.ItemTile
import com.keepingstock.ui.components.screen.LoadingContent

/**
 * Screen for browsing container contents. Render based on ContainerBrowserUiState.
 *
 * TODO: Add addContainer and addItem buttons
 * TODO: Search/Filter/Sort feature
 *
 * @param modifier: Optional modifier for the top-level screen container.
 * @param uiState: Current state of loading, error, or container contents.
 * @param onIntent: Callbacks for user intent.
 * @param onOpenSubcontainer: User intent to open a subcontainer.
 * @param onOpenItem: User intent to open an item detail screen.
 * @param onOpenContainerInfo: User intent to open the current container's info/detail screen.
 * @param onAddContainer: User intent to create a container under the current container.
 * @param onAddItem: User intent to create an item under the current container.
 */
@Composable
fun ContainerBrowserScreen(
    modifier: Modifier = Modifier,
    uiState: ContainerBrowserUiState,
    onIntent: (ContainerBrowserIntent) -> Unit = {},
    onOpenSubcontainer: (containerId: ContainerId) -> Unit = {},
    onOpenItem: (itemId: ItemId) -> Unit = {},
    onOpenContainerInfo: (containerId: ContainerId) -> Unit = {},
    onAddContainer: (parentContainerId: ContainerId?) -> Unit = {},
    onAddItem: (containerId: ContainerId?) -> Unit = {},
    onScan: () -> Unit = { }
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
            uiState = uiState,
            onIntent = onIntent,
            onOpenSubcontainer = onOpenSubcontainer,
            onOpenItem = onOpenItem,
            onOpenContainerInfo = onOpenContainerInfo,
            onAddContainer = onAddContainer,
            onAddItem = onAddItem,
            onScan = onScan
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
 * @param modifier: Optional modifier for the screen container.
 * @param uiState: Current state of loading, error, or container contents.
 * @param onIntent: Callbacks for user intent.
 * @param onOpenSubcontainer: User intent to open a subcontainer.
 * @param onOpenItem: User intent to open an item detail view.
 * @param onOpenContainerInfo: User intent to open container info/detail for containerId.
 * @param onAddContainer: User intent to add a subcontainer under containerId.
 * @param onAddItem: User intent to add an item under containerId.
 */
@Composable
private fun ReadyContent(
    modifier: Modifier,
    uiState: ContainerBrowserUiState.Ready,
    onIntent: (ContainerBrowserIntent) -> Unit,
    onOpenSubcontainer: (containerId: ContainerId) -> Unit = {},
    onOpenItem: (itemId: ItemId) -> Unit = {},
    onOpenContainerInfo: (containerId: ContainerId) -> Unit = {},
    onAddContainer: (parentContainerId: ContainerId?) -> Unit = {},
    onAddItem: (containerId: ContainerId?) -> Unit = {},
    onScan: () -> Unit
) {
    Column() {
        // Content header; mainly counts and info button
        ContentHeader(
            subcontainerCount = uiState.subcontainers.size,
            itemCount = uiState.items.size,
            onOpenContainerInfo = onOpenContainerInfo,
            containerId = uiState.containerId
        )

        ControlsBar(
            query = uiState.query,
            filter = uiState.filter,
            sort = uiState.sort,
            layout = uiState.layout,
            onIntent = onIntent,
            onScan = onScan
        )

        HorizontalDivider()

        // Empty state; not it's own state variant
        when (uiState.emptyState) {
            BrowserEmptyState.EMPTY -> {
                EmptyState(
                    modifier = Modifier.fillMaxSize(),
                    onAddContainer = { onAddContainer(uiState.containerId) },
                    onAddItem = { onAddItem(uiState.containerId) },
                    onScan = onScan
                )
                return
            }

            BrowserEmptyState.NO_RESULTS -> {
                NoResultsState(
                    modifier = Modifier.fillMaxSize(),
                    onClearQuery = { onIntent(ContainerBrowserIntent.ClearQuery) },
                    onResetFilters = {
                        onIntent(ContainerBrowserIntent.FilterChange(ContainerBrowserFilter()))
                    },
                )
                return
            }

            BrowserEmptyState.NONE -> Unit
        }

        PopulatedStateContents(
            layout = uiState.layout,
            visibleSubcontainers = uiState.visibleSubcontainers,
            visibleItems = uiState.visibleItems,
            containerId = uiState.containerId,
            onAddContainer = onAddContainer,
            onAddItem = onAddItem,
            onOpenSubcontainer = onOpenSubcontainer,
            onOpenItem = onOpenItem,
            onScan = onScan
        )
    }
}

/**
 * Provides the content header component. Content Header contains a count of subcontainers and
 * items in the current container, as well as an info button if the current container is not
 * the root container. Clicking reveals the Container details for the current Container.
 *
 * @param
 */
@Composable
private fun ContentHeader(
    subcontainerCount: Int,
    itemCount: Int,
    onOpenContainerInfo: (ContainerId) -> Unit,
    containerId: ContainerId?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val counts = "$subcontainerCount containers • $itemCount items"
        Text(
            text = counts,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        if (containerId != null) {
            TextButton(
                onClick = { onOpenContainerInfo(containerId) },
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Info",
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Info"
                )
            }
        }
    }
}

/**
 * The component between the header and the content, which allows the user to control the content
 * via searching, filtering, and display modes ([BrowserLayout])
 *
 * @param query: The string entered into the search bar
 * @param filter: The data class of filter settings currently in place to filter the results of
 *                the user search query.
 * @param sort: The sort mode currently selected by the user
 * @param layout: The user-selected display layout of the containers and items.
 * @param onIntent: The callback methods to be invoked on user-intent.
 */
@Composable
private fun ControlsBar(
    query: String,
    filter: ContainerBrowserFilter,
    sort: BrowserSort,
    layout: BrowserLayout,
    onIntent: (ContainerBrowserIntent) -> Unit,
    onScan: () -> Unit
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
            onQueryChange = { onIntent(ContainerBrowserIntent.QueryChange(it)) },
            onClearQuery = { onIntent(ContainerBrowserIntent.ClearQuery) }
        )

        FiltersRow(
            filter = filter,
            onFilterChange = { onIntent(ContainerBrowserIntent.FilterChange(it)) }
        )

        SortAndLayoutRow(
            sort = sort,
            layout = layout,
            onSortChange = { onIntent(ContainerBrowserIntent.SortChange(it)) },
            onLayoutChange = { onIntent(ContainerBrowserIntent.LayoutChange(it)) },
            onScan = onScan
        )
    }
}

/**
 * Displays a single-line search input field with built-in clear functionality.
 *
 * @param query Current search query displayed in the field.
 * @param onQueryChange Invoked whenever the user modifies the search text.
 * @param onClearQuery Invoked when the user taps the clear button.
 */
@Composable
private fun SearchField(
    query: String,
    label: String = "Search",
    onQueryChange: (query: String) -> Unit,
    onClearQuery: () -> Unit
) {
    // Search field
    OutlinedTextField(
        value = query,
        onValueChange = { onQueryChange(it) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(
                    onClick = { onClearQuery() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear Search"
                    )
                }
            }
        }
    )
}

/**
 * Displays a set of available filters for the search results
 *
 * @param filter: The data class of current filter settings.
 * @param onFilterChange: Invokes to intended user action callback for changing the filter settings.
 */
@Composable
private fun FiltersRow(
    filter: ContainerBrowserFilter,
    onFilterChange: (ContainerBrowserFilter) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // The actual filter options - if more are added, may need to adjust
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Show subcontainers
            FilterChip(
                selected = filter.includeContainers,
                onClick = {
                    onFilterChange(filter.copy(includeContainers = !filter.includeContainers))
                },
                label = { Text("Containers") }
            )

            // Show items
            FilterChip(
                selected = filter.includeItems,
                onClick = {
                    onFilterChange(filter.copy(includeItems = !filter.includeItems))
                },
                label = { Text("Items") }
            )

            Spacer(Modifier.weight(1f))

            // Show only items with specific status (do not display if not showing items)
            if (filter.includeItems) {
                ItemStatusPickerChip(
                    status = filter.itemStatus,
                    onStatusChange = { onFilterChange(filter.copy(itemStatus = it)) }
                )
            }
        }

        val isDefault =
            filter.includeContainers && filter.includeItems && filter.itemStatus == null

        // Only show clear filters chip row if filters are non-default.
        if (!isDefault) {
            AssistChip(
                onClick = { onFilterChange(ContainerBrowserFilter()) },
                label = { Text("Clear filters") },
            )
        }
    }
}

/**
 * Displays the Item Status Picker dropdown menu component, which allows the user to select
 * which item status items to show
 *
 * @param status: The item status to filter items with.
 * @param onStatusChange: Invoked when the user changes the desired status filter.
 */
@Composable
private fun ItemStatusPickerChip(
    status: ItemStatus?,
    onStatusChange: (ItemStatus?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val label = when (status) {
        null -> "Any status"
        ItemStatus.STORED -> "Stored"
        ItemStatus.TAKEN_OUT -> "Checked out"
    }

    Box() {
        AssistChip(
            onClick = { expanded = true },
            label = { Text(label) }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Any status") },
                onClick = {
                    expanded = false
                    onStatusChange(null)
                }
            )
            DropdownMenuItem(
                text = { Text("Stored") },
                onClick = {
                    expanded = false
                    onStatusChange(ItemStatus.STORED)
                }
            )
            DropdownMenuItem(
                text = { Text("Taken Out") },
                onClick = {
                    expanded = false
                    onStatusChange(ItemStatus.TAKEN_OUT)
                }
            )
        }
    }
}

/**
 * Displays the sorting and layout options available to the user
 *
 * @param sort: The currently selected sort options for the results
 * @param layout: The currently selected display layout for the results.
 * @param onSortChange: Invoked when the user selects a new sorting option
 * @param onLayoutChange: Invoked when the user selects a new display layout.
 */
@Composable
private fun SortAndLayoutRow(
    sort: BrowserSort,
    layout: BrowserLayout,
    onSortChange: (BrowserSort) -> Unit,
    onLayoutChange: (BrowserLayout) -> Unit,
    onScan: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SortMenu(
            sort = sort,
            onSortChange = onSortChange
        )

        LayoutMenu(
            layout = layout,
            onLayoutChange = onLayoutChange
        )

        QrScanMenuOption(
            onScan = onScan
        )
    }
}

/**
 * Displays the sorting menu.
 *
 * @param sort: The currently selected sort options for the results
 * @param onSortChange: Invoked when the user selects a new sorting option
 */
@Composable
private fun SortMenu(
    sort: BrowserSort,
    onSortChange: (BrowserSort) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val label = when (sort) {
        BrowserSort.NAME_ASC -> "Name A-Z"
        BrowserSort.NAME_DESC -> "Name Z-A"
        BrowserSort.CREATED_NEWEST -> "Created (newest)"
        BrowserSort.CREATED_OLDEST -> "Created (oldest)"
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = { expanded = true }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.Sort,
                contentDescription = "Sort"
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Name (A–Z)") },
                onClick = { expanded = false; onSortChange(BrowserSort.NAME_ASC) }
            )
            DropdownMenuItem(
                text = { Text("Name (Z–A)") },
                onClick = { expanded = false; onSortChange(BrowserSort.NAME_DESC) }
            )
            DropdownMenuItem(
                text = { Text("Created (newest)") },
                onClick = { expanded = false; onSortChange(BrowserSort.CREATED_NEWEST) }
            )
            DropdownMenuItem(
                text = { Text("Created (oldest)") },
                onClick = { expanded = false; onSortChange(BrowserSort.CREATED_OLDEST) }
            )
        }
    }
}

/**
 * Displays the layout menu.
 *
 * @param layout: The currently selected display layout for the results.
 * @param onLayoutChange: Invoked when the user selects a new display layout.
 */
@Composable
private fun LayoutMenu(
    layout: BrowserLayout,
    onLayoutChange: (BrowserLayout) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val label = when (layout) {
        BrowserLayout.LIST -> "List"
        BrowserLayout.GRID -> "Grid"
        BrowserLayout.COMPACT -> "Compact"
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = { expanded = true }
        ) {
            Icon(
                imageVector = Icons.Default.ViewModule,
                contentDescription = "Layout"
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("List") },
                onClick = { expanded = false; onLayoutChange(BrowserLayout.LIST) }
            )
            DropdownMenuItem(
                text = { Text("Grid") },
                onClick = { expanded = false; onLayoutChange(BrowserLayout.GRID) }
            )
            DropdownMenuItem(
                text = { Text("Compact") },
                onClick = { expanded = false; onLayoutChange(BrowserLayout.COMPACT) }
            )
        }
    }
}

@Composable
private fun QrScanMenuOption(
    onScan: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onScan
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "Scan"
            )
        }

        Text(
            text = "Scan",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Empty-state UI shown when a container has no subcontainers and no items.
 *
 * @param modifier Modifier applied to the full-size empty-state container.
 * @param onAddContainer Invoked when user chooses to add a container.
 * @param onAddItem Invoked when user chooses to add an item.
 */
@Composable
private fun EmptyState(
    modifier: Modifier,
    onAddContainer: () -> Unit,
    onAddItem: () -> Unit,
    onScan: () -> Unit
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

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = onScan) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Scan to find")
            }
        }
    }
}

/**
 * Empty state UI to show when the container has subcontainers and/or items, but none match the
 * provided filter / query settings.
 *
 * @param modifier: Optional modifier for the screen container.
 * @param onClearQuery Invoked when the user intends to clear the search field's query.
 * @param onResetFilters: Invoked when the user intends to clear the current filter settings
 */
@Composable
private fun NoResultsState(
    modifier: Modifier,
    onClearQuery: () -> Unit,
    onResetFilters: () -> Unit
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
                text = "No results found",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Try a different search or adjust your filters.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(onClick = onClearQuery) { Text("Clear search") }
                OutlinedButton(onClick = onResetFilters) { Text("Reset filters") }
            }
        }
    }
}

/**
 * Component for the main scrollable content area for the Container Browser when data is available.
 *
 * Displays two sections ("Containers" and "Items") with optional add actions and summary rows
 * for each visible entry.
 *
 * @param visibleSubcontainers List of visible subcontainers to display.
 * @param visibleItems List of visible items to display.
 * @param containerId ID of the currently displayed container, or null for root.
 * @param onAddContainer Callback for creating a new subcontainer.
 * @param onAddItem Callback for creating a new item.
 * @param onOpenSubcontainer Invoked when a subcontainer row is selected.
 * @param onOpenItem Invoked when an item row is selected.
 */
@Composable
private fun PopulatedStateContents(
    layout: BrowserLayout,
    visibleSubcontainers: List<Container>,
    visibleItems: List<Item>,
    containerId: ContainerId?,
    onAddContainer: (ContainerId?) -> Unit,
    onAddItem: (ContainerId?) -> Unit,
    onOpenSubcontainer: (ContainerId) -> Unit,
    onOpenItem: (ItemId) -> Unit,
    onScan: () -> Unit
) {
    when (layout) {
        BrowserLayout.LIST ->
            ListContents(
                visibleSubcontainers = visibleSubcontainers,
                visibleItems = visibleItems,
                containerId = containerId,
                onAddContainer = onAddContainer,
                onAddItem = onAddItem,
                onOpenSubcontainer = onOpenSubcontainer,
                onOpenItem = onOpenItem,
                onScan = onScan
            )

        BrowserLayout.GRID -> {
            GridContents(
                visibleSubcontainers = visibleSubcontainers,
                visibleItems = visibleItems,
                containerId = containerId,
                onAddContainer = onAddContainer,
                onAddItem = onAddItem,
                onOpenSubcontainer = onOpenSubcontainer,
                onOpenItem = onOpenItem,
                onScan = onScan
            )
        }

        BrowserLayout.COMPACT -> {
            CompactContents(
                visibleSubcontainers = visibleSubcontainers,
                visibleItems = visibleItems,
                containerId = containerId,
                onAddContainer = onAddContainer,
                onAddItem = onAddItem,
                onOpenSubcontainer = onOpenSubcontainer,
                onOpenItem = onOpenItem,
                onScan = onScan
            )
        }
    }
}

/**
 * Displays container and item results in a basic vertical list layout. This layout uses summary
 * row components for both containers and items.
 *
 * @param visibleSubcontainers Subcontainers to display.
 * @param visibleItems Items to display.
 * @param containerId ID of the current container, or null if root.
 * @param onAddContainer Callback for adding a subcontainer.
 * @param onAddItem Callback for adding an item.
 * @param onOpenSubcontainer Invoked when a subcontainer row is selected.
 * @param onOpenItem Invoked when an item row is selected.
 */
@Composable
private fun ListContents(
    visibleSubcontainers: List<Container>,
    visibleItems: List<Item>,
    containerId: ContainerId?,
    onAddContainer: (ContainerId?) -> Unit,
    onAddItem: (ContainerId?) -> Unit,
    onOpenSubcontainer: (ContainerId) -> Unit,
    onOpenItem: (ItemId) -> Unit,
    onScan: () -> Unit
) {
    // Populated state; scrolling list with individual sections for subcontainers/items
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SectionHeader(
                sectionTitle = "Containers",
                onAdd = { onAddContainer(containerId) },
            )
        }

        if (visibleSubcontainers.isNotEmpty()) {
            items(visibleSubcontainers, key = { it.id.value }) { container ->
                ContainerSummaryRow(
                    modifier = Modifier,
                    container = container,
                    onClick = { onOpenSubcontainer(container.id) }
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }

        item {
            SectionHeader(
                sectionTitle = "Items",
                onAdd = { onAddItem(containerId) },
            )
        }

        if (visibleItems.isNotEmpty()) {
            items(visibleItems, key = { it.id.value }) { item ->
                ItemSummaryRow(
                    modifier = Modifier,
                    item = item,
                    onClick = { onOpenItem(item.id) }
                )
            }
        }

        item { ContentEndQrButton(onScan = onScan) }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

/**
 * Displays container and item results using a responsive grid layout. individual entries are
 * rendered as tiles.
 *
 * @param visibleSubcontainers Subcontainers to display.
 * @param visibleItems Items to display.
 * @param containerId ID of the current container, or null if root.
 * @param onAddContainer Callback for adding a subcontainer.
 * @param onAddItem Callback for adding an item.
 * @param onOpenSubcontainer Invoked when a container tile is selected.
 * @param onOpenItem Invoked when an item tile is selected.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridContents(
    visibleSubcontainers: List<Container>,
    visibleItems: List<Item>,
    containerId: ContainerId?,
    onAddContainer: (ContainerId?) -> Unit,
    onAddItem: (ContainerId?) -> Unit,
    onOpenSubcontainer: (ContainerId) -> Unit,
    onOpenItem: (ItemId) -> Unit,
    onScan: () -> Unit
) {
    val gridState = rememberLazyGridState()

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
            top = 8.dp,
            bottom = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Containers header
        item(
            span = { GridItemSpan(maxLineSpan) }
        ) {
            SectionHeader(
                sectionTitle = "Containers",
                onAdd = { onAddContainer(containerId) }
            )
        }

        items(
            items = visibleSubcontainers,
            key = { it.id.value }
        ) { container ->
            ContainerTile (
                container = container,
                onClick = { onOpenSubcontainer(container.id) }
            )
        }

        // Items header
        item(
            span = { GridItemSpan(maxLineSpan) }
        ) {
            Spacer(Modifier.height(4.dp))
        }

        item(
            span = { GridItemSpan(maxLineSpan) }
        ) {
            SectionHeader(
                sectionTitle = "Items",
                onAdd = { onAddItem(containerId) }
            )
        }

        items(
            items = visibleItems,
            key = { it.id.value }
        ) { item ->
            ItemTile(
                item = item,
                onClick = { onOpenItem(item.id) }
            )
        }

        item { ContentEndQrButton(onScan = onScan) }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

/**
 * Displays container and item results in a denser, compact list layout.
 *
 * @param visibleSubcontainers Subcontainers to display.
 * @param visibleItems Items to display.
 * @param containerId ID of the current container, or null if root.
 * @param onAddContainer Callback for adding a subcontainer.
 * @param onAddItem Callback for adding an item.
 * @param onOpenSubcontainer Invoked when a subcontainer row is selected.
 * @param onOpenItem Invoked when an item row is selected.
 */
@Composable
private fun CompactContents(
    visibleSubcontainers: List<Container>,
    visibleItems: List<Item>,
    containerId: ContainerId?,
    onAddContainer: (ContainerId?) -> Unit,
    onAddItem: (ContainerId?) -> Unit,
    onOpenSubcontainer: (ContainerId) -> Unit,
    onOpenItem: (ItemId) -> Unit,
    onScan: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            SectionHeader(
                sectionTitle = "Containers",
                onAdd = { onAddContainer(containerId) }
            )
        }

        items(visibleSubcontainers, key = { it.id.value }) { c ->
            ContainerCompactRow (
                container = c,
                onClick = { onOpenSubcontainer(c.id) }
            )
        }

        item { Spacer(Modifier.height(6.dp)) }

        item {
            SectionHeader(
                sectionTitle = "Items",
                onAdd = { onAddItem(containerId) }
            )
        }

        items(visibleItems, key = { it.id.value }) { i ->
            ItemCompactRow(
                item = i,
                onClick = { onOpenItem(i.id) }
            )
        }

        item { ContentEndQrButton(onScan = onScan) }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

/**
 * Component to show the section title as well as an Add button that utilizes the onAdd navigation
 * callback.
 *
 * @param sectionTitle: The title of the section (i.e. "Containers" or "Items)
 * @param onAdd: Navigation callback for creating a new instance of the section type (e.g.
 *               container or item)
 */
@Composable
private fun SectionHeader(
    sectionTitle: String,
    onAdd: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            sectionTitle,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )

        TextButton(
            onClick = onAdd
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add $sectionTitle"
            )
            Spacer(Modifier.width(4.dp))
            Text("Add")
        }
    }
}

/**
 * Simple button component for QR Scanning navigation
 *
 * @param onScan: User intent to navigate to the QR Scan screen.
 */
@Composable
private fun ContentEndQrButton(
    onScan: () -> Unit
) {
    Spacer(Modifier.height(16.dp))
    OutlinedButton(
        onClick = onScan,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Scan QR Code")
    }
}

/**
 * Preview of the search/controls bar
 */
@Preview(showBackground = true)
@Composable
private fun Preview_ControlsBar() {
    ControlsBar(
        query = "DEMO SEARCH",
        filter = ContainerBrowserFilter(false),
        sort = BrowserSort.NAME_ASC,
        layout = BrowserLayout.LIST,
        onIntent = { },
        onScan = { }
    )
}

/**
 * Preview of the search field row
 */
@Preview(showBackground = true)
@Composable
private fun Preview_SearchField() {
    SearchField(
        query = "",
        onQueryChange = { },
        onClearQuery = { }
    )
}

/**
 * Preview of the filter row
 */
@Preview(showBackground = true)
@Composable
private fun Preview_FilterRow() {
    FiltersRow(
        filter = ContainerBrowserFilter(),
        onFilterChange = { }
    )
}

/**
 * Preview of the filter row with an option selected to show Clear button.
 */
@Preview(showBackground = true)
@Composable
private fun Preview_FilterRowOptionSelected() {
    FiltersRow(
        filter = ContainerBrowserFilter(false, false),
        onFilterChange = { }
    )
}

/**
 * Preview of the Sort and Layout Row
 */
@Preview(showBackground = true)
@Composable
private fun Preview_SortAndLayoutRow() {
    SortAndLayoutRow(
        sort = BrowserSort.NAME_ASC,
        layout = BrowserLayout.LIST,
        onSortChange = { },
        onLayoutChange = { },
        onScan = { }
    )
}

/**
 * Preview of the Sort Menu
 */
@Preview(showBackground = true)
@Composable
private fun Preview_SortMenu() {
    SortMenu(
        sort = BrowserSort.NAME_ASC,
        onSortChange = { }
    )
}

/**
 * Preview of the Layout Menu
 */
@Preview(showBackground = true)
@Composable
private fun Preview_LayoutMenu() {
    LayoutMenu(
        layout = BrowserLayout.LIST,
        onLayoutChange = { }
    )
}

/**
 * Preview of the SectionHeader
 */
@Preview(showBackground = true)
@Composable
private fun Preview_SectionHeader(){
    SectionHeader(
        sectionTitle = "Containers",
        onAdd = {}
    )
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
            imageUri = null,
            status = ItemStatus.STORED,
            containerId = ContainerId(1L)
        )
    )
}