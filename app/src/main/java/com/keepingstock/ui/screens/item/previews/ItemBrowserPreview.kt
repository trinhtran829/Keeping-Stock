package com.keepingstock.ui.screens.item.previews

import android.R.attr.data
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.keepingstock.core.contracts.BrowserEmptyState
import com.keepingstock.core.contracts.BrowserLayout
import com.keepingstock.core.contracts.BrowserSort
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemBrowserFilter
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.UiState
import com.keepingstock.core.contracts.uistates.item.ItemBrowserUiState
import com.keepingstock.data.entities.ItemStatus
import com.keepingstock.ui.screens.item.ItemBrowserScreen
import com.keepingstock.viewmodel.item.ItemBrowserUiData

/**
 * Provides previews for each UI State of the ContainerBrowserScreen
 */

@Preview(showBackground = true)
@Composable
private fun Preview_ItemBrowser_Loading() {
    ItemBrowserScreen(
        uiState = ItemBrowserUiState.Loading(),
        onIntent = { }
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview_ItemBrowser_Error() {
    ItemBrowserScreen(
        uiState = ItemBrowserUiState.Error(message = "Failed to load container."),
        onIntent = { }
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview_ItemBrowser_EmptyReady() {
    ItemBrowserScreen(
        uiState = ItemBrowserUiState.Success(
            items = emptyList(),
            visibleItems = emptyList(),
            query = "",
            filter = ItemBrowserFilter(),
            sort = BrowserSort.NAME_ASC,
            layout = BrowserLayout.COMPACT,
            emptyState = BrowserEmptyState.EMPTY,
        ),
        onIntent = { }
    )
}

/**
 * ---
 * GenAI usage citation:
 * Sample Items and Containers auto-generated with the help of ChatGPT.
 */
@Preview(showBackground = true)
@Composable
private fun Preview_ItemBrowser_PopulatedReady() {
    val items = listOf(
        Item(
            id = ItemId(100L),
            name = "Impact Driver",
            description = "DeWalt Brand 18V brushless",
            imageUri = null,
            status = ItemStatus.STORED,
            containerId = ContainerId(1L)
        ),
        Item(
            id = ItemId(102L),
            name = "Reciprocating Saw",
            description = "Ryobi Brand",
            imageUri = null,
            status = ItemStatus.STORED,
            containerId = ContainerId(1L)
        )
    )

    ItemBrowserScreen(
        uiState = ItemBrowserUiState.Success(
            items = items,
            visibleItems = items,
            query = "",
            filter = ItemBrowserFilter(),
            sort = BrowserSort.NAME_ASC,
            layout = BrowserLayout.COMPACT,
            emptyState = BrowserEmptyState.NONE,
        ),
        onIntent = { }
    )
}