package com.keepingstock.ui.screens.item.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.UiState
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
        uiState = UiState.Loading
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview_ItemBrowser_Error() {
    ItemBrowserScreen(
        uiState = UiState.Error("Failed to load container."),
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview_ItemBrowser_EmptyReady() {
    ItemBrowserScreen(
        uiState = UiState.Success(
            data = ItemBrowserUiData (
                items = emptyList()
            )
        )
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
        uiState = UiState.Success(
            data = ItemBrowserUiData(
                items = items
            )
        )
    )
}