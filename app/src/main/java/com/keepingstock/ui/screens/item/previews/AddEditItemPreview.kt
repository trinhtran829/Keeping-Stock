package com.keepingstock.ui.screens.item.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.Tag
import com.keepingstock.core.contracts.TagId
import com.keepingstock.core.contracts.uistates.item.AddEditItemUiState
import com.keepingstock.data.entities.ItemStatus
import com.keepingstock.ui.screens.item.AddEditItemScreen
import java.util.Date


@Preview(showBackground = true)
@Composable
private fun Preview_AddEditItem_Loading() {
    AddEditItemScreen(uiState = AddEditItemUiState.Loading)
}

@Preview(showBackground = true)
@Composable
private fun Preview_AddEditItem_Error() {
    AddEditItemScreen(uiState = AddEditItemUiState.Error("Failed to load item."))
}

@Preview(showBackground = true)
@Composable
private fun Preview_AddEditItem_Create() {
    AddEditItemScreen(
        uiState = AddEditItemUiState.Ready(
            mode = AddEditItemUiState.Ready.Mode.CREATE,
            itemId = null,
            containerId = null,
            containerName = null,
            availableParents = listOf(
                AddEditItemUiState.Ready.ParentOption(ContainerId(1L), "Garage"),
                AddEditItemUiState.Ready.ParentOption(ContainerId(2L), "Kitchen")
            ),

            name = "",
            description = "",
            imageUri = null,
            status = ItemStatus.TAKEN_OUT,
            createdDate = Date(),
            checkoutDate = Date(),

            selectedTags = emptyList(),
            maxTags = 20,
            suggestionsLimit = 8,

            isSaving = false,
            isDirty = false,
            validation = AddEditItemUiState.Ready.Validation(),
            canChangeParent = true
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview_AddEditItem_Edit() {
    AddEditItemScreen(
        uiState = AddEditItemUiState.Ready(
            mode = AddEditItemUiState.Ready.Mode.EDIT,
            itemId = ItemId(10L),
            containerId = ContainerId(1L),
            containerName = "Garage",
            availableParents = listOf(
                AddEditItemUiState.Ready.ParentOption(ContainerId(1L), "Garage"),
                AddEditItemUiState.Ready.ParentOption(ContainerId(2L), "Kitchen"),
                AddEditItemUiState.Ready.ParentOption(ContainerId(3L), "Shed")
            ),

            name = "Impact Driver",
            description = "18V brushless",
            imageUri = null,
            status = ItemStatus.STORED,
            createdDate = Date(),
            checkoutDate = null,

            selectedTags = listOf(
                Tag(TagId(1L), "Tools"),
                Tag(TagId(2L), "Electrical")
            ),
            maxTags = 20,
            suggestionsLimit = 8,

            isSaving = false,
            isDirty = false,
            validation = AddEditItemUiState.Ready.Validation(),
            canChangeParent = true
        )
    )
}