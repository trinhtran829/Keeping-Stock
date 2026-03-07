package com.keepingstock.core.contracts.intents.utility

import com.keepingstock.core.contracts.ContainerId

/**
 * Intents emitted by the Select Container UI and handled by the corresponding ViewModel.
 *
 * These intents represent user actions for browsing the container hierarchy, choosing a
 * destination container, and completing or canceling the selection flow.
 */
sealed interface SelectContainerIntent {
    /**
     * Indicates that the user wants to browse into the specified container.
     *
     * Expected behavior:
     * - Update the currently browsed container context.
     * - Recompute the breadcrumb path from Root to the new browsing location.
     * - Reload the visible child containers for the selected browsing location.
     *
     * @param containerId: The container to browse into, or null to browse the root level.
     */
    data class EnterContainer(val containerId: ContainerId?) : SelectContainerIntent

    /**
     * Indicates that the user has selected a breadcrumb segment in the current path.
     *
     * Expected behavior:
     * - Jump browsing context directly to the selected breadcrumb container.
     * - Recompute breadcrumb path and visible child containers for that location.
     *
     * @param containerId: The container represented by the clicked breadcrumb, or null for Root.
     */
    data class ClickBreadcrumb(val containerId: ContainerId?) : SelectContainerIntent

    /**
     * Indicates that the user has selected a destination container for the current move flow.
     *
     * Expected behavior:
     * - Update the currently selected destination container.
     * - Recompute any derived UI indicators such as selected-row state.
     *
     * @param containerId: The selected destination container, or null to indicate Root.
     */
    data class ChangeSelection(val containerId: ContainerId?) : SelectContainerIntent

    /**
     * Indicates that the user has canceled the Select Container flow.
     *
     * Expected behavior:
     * - Emit a navigation side effect to leave the screen without returning a selection.
     */
    data object Cancel: SelectContainerIntent

    /**
     * Indicates that the user has confirmed the current destination selection.
     *
     * Expected behavior:
     * - Emit the currently selected container id as a one-time result.
     * - Navigate back to the calling destination after returning that result.
     */
    data object Confirm: SelectContainerIntent
}