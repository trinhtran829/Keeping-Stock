package com.keepingstock.ui.components.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Empty-state UI shown when a browser has no subcontainers and no items.
 *
 * @param modifier Modifier applied to the full-size empty-state container.
 * @param onAddContainer Invoked when user chooses to add a container.
 * @param onAddItem Invoked when user chooses to add an item.
 */
@Composable
fun EmptyState(
    modifier: Modifier,
    onAddContainer: () -> Unit,
    onAddItem: () -> Unit,
    isItemBrowser: Boolean,
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
                text =
                    "Add ${if (!isItemBrowser) "a container or" else "an"} item to get started.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!isItemBrowser) {
                    Button(onClick = onAddContainer) { Text("Add container") }
                }

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
fun NoResultsState(
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