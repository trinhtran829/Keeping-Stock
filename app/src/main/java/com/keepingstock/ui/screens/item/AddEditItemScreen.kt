package com.keepingstock.ui.screens.item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.uistates.item.AddEditItemIntent
import com.keepingstock.core.contracts.uistates.item.AddEditItemUiState
import com.keepingstock.ui.components.screen.ErrorContent
import com.keepingstock.ui.components.screen.LoadingContent

@Composable
fun AddEditItemScreen(
    modifier: Modifier = Modifier,
    uiState: AddEditItemUiState,
    onIntent: (AddEditItemIntent) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    when (uiState) {
        AddEditItemUiState.Loading ->
            LoadingContent(modifier.fillMaxSize())

        is AddEditItemUiState.Error ->
            ErrorContent(modifier = modifier.fillMaxSize(), message = uiState.message)

        is AddEditItemUiState.Ready ->
            AddEditItemReadyContent(
                modifier = modifier.fillMaxSize(),
                uiState = uiState,
                onIntent = onIntent,
                onNavigateBack = onNavigateBack
            )
    }
    /*
    // TODO(REMOVE): Replace old code after screen is updated

    val mode = if (itemId == null) "ADD" else "EDIT"

    Column(modifier = modifier.padding(16.dp)) {
        Text("Add/Edit Item Screen (placeholder)")
        Text("mode = $mode")
        Text("itemId = ${itemId ?: "null"}")
        Text("containerId = ${containerId ?: "null"}")

        Button(onClick = onSave, modifier = Modifier.padding(top = 12.dp)) {
            Text("Save (placeholder)")
        }
        Button(onClick = onCancel, modifier = Modifier.padding(top = 12.dp)) {
            Text("Cancel")
        }
    }
     */
}

@Composable
private fun AddEditItemReadyContent(
    modifier: Modifier,
    uiState: AddEditItemUiState.Ready,
    onIntent: (AddEditItemIntent) -> Unit,
    onNavigateBack: () -> Unit
) {

}