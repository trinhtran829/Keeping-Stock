package com.keepingstock.core.contracts.uistates

import com.keepingstock.ui.models.ItemSummaryUi

/**
 * UI state for the Item Browser screen.
 *
 * This state is intentionally UI-oriented and does not expose Room entities.
 * ViewModels are responsible for mapping persistence/domain models into UI models.
 *
 * Notes:
 * - Include the current query so the screen can render a search field reliably.
 * - "Empty" is represented as Ready(items = emptyList())
 *
 * TODO: Add filters (tags, status, container) to ready?
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
    data class Ready(
        val query: String = "",
        val items: List<ItemSummaryUi> = emptyList()
    ) : ItemBrowserUiState

    /**
     * Loading failed. Query is included so the UI can keep showing the search text.
     */
    data class Error(
        val query: String = "",
        val message: String
    ) : ItemBrowserUiState
}