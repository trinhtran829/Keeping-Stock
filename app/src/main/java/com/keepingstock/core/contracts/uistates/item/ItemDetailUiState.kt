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
     * Item details successfully loaded and ready for display
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