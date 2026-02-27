package com.keepingstock.ui.components.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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