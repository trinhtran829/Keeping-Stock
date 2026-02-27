package com.keepingstock.ui.components.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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