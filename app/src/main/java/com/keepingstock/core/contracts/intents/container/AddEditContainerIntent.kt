package com.keepingstock.core.contracts.intents.container

import com.keepingstock.core.contracts.ContainerId

/**
 * Adds Intent as event model for AddEditContainerScreen.
 *
 * These intents represent user-driven events emitted by the Add/Edit Container UI. The state owner
 * (demo controller or ViewModel) is responsible for reducing these intents into updated
 * [AddEditContainerUiState] and triggering side effects such as saving and navigation.
 */
sealed interface AddEditContainerIntent {
    /**
     * Indicates that the user has edited the Name field.
     *
     * Expected behavior:
     * - Update the stored name value.
     * - Mark the form dirty.
     * - Re-run validation and update [AddEditContainerUiState.Ready.validation].
     *
     * @param value: New name field value.
     */
    data class NameChanged(val value: String) : AddEditContainerIntent

    /**
     * Indicates that the user has edited the Description field.
     *
     * Expected behavior:
     * - Update the stored description value.
     * - Mark the form dirty.
     *
     * @param value: New description field value.
     */
    data class DescriptionChanged(val value: String) : AddEditContainerIntent

    /**
     * Indicates that the user has changed the selected parent container. Null parent represents
     * Root.
     *
     * Expected behavior:
     * - If [AddEditContainerUiState.Ready.canChangeParent] is false, ignore this intent.
     * - Otherwise update the selected parent id and derived parent display name.
     * - Mark the form dirty.
     *
     * @param parentId: Newly selected parent container id (null represents Root).
     */
    data class ParentChanged(val parentId: ContainerId?) : AddEditContainerIntent

    /**
     * Indicates that the user wants to pick or change the container image.
     *
     * Expected behavior:
     * - For MVP, UI is handling this intent, no state owner action required and this event
     *   can be ignored.
     */
    data object PickImageClicked : AddEditContainerIntent

    /**
     * Indicates that an image was selected from the system picker.
     *
     * Expected behavior:
     * - Update [AddEditContainerUiState.Ready.imageUri].
     * - Mark the form dirty.
     *
     * @param uriString: URI string returned by the system picker.
     */
    data class ImagePicked(val uriString: String) : AddEditContainerIntent

    /**
     * Indicates that the user wants to remove the currently selected image.
     *
     * Expected behavior:
     * - Clear [AddEditContainerUiState.Ready.imageUri].
     * - Mark the form dirty.
     */
    data object RemoveImageClicked : AddEditContainerIntent

    /**
     * Indicates that the user wants to save the container with any changes made.
     *
     * Expected behavior:
     * - Validate current field values.
     * - If invalid, update validation fields and do not save.
     * - If valid, set [AddEditContainerUiState.Ready.isSaving] true while saving.
     * - On success, trigger navigation away (popBackStack) via destination or effect model.
     */
    data object SaveClicked : AddEditContainerIntent

    /**
     * Indicates that the user wants to leave the screen (via Cancel or top-bar back).
     *
     * Expected behavior:
     * - MVP has UI handling this intent, can be ignored by state owner
     * - If the form is dirty, UI should show a discard confirmation dialog.
     * - If not dirty, navigate away immediately.
     */
    data object BackClicked : AddEditContainerIntent

    /**
     * Indicates that the user confirmed discarding unsaved changes.
     *
     * Expected behavior:
     * - Navigate away (popBackStack).
     * - No save should occur.
     */
    data object DiscardChangesConfirmed : AddEditContainerIntent

    /**
     * Indicates that the user dismissed the discard confirmation dialog without leaving.
     *
     * Expected behavior:
     * - MVP has UI handling this event, so this can be ignored by the state owner.
     * - Clear the dialog visibility (if dialog visibility is modeled in state owner).
     */
    data object DismissDiscardDialog : AddEditContainerIntent
}