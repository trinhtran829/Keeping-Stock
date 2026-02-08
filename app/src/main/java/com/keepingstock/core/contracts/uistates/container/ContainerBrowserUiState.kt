package com.keepingstock.core.contracts.uistates.container

import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.Item

/**
 * UI state for the Container Browser screen.
 *
 * This state is intentionally UI-oriented and does not expose Room entities.
 * ViewModels are responsible for mapping persistence/domain models into UI models.
 *
 * Notes:
 * - Root is represented by containerId == null.
 * - "Empty" containers are represented by Ready(subcontainers = emptyList(), items = emptyList()).
 *
 * TODO: Add presentation options to Ready (list/grid, sorting, filtering)
 */
sealed interface ContainerBrowserUiState {

    /**
     * Container contents are being loaded
     */
    data object Loading : ContainerBrowserUiState

    /**
     * Container contents successfully loaded and ready for display.
     * Both subcontainer and item lists are empty = display "empty container" msg
    */
    data class Ready(
        val containerId: Long?,         // null represents root container
        val containerName: String,
        val subcontainers: List<Container>,
        val items: List<Item>
    ) : ContainerBrowserUiState

    /**
     * An error occurred while loading container contents
     * TODO: consider a retry option?
     */
    data class Error(val message: String) : ContainerBrowserUiState
}