package com.keepingstock.core.contracts.uistates.item

import com.keepingstock.core.contracts.Item

/**
 * UI state for the Item Details screen.
 */
sealed interface ItemDetailUiState {

    /**
     * Item details are being loaded
     */
    data object Loading : ItemDetailUiState

    /**
     * Item details successfully loaded and ready for display.
     *
     * The ViewModel is responsible for:
     * - Loading the item by id.
     * - Resolving [parentContainerName] using the item's container id (when present).
     *
     * @param item The item being displayed.
     * @param parentContainerName The display name of the item's container, or null when the item
     *                            is not assigned to a container.
     */
    data class Ready(
        val item: Item,
        val parentContainerName: String? = null
    ) : ItemDetailUiState

    /**
     * An error occurred while loading item contents
     */
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : ItemDetailUiState
}