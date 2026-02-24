package com.keepingstock.core.contracts.uistates.container

import com.keepingstock.core.contracts.ContainerId

/**
 * Adds Intent as event model for AddEditContainerScreen
 */
sealed interface AddEditContainerIntent {
    // Editable fields
    data class NameChanged(val value: String) : AddEditContainerIntent
    data class DescriptionChanged(val value: String) : AddEditContainerIntent
    data class ParentChanged(val parentId: ContainerId?) : AddEditContainerIntent

    // Image related intent
    data object PickImageClicked : AddEditContainerIntent
    data class ImagePicked(val uriString: String) : AddEditContainerIntent
    data object RemoveImageClicked : AddEditContainerIntent

    // TODO: Add tagging intent

    // Action intent
    data object SaveClicked : AddEditContainerIntent
    data object BackClicked : AddEditContainerIntent

    // TODO: Intent related to navigating away
    data object DiscardChangesConfirmed : AddEditContainerIntent
    data object DismissDiscardDialog : AddEditContainerIntent
}
