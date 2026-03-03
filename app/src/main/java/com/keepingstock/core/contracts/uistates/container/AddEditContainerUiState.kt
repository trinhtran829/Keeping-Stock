package com.keepingstock.core.contracts.uistates.container

import com.keepingstock.core.contracts.ContainerId

sealed interface AddEditContainerUiState {
    /**
     * Form is being loaded/initialized
     */
    data object Loading: AddEditContainerUiState

    /**
     * Form is ready for display and/or editing.
     *
     * Expected behavior:
     * - In CREATE mode, [containerId] is null and fields are typically initialized empty (or from
     *   provided navigation defaults such as [parentContainerId]).
     * - In EDIT mode, [containerId] is non-null and fields are initialized from repository-loaded
     *   data.
     * - UI edits emit [AddEditContainerIntent] events and the state owner updates this state.
     * - [isDirty] becomes true when any user-editable field differs from the initially loaded
     *   values.
     * - Validation updates occur as fields change and are surfaced through [validation].
     *
     * Parent selection behavior:
     * - [parentContainerId] null represents Root.
     * - [availableParents] contains the list of selectable parent containers for this container.
     * - When [canChangeParent] is false, the UI should show the parent as read-only.
     *
     * Saving behavior:
     * - When [isSaving] is true, the UI should disable Save and present a saving indicator.
     * - After a successful save, the state owner should trigger navigation away (popBackStack) via
     *   destination-owned navigation or an effect model (not modeled in UiState).
     *
     * @param mode CREATE vs EDIT mode for this screen.
     * @param containerId The container being edited (null for CREATE).
     * @param parentContainerId Selected parent container (null represents Root).
     * @param parentContainerName Display name of the selected parent (null represents Root).
     * @param availableParents Available parent options for selection.
     * @param name Current value of the container name field.
     * @param description Current value of the container description field.
     * @param imageUri Current image URI string (nullable/blank indicates no image).
     * @param isSaving Whether a save operation is currently in progress.
     * @param isDirty Whether the form has unsaved user edits.
     * @param validation Validation results for the current field values.
     * @param canChangeParent Whether the user may change the selected parent container.
     */
    data class Ready(
        val mode: Mode,
        val containerId: ContainerId?,              // null when creating
        val parentContainerId: ContainerId?,        // null = root
        val parentContainerName: String?,           // need for displaying
        val availableParents: List<ParentOption>,   // TODO: Improve for hierarchical display?
        val name: String,
        val description: String?,
        val imageUri: String?,
        val isSaving: Boolean = false,
        val isDirty: Boolean = false,
        val validation: Validation = Validation(),
        val canChangeParent: Boolean = true
    ) : AddEditContainerUiState {
        enum class Mode { CREATE, EDIT }

        data class ParentOption(
            val id: ContainerId?, // null = root
            val name: String
        )

        data class Validation(
            val nameError: String? = null
        )
    }

    /**
     * Error occurred while preparing the form.
     */
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : AddEditContainerUiState
}