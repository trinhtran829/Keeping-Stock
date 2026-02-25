package com.keepingstock.core.contracts.intents.item

/**
 * Intents emitted by the Item Detail UI and handled by the corresponding ViewModel.
 *
 * All user-driven events that can affect the Item Detail screen's state.
 */
sealed interface ItemDetailIntent {
    /**
     * Request that the VM retry loading item details after a failure
     *
     * Expected behavior:
     * - Re-fetch item info from repos
     * - Recompute derived values such as item and item counts
     * - Transition the UI state from Error -> Loading -> Ready, or back to Error if retry fails
     */
    data object Retry : ItemDetailIntent

    /**
     * User has confirmed that they wish to delete the indicated item
     *
     * MVP has the UI handling deletion confirmation
     *
     * Expected behavior:
     * - Invoke the repo to delete the item (if allowed)
     * - Emit a one-time nav/snackbar effect for the destination to implement (e.g. "Successfully
     *   deleted", pop backstack on success)
     * - If deletion fails, remain on screen and surface an error message
     */
    data object DeleteConfirmed : ItemDetailIntent
}