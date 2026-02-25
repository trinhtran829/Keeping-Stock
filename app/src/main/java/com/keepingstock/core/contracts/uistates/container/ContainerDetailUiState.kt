package com.keepingstock.core.contracts.uistates.container

import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId
import java.util.Date

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
     */
    data class Ready(
        val container: Container,
        val parentContainerName: String?,
        val createdDate: Date,
        val subcontainerCount: Int,
        val itemCount: Int,
        val canDelete: Boolean,
        val deleteBlockedReason: String?, // null when canDelete
    ) : ContainerDetailUiState

    /**
     * An error occurred while loading container contents
     */
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : ContainerDetailUiState
}