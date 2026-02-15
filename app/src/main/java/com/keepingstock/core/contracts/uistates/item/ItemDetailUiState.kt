package com.keepingstock.core.contracts.uistates.item

import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.uistates.container.ContainerDetailUiState
import com.keepingstock.data.entities.ItemStatus
import java.util.Date

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
        val itemId: ItemId,
        val item: Item,
        val parentContainerName: String? = null
    ) : ItemDetailUiState

    /**
     * An error occurred while loading item contents
     * TODO: consider a retry option?
     */
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : ItemDetailUiState
}