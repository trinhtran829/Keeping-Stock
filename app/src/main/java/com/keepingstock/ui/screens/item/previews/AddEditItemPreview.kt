package com.keepingstock.ui.screens.item.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.keepingstock.core.contracts.uistates.item.AddEditItemUiState
import com.keepingstock.ui.screens.item.AddEditItemScreen


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