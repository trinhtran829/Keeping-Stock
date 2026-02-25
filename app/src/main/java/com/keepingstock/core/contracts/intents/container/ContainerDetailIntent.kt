package com.keepingstock.core.contracts.intents.container

/**
 * Intents emitted by the Container Detail UI and handled by the corresponding ViewModel.
 *
 * All user-driven events that can affect the Container Detail screen's state.
 */
sealed interface ContainerDetailIntent {
    /**
     * Request that the VM retry loading container details after a failure
     *
     * Expected behavior:
     * - Re-fetch container info from repos
     * - Recompute derived values such as item and subcontainer counts
     * - Transition the UI state from Error -> Loading -> Ready, or back to Error if retry fails
     */
    data object Retry : ContainerDetailIntent

    /**
     * User has confirmed that they wish to delete the indicated container
     *
     * MVP has the UI handling deletion confirmation
     *
     * Expected behavior:
     * - Invoke the repo to delete the container (if allowed)
     * - Emit a one-time nav/snackbar effect for the destination to implement (e.g. "Successfully
     *   deleted", pop backstack on success)
     * - If deletion fails, remain on screen and surface an error message
     */
    data object DeleteConfirmed : ContainerDetailIntent
}