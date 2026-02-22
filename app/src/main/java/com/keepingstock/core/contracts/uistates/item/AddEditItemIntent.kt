package com.keepingstock.core.contracts.uistates.item

import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.TagId
import com.keepingstock.data.entities.ItemStatus

sealed interface AddEditItemIntent {
    // Editable fields
    data class NameChanged(val value: String) : AddEditItemIntent
    data class DescriptionChanged(val value: String) : AddEditItemIntent

    // Container/Status fields
    data class ContainerChanged(val containerId: ContainerId?) : AddEditItemIntent
    data class StatusChanged(val status: ItemStatus) : AddEditItemIntent

    // Image related intent
    data object PickImageClicked : AddEditItemIntent
    data class ImagePicked(val uriString: String) : AddEditItemIntent
    data object RemoveImageClicked : AddEditItemIntent

    // Action intent
    data object SaveClicked : AddEditItemIntent
    data object BackClicked : AddEditItemIntent

    // TODO: Intent related to navigating away
    data object DiscardChangesConfirmed : AddEditItemIntent
    data object DismissDiscardDialog : AddEditItemIntent

    // Tag related Intent
    data class QueryChanged(val value: String) : AddEditItemIntent
    data object ClearQuery : AddEditItemIntent

    data object AddQueryAsTagClicked : AddEditItemIntent
    data class ExistingTagSelected(val tagId: TagId) : AddEditItemIntent
    data class RemoveTagClicked(val tagId: TagId) : AddEditItemIntent

    data class RecommendedTagSelected(val name: String) : AddEditItemIntent
    data object RefreshRecommendations : AddEditItemIntent
}