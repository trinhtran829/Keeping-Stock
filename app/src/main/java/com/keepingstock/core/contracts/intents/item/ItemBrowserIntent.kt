package com.keepingstock.core.contracts.intents.item

import com.keepingstock.core.contracts.BrowserLayout
import com.keepingstock.core.contracts.BrowserSort
import com.keepingstock.core.contracts.ItemBrowserFilter

/**
 * Intents emitted by the Item Browser UI and handled by the corresponding ViewModel.
 *
 * All user-driven events that can affect the Item Browser screen's state.
 */
sealed interface ItemBrowserIntent {

    /**
     * Indicates that the user has modified the search query. This intent is typically emitted on
     * each change to the search input field for "search as you type" behavior.
     *
     * Expected behavior:
     * - Update the stored query value.
     * - Recompute visible results using the new query.
     * - Optionally debounce processing to avoid excessive recomputation.
     */
    data class QueryChange(val query: String) : ItemBrowserIntent

    /**
     * Requests that the current search query be cleared. This intent is typically emitted when
     * the user taps a clear ("X") button in the search field.
     *
     * Expected behavior:
     * - Reset the stored query to an empty string.
     * - Restore unfiltered results (subject to active filters and sort order).
     */
    data object ClearQuery : ItemBrowserIntent

    /**
     * Updates the active filter configuration for the item browser. This intent is emitted when
     * the user changes any filter option.
     *
     * Expected behavior:
     * - Store the new filter configuration.
     * - Recompute visible results using the updated filter.
     *
     * :param filter: The new filter configuration.
     */
    data class FilterChange(val filter: ItemBrowserFilter) : ItemBrowserIntent

    /**
     * Updates the active sort order for displayed items. This intent is emitted when the user
     * selects a different sorting option.
     *
     * Expected behavior:
     * - Store the selected sort mode.
     * - Reorder visible results accordingly.
     *
     * :param sort: The selected sort mode.
     */
    data class SortChange(val sort: BrowserSort) : ItemBrowserIntent

    /**
     * Updates the preferred layout mode for displaying results. This intent is emitted when
     * the user switches between layouts.
     *
     * Expected behavior:
     * - Store the selected layout mode.
     * - Emit updated state so the UI can recompose using the new layout.
     *
     * :param layout: The selected layout mode.
     */
    data class LayoutChange(val layout: BrowserLayout) : ItemBrowserIntent

    /**
     * Requests that the VM retry loading item data after a failure.
     *
     * Expected behavior:
     * - Re-fetch data from repositories.
     * - Reapply current query, filter, and sort settings.
     * - Transition the UI state from Error -> Loading -> Ready, or back to Error if retry fails.
     */
    data object Retry : ItemBrowserIntent
}
