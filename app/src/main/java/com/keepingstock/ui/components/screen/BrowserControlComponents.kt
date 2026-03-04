package com.keepingstock.ui.components.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.keepingstock.core.contracts.BrowserLayout
import com.keepingstock.core.contracts.BrowserSort
import com.keepingstock.data.entities.ItemStatus

/**
 * Displays a single-line search input field with built-in clear functionality.
 *
 * @param query Current search query displayed in the field.
 * @param onQueryChange Invoked whenever the user modifies the search text.
 * @param onClearQuery Invoked when the user taps the clear button.
 */
@Composable
fun SearchField(
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
 * Displays the Item Status Picker dropdown menu component, which allows the user to select
 * which item status items to show
 *
 * @param status: The item status to filter items with.
 * @param onStatusChange: Invoked when the user changes the desired status filter.
 */
@Composable
fun ItemStatusPickerChip(
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
fun SortAndLayoutRow(
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

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { expanded = true }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Default.Sort,
            contentDescription = "Sort"
        )

        Spacer(Modifier.width(4.dp))

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

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { expanded = true }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ViewModule,
            contentDescription = "Layout"
        )

        Spacer(Modifier.width(4.dp))

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

/**
 * Component to allow users to navigate to the QR Scan screen
 *
 * @param onScan: User intent to navigate to the QR scan screen.
 */
@Composable
private fun QrScanMenuOption(
    onScan: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onScan() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = "Scan"
        )

        Spacer(Modifier.width(4.dp))

        Text(
            text = "Scan",
            style = MaterialTheme.typography.bodyMedium
        )
    }
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