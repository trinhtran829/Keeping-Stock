package com.keepingstock.core.contracts.uistates.item

import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.data.entities.ItemStatus

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
        val itemName: String,
        val description: String?,
        val imageUri: String?,
        val parentContainerId: ContainerId?,
        val parentContainerName: String?,
        val status: ItemStatus
    ) : ItemDetailUiState
}