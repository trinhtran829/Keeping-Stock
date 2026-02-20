package com.keepingstock.ui.screens.container.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
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

