package com.keepingstock.core.contracts.uistates.container

import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.intents.container.ContainerBrowserFilter
import com.keepingstock.core.contracts.intents.container.ContainerBrowserLayout
import com.keepingstock.core.contracts.intents.container.ContainerBrowserSort

/**
 * UI state for the Container Browser screen.
 *
 * This state is intentionally UI-oriented and does not expose Room entities.
 * ViewModels are responsible for mapping persistence/domain models into UI models.
 *
 * Notes:
 * - Root is represented by containerId == null.
 * - "Empty" containers are represented by Ready(subcontainers = emptyList(),
 *   items = emptyList()).
 */
sealed interface ContainerBrowserUiState {

    /**
     * Container contents are being loaded
     */
    data object Loading : ContainerBrowserUiState

    /**
     * Container contents successfully loaded and ready for display.
     *
     * Data layers:
     * - [subcontainers] and [items] represent the complete, unfiltered contents of the current
     *   container.
     * - [visibleSubcontainers] and [visibleItems] represent the subset of results that should
     *   currently be displayed after applying search, filter, and sorting logic.
     *
     * Empty state handling:
     * - When both [subcontainers] and [items] are empty, the container itself is empty.
     * - When [visibleSubcontainers] and [visibleItems] are empty but base lists are not, the
     *   screen is in a "no results" state.
     * - The ViewModel must set [emptyState] accordingly so the UI can render the appropriate
     *   messaging.
     *
     * Invariants:
     * - [visibleSubcontainers] must always be derived from [subcontainers].
     * - [visibleItems] must always be derived from [items].
     * - [emptyState] must be consistent with the contents of the visible lists.
     *
     * The ViewModel is responsible for maintaining these invariants and ensuring that all derived
     * fields are recomputed whenever query, filter, sort, or source data changes.
     *
     * @param containerId The identifier of the currently displayed container, or null when
     *                    displaying the root container.
     * @param containerName The display name of the current container.
     * @param subcontainers All direct child containers of the current container, prior to any
     *                     filtering or searching.
     * @param items All items contained in the current container, prior to any filtering or
     *              searching.
     * @param visibleSubcontainers The list of subcontainers that should currently be displayed
     *                             after applying search, filter, and sort rules.
     * @param visibleItems The list of items that should currently be displayed after applying
     *                     search, filter, and sort rules.
     * @param query The current search query entered by the user.
     * @param filter The active filter configuration.
     * @param sort The active sort mode.
     * @param layout The selected layout mode for displaying results.
     * @param emptyState Indicates which empty-state message (if any) should be displayed by the UI.
     */
    data class Ready(
        val containerId: ContainerId?,         // null represents root container
        val containerName: String,

        // All results
        val subcontainers: List<Container>,
        val items: List<Item>,

        // Results derived based on filter/search.
        val visibleSubcontainers: List<Container>,
        val visibleItems: List<Item>,

        // UI control state
        val query: String,
        val filter: ContainerBrowserFilter,
        val sort: ContainerBrowserSort,
        val layout: ContainerBrowserLayout,

        // Because showing "Nothing here yet" for empty search/filter results doesn't make
        // sense, so I'll need the VM to specify what kind of empty state it's in.
        val emptyState: ContainerBrowserEmptyState
    ) : ContainerBrowserUiState

    /**
     * An error occurred while loading container contents
     * TODO: consider a retry option?
     */
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : ContainerBrowserUiState
}

/**
 * EMPTY_CONTAINER: show message “There's nothing here yet!”
 * NO_RESULTS: show “No results found”
 * NONE: Not empty, render list
 */
enum class ContainerBrowserEmptyState {
    NONE,
    EMPTY_CONTAINER,
    NO_RESULTS
}