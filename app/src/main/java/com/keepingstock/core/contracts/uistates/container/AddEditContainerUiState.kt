package com.keepingstock.core.contracts.uistates.container

import com.keepingstock.core.contracts.ContainerId

sealed interface AddEditContainerUiState {
    /**
     * Form is being loaded/initialized
     */
    data object Loading: AddEditContainerUiState

    /**
     * Form is ready for display and/or editing
     *
     * Not using Container model because containerId needs to be able to be null
     */
    data class Ready(
        val mode: Mode,
        val containerId: ContainerId?,              // null when creating
        val parentContainerId: ContainerId?,        // null = root
        val parentContainerName: String?,           // need for displaying
        val availableParents: List<ParentOption>,   // TODO: Improve for hierarchical display?
        val name: String,
        val description: String,
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

        // TODO: Anything else need to be validated? Tags?
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