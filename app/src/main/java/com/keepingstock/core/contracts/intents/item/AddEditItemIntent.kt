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
    /* ---------- Editable text fields ---------- */
    data class NameChanged(val value: String) : AddEditItemIntent
    data class DescriptionChanged(val value: String) : AddEditItemIntent

    /* ---------- Container / status selection ---------- */
    data class ContainerChanged(val containerId: ContainerId?) : AddEditItemIntent
    data class StatusChanged(val status: ItemStatus) : AddEditItemIntent

    /* ---------- Image selection ---------- */
    data object PickImageClicked : AddEditItemIntent
    data class ImagePicked(val uriString: String) : AddEditItemIntent
    data object RemoveImageClicked : AddEditItemIntent

    /* ---------- Primary actions / navigation ---------- */
    data object SaveClicked : AddEditItemIntent
    data object BackClicked : AddEditItemIntent
    data object DiscardChangesConfirmed : AddEditItemIntent
    data object DismissDiscardDialog : AddEditItemIntent

    /* ---------- Tag editing ---------- */
    data class QueryChanged(val value: String) : AddEditItemIntent

    data object AddQueryAsTagClicked : AddEditItemIntent
    data class ExistingTagSelected(val tagId: TagId) : AddEditItemIntent
    data class RemoveTagClicked(val tagId: TagId) : AddEditItemIntent

    data class RecommendedTagSelected(val name: String) : AddEditItemIntent
    data object RefreshRecommendations : AddEditItemIntent
}