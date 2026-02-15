package com.keepingstock.ui.screens.item.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.uistates.item.ItemDetailUiState
import com.keepingstock.data.entities.ItemStatus
import com.keepingstock.ui.screens.item.ItemDetailsScreen

/**
 * Provides previews for each UI State of the ItemDetailScreen.
 */

@Preview(showBackground = true)
@Composable
private fun Preview_ItemDetail_Loading() {
    ItemDetailsScreen(
        uiState = ItemDetailUiState.Loading,
        onBack = {},
        onEdit = {},
        onMove = {},
        onDelete = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview_ItemDetail_Error() {
    ItemDetailsScreen(
        uiState = ItemDetailUiState.Error("Failed to load container details."),
        onBack = {},
        onEdit = {},
        onMove = {},
        onDelete = {},
    )
}

/**
 * ---
 * GenAI usage citation:
 * Sample container detail data auto-generated with the help of ChatGPT.
 * Prompt: "Please generate data for a sample object with the following class signature:"
 */
@Preview(showBackground = true)
@Composable
private fun Preview_ItemDetail_Ready() {
    val itemId = ItemId(100L)
    ItemDetailsScreen(
        uiState = ItemDetailUiState.Ready(
            itemId = itemId,
            item = Item(
                id = itemId,
                name = "Impact Driver",
                description = "DeWalt Brand 18V brushless",
                imageUri = null,
                status = ItemStatus.STORED,
                containerId = ContainerId(1L)
            ),
            parentContainerName = "Garage"
        ),
        onBack = {},
        onEdit = {},
        onMove = {},
        onDelete = {},
    )
}
