package com.keepingstock.core.contracts.uistates.container

import com.keepingstock.core.contracts.ContainerId

/**
 * UI state for the Container Details screen.
 */
sealed interface ContainerDetailUiState {

    /**
     * Container details are being loaded
     */
    data object Loading : ContainerDetailUiState

    /**
     * Container details successfully loaded and ready for display.
     *
     * TODO: itemCount/subcontainerCount - needed?
     */
    data class Ready(
        val containerId: ContainerId,
        val containerName: String,
        val description: String?,
        val imageUri: String?,
        val parentContainerId: ContainerId?,
        val subcontainerCount: Int,
        val itemCount: Int,
        val canDelete: Boolean,
        val deleteBlockedReason: String?, // null when canDelete
    ) : ContainerDetailUiState

    /**
     * An error occurred while loading container contents
     * TODO: consider a retry option?
     */
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : ContainerDetailUiState
}
