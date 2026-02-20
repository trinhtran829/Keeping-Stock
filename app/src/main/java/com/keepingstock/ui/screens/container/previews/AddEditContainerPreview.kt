package com.keepingstock.ui.screens.container.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.uistates.container.AddEditContainerUiState
import com.keepingstock.ui.screens.container.AddEditContainerScreen

/**
 * Provides previews for each UI State of the AddEditContainerScreen.
 */

@Preview(showBackground = true)
@Composable
private fun Preview_AddEditContainer_Loading() {
    AddEditContainerScreen(uiState = AddEditContainerUiState.Loading)
}

@Preview(showBackground = true)
@Composable
private fun Preview_AddEditContainer_Error() {
    AddEditContainerScreen(uiState = AddEditContainerUiState.Error("Failed to load form."))
}

@Preview(showBackground = true)
@Composable
private fun Preview_AddEditContainer_Create() {
    AddEditContainerScreen(
        uiState = AddEditContainerUiState.Ready(
            mode = AddEditContainerUiState.Ready.Mode.CREATE,
            containerId = null,
            parentContainerId = ContainerId(1L),
            parentContainerName = "Garage",
            availableParents = listOf(
                AddEditContainerUiState.Ready.ParentOption(null, "Root"),
                AddEditContainerUiState.Ready.ParentOption(ContainerId(1L), "Garage"),
                AddEditContainerUiState.Ready.ParentOption(ContainerId(2L), "Kitchen"),
            ),
            name = "",
            description = "",
            imageUri = null,
            canChangeParent = true
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview_AddEditContainer_Edit_WithValidationError() {
    AddEditContainerScreen(
        uiState = AddEditContainerUiState.Ready(
            mode = AddEditContainerUiState.Ready.Mode.EDIT,
            containerId = ContainerId(10L),
            parentContainerId = null,
            parentContainerName = "Root",
            availableParents = listOf(
                AddEditContainerUiState.Ready.ParentOption(null, "Root"),
                AddEditContainerUiState.Ready.ParentOption(ContainerId(1L), "Garage"),
            ),
            name = "   ",
            description = "Example description",
            imageUri = null,
            canChangeParent = true,
            validation = AddEditContainerUiState.Ready.Validation(nameError = "Name is required.")
        )
    )
}