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
     * Container contents ready for display.
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