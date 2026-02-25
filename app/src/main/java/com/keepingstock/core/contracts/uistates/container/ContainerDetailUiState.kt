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
     *
     * Count semantics:
     * - [subcontainerCount] represents the number of direct child containers.
     * - [itemCount] represents the number of items directly contained in this container.
     * - These values are intended for display purposes only and should reflect repository-derived
     *   counts at the time the state was emitted.
     *
     * Deletion rules:
     * - [canDelete] determines whether the Delete action should be enabled in the UI.
     * - When [canDelete] is false, [deleteBlockedReason] must contain a user-facing explanation
     *   describing why deletion is not allowed.
     * - When [canDelete] is true, [deleteBlockedReason] should be null.
     *
     * Invariants:
     * - If [canDelete] is false, [deleteBlockedReason] must not be null.
     * - If [canDelete] is true, [deleteBlockedReason] must be null.
     *
     * The ViewModel is responsible for:
     * - Loading the container and its parent (if any).
     * - Computing child container and item counts.
     * - Enforcing deletion rules and populating the corresponding fields.
     *
     * @param container The container being displayed.
     * @param parentContainerName The display name of the parent container, or null if this
     *                            container is at the root level.
     * @param subcontainerCount Number of direct child containers.
     * @param itemCount Number of direct items contained within this container.
     * @param canDelete Whether the container can be deleted.
     * @param deleteBlockedReason Explanation shown when deletion is not allowed.
     */
    data class Ready(
        val container: Container,
        val parentContainerName: String?,
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