package com.keepingstock.core.contracts.intents.item

import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.TagId
import com.keepingstock.data.entities.ItemStatus

/**
 * This Intent interface defines the contract for user intent for the AddEditItem UI
 *
 * Each implementation represents a single, intentional user action
 * (button press, text edit, selection, confirmation, etc.).
 *
 * These intents are emitted by the UI layer and consumed by the controller/ViewModel to
 * update [com.keepingstock.core.contracts.uistates.item.AddEditItemUiState] and/or trigger effects.
 */
sealed interface AddEditItemIntent {
    /**
     * Indicates that the user has modified the item name field.
     *
     * Expected behavior:
     * - Update the stored name value.
     * - Mark the form dirty.
     * - Re-run validation and update [AddEditItemUiState.Ready.validation.nameError] as needed.
     *
     * @param value: The latest name input value.
     */
    data class NameChanged(val value: String) : AddEditItemIntent

    /**
     * Indicates that the user has modified the item description field.
     *
     * Expected behavior:
     * - Update the stored description value.
     * - Mark the form dirty.
     *
     * @param value: The latest description input value.
     */
    data class DescriptionChanged(val value: String) : AddEditItemIntent

    /**
     * Indicates that the user has selected a different container for the item.
     *
     * Expected behavior:
     * - Update [AddEditItemUiState.Ready.containerId] and [containerName].
     * - Mark the form dirty.
     * - Enforce container/status consistency rules. Common MVP rule:
     *   - If containerId becomes null, force status to TAKEN_OUT and ensure checkoutDate is set.
     *
     * @param containerId: The newly selected container id, or null for no container.
     */
    data class ContainerChanged(val containerId: ContainerId?) : AddEditItemIntent

    /**
     * Indicates that the user has changed the item status (stored vs taken out).
     *
     * Expected behavior:
     * - Update [AddEditItemUiState.Ready.status].
     * - Update [checkoutDate] when transitioning to TAKEN_OUT (set to now if null).
     * - Clear [checkoutDate] when transitioning to STORED.
     * - Mark the form dirty.
     * - If containerId is null and status changes are disallowed, ignore or correct and surface
     *   a validation message (MVP: force TAKEN_OUT when containerId is null).
     *
     * :param status: The newly selected status.
     */
    data class StatusChanged(val status: ItemStatus) : AddEditItemIntent

    /**
     * Indicates that the user has requested to pick or change the item image.
     *
     * Expected behavior:
     * - Currently for MVP, UI launches the system picker.
     * - VM may ignore this intent
     */
    data object PickImageClicked : AddEditItemIntent

    /**
     * Indicates that the system image picker has returned a selected image.
     *
     * Expected behavior:
     * - Update [AddEditItemUiState.Ready.imageUri].
     * - Mark the form dirty.
     *
     * :param uriString: String form of the selected image URI.
     */
    data class ImagePicked(val uriString: String) : AddEditItemIntent

    /**
     * Indicates that the user has removed the current image from the form.
     *
     * Expected behavior:
     * - Clear [AddEditItemUiState.Ready.imageUri].
     * - Mark the form dirty.
     */
    data object RemoveImageClicked : AddEditItemIntent

    /**
     * Indicates that the user has requested to save the current form.
     *
     * Expected behavior:
     * - Validate current inputs.
     * - If valid, persist via repositories and transition isSaving (Ready -> Ready(isSaving=true)).
     * - Emit a one-off success effect (snackbar) and navigate back on success.
     * - On failure, surface an error state or inline error while preserving form contents.
     */
    data object SaveClicked : AddEditItemIntent

    /**
     * Indicates that the user has requested to leave the screen (UI back/cancel).
     *
     * Expected behavior:
     * - Currently handled by UI for MVP
     * - VM can ignore this intent for now.
     */
    data object BackClicked : AddEditItemIntent

    /**
     * Indicates that the user has confirmed discarding unsaved changes.
     *
     * Expected behavior:
     * - Allow navigation away (pop back stack).
     */
    data object DiscardChangesConfirmed : AddEditItemIntent

    /**
     * Indicates that the user dismissed the discard confirmation dialog.
     *
     * Expected behavior:
     * - Keep the user on the screen and clear dialog visibility (if VM-managed).
     */
    data object DismissDiscardDialog : AddEditItemIntent

    /**
     * Indicates that the user has changed the tag query text.
     *
     * Expected behavior:
     * - Update [AddEditItemUiState.Ready.tagQuery].
     * - Validate tag input format and update [inputError].
     * - Update [tagSuggestions] (optionally debounce to reduce repository calls).
     *
     * @param value: The latest query text.
     */
    data class QueryChanged(val value: String) : AddEditItemIntent

    /**
     * Indicates that the user wants to add the current query as a tag.
     *
     * Expected behavior:
     * - Normalize and validate the query.
     * - If tag exists, add it to selectedTags; otherwise stage a new tag for creation.
     * - Clear query and suggestions.
     * - Mark form dirty.
     */
    data object AddQueryAsTagClicked : AddEditItemIntent

    /**
     * Indicates that the user selected an existing tag from the tag suggestions.
     *
     * Expected behavior:
     * - Add the selected tag to [selectedTags] if not already present.
     * - Clear query and suggestions.
     * - Mark form dirty.
     *
     * @param tagId: The selected existing tag id.
     */
    data class ExistingTagSelected(val tagId: TagId) : AddEditItemIntent

    /**
     * Indicates that the user removed a selected tag.
     *
     * Expected behavior:
     * - Remove the tag from [selectedTags].
     * - Mark form dirty.
     *
     * @param tagId: The tag id to remove.
     */
    data class RemoveTagClicked(val tagId: TagId) : AddEditItemIntent

    /**
     * Indicates that the user selected a recommended tag.
     *
     * Expected behavior:
     * - Add this tag name to [selectedTags] (reusing an existing tag if it exists).
     * - Mark form dirty.
     *
     * @param name: The recommended tag name.
     */
    data class RecommendedTagSelected(val name: String) : AddEditItemIntent

    /**
     * Indicates that the user requested refreshed tag recommendations.
     *
     * Expected behavior:
     * - Set [isRecommending] true.
     * - Recompute recommendations (from image/name/description as available).
     * - Update [tagRecommendations] and set [isRecommending] false.
     */
    data object RefreshRecommendations : AddEditItemIntent
}