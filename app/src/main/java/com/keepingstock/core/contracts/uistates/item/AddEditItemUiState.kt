package com.keepingstock.core.contracts.uistates.item

import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.Tag
import com.keepingstock.data.entities.ItemStatus
import java.util.Date

sealed interface AddEditItemUiState {
    /**
     * Form is being loaded/initialized
     */
    data object Loading: AddEditItemUiState

    /**
     * Form is ready for display and/or editing
     *
     * Not using Item model because containerId needs to be able to be null
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

        val selectedTags: List<Tag> = emptyList(),
        val tagQuery: String = "",
        val tagSuggestions: List<Tag> = emptyList(),
        val tagRecommendations: List<String> = emptyList(),
        val isRecommending: Boolean = false,
        val inputError: String? = null,
        val maxTags: Int = 20,
        val suggestionsLimit: Int = 8,

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