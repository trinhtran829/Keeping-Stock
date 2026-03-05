package com.keepingstock.core.contracts.uistates.item

import com.keepingstock.core.contracts.BrowserEmptyState
import com.keepingstock.core.contracts.BrowserLayout
import com.keepingstock.core.contracts.BrowserSort
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemBrowserFilter
import com.keepingstock.data.entities.ItemStatus

/**
 * UI state for the Item Browser screen.
 *
 * This state is intentionally UI-oriented and does not expose Room entities to UI.
 * ViewModels are responsible for mapping persistence/domain models into UI models.
 *
 * Notes:
 * - Include the current query so the screen can render a search field reliably.
 * - "Empty" is represented as Ready(items = emptyList())
 */
sealed interface ItemBrowserUiState {

    /**
     * Items are being loaded and/or the initial state is not ready to render results yet.
     * Query is included so the search field can still show what the user typed.
     */
    data class Loading(
        val query: String = ""
    ) : ItemBrowserUiState

    /**
     * Items successfully loaded for the current query.
     * When items is empty, the screen should show an "empty results" UI.
     */
    data class Success(
        val items: List<Item>,
        val visibleItems: List<Item>,
        val query: String,
        val filter: ItemBrowserFilter,
        val sort: BrowserSort,
        val layout: BrowserLayout,
        val emptyState: BrowserEmptyState
    ) : ItemBrowserUiState

    /**
     * Loading failed. Query is included so the UI can keep showing the search text.
     */
    data class Error(
        val query: String = "",
        val message: String,
        val cause: Throwable? = null
    ) : ItemBrowserUiState
}