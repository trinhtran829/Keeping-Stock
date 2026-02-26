package com.keepingstock.core.contracts.uistates.item

import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.Tag
import com.keepingstock.data.entities.ItemStatus
import java.util.Date

/**
 * UI state contract for the Add/Edit Item flow.
 *
 * This sealed interface represents all possible renderable states for the screen:
 * - Loading: data is being prepared.
 * - Ready: form is available for viewing/editing.
 * - Error: unrecoverable failure during initialization.
 *
 * The screen renders exclusively from this model and emits user intents that
 * mutate or replace it via a controller/ViewModel.
 */
sealed interface AddEditItemUiState {
    /**
     * Form is being loaded/initialized
     */
    data object Loading: AddEditItemUiState

    /**
     * Form-ready UI state for the Add/Edit Item screen.
     *
     * This state is the single source of truth for rendering the editable form and associated
     * UI controls. The UI should treat this as immutable and emit [AddEditItemIntent] events
     * for all user actions (field edits, selections, save, navigation, tag edits).
     *
     * Expected behavior:
     * - [mode] and [itemId] determine whether this is CREATE or EDIT.
     * - [containerId]/[containerName] represent the current parent container selection.
     * - [status] and [checkoutDate] should remain consistent with container selection rules
     *   (e.g., items not in a container should typically be treated as checked out / taken out).
     * - Tag fields ([selectedTags], [tagQuery], [tagSuggestions], [tagRecommendations]) are
     *   UI-driven; the VM should handle suggestions/recommendations and enforce limits.
     * - [isDirty] tracks unsaved changes; UI may use it for discard confirmation on back/cancel.
     * - [validation] contains field-level validation errors to display inline.
     *
     * @param mode: CREATE vs EDIT mode for the flow.
     * @param itemId: The item being edited. Null when creating a new item.
     * @param containerId: The currently selected container for the item. Null indicates no
     *                     container.
     * @param containerName: Display name for the currently selected container (nullable).
     * @param availableParents: Candidate container options for selection (demo list now;
     *                          repo-backed later).
     *
     * @param name: Editable item name (required).
     * @param description: Editable item description (optional; stored as empty string when unset).
     * @param imageUri: Optional image URI string for display and persistence.
     * @param status: Item status (e.g., stored vs taken out).
     * @param createdDate: Creation timestamp (usually immutable once created).
     * @param checkoutDate: Timestamp for when the item was checked out (nullable when stored).
     *
     * @param selectedTags: Tags currently attached to the item in the form.
     * @param tagQuery: Current tag input query string.
     * @param tagSuggestions: Suggested existing tags based on [tagQuery].
     * @param tagRecommendations: Recommended tag names (e.g., from image/name-based heuristics).
     * @param isRecommending: Whether recommendation computation/loading is in progress.
     * @param inputError: Tag input validation error message (e.g., invalid characters) or null.
     * @param maxTags: Maximum number of tags allowed for an item.
     * @param suggestionsLimit: Maximum number of tag suggestions to display.
     *
     * @param isSaving: Whether a save operation is in progress (disables Save UI).
     * @param isDirty: Whether the form has unsaved changes (used for discard confirmation).
     * @param validation: Field-level validation errors (name/container).
     * @param canChangeParent: Whether the container selection is editable in the current mode.
     */
    data class Ready(
        val mode: Mode,
        val itemId: ItemId?,                        // null when creating
        val containerId: ContainerId?,              // null = no container
        val containerName: String?,                 // need for displaying
        val availableParents: List<ParentOption>,   // TODO: Improve for hierarchical display?

        val name: String,
        val description: String,
        val imageUri: String?,
        val status: ItemStatus,
        val createdDate: Date,
        val checkoutDate: Date?,

        // Tagging
        val selectedTags: List<Tag> = emptyList(),
        val tagQuery: String = "",
        val tagSuggestions: List<Tag> = emptyList(),
        val tagRecommendations: List<String> = emptyList(),
        val isRecommending: Boolean = false,
        val inputError: String? = null,
        val maxTags: Int = 20,
        val suggestionsLimit: Int = 8,

        // Form Lifecycle
        val isSaving: Boolean = false,
        val isDirty: Boolean = false,
        val validation: Validation = Validation(),
        val canChangeParent: Boolean = true
    ) : AddEditItemUiState {
        enum class Mode { CREATE, EDIT }

        data class ParentOption(
            val id: ContainerId?, // null = root
            val name: String
        )

        // TODO: Anything else need to be validated? Tags?
        data class Validation(
            val nameError: String? = null,
            val containerError: String? = null
        )

        val canAddMore: Boolean get() = selectedTags.size < maxTags
    }

    /**
     * Error occurred while preparing the form.
     */
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : AddEditItemUiState
}