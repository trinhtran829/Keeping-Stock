package com.keepingstock.ui.screens.item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.UiState
import com.keepingstock.viewmodel.item.ItemBrowserUiData

@Composable
fun ItemBrowserScreen(
    modifier: Modifier = Modifier,
    uiState: UiState<ItemBrowserUiData>,
    onOpenItem: (itemId: ItemId) -> Unit = {}
) {
    Column(modifier = modifier.padding(16.dp)) {
        // Show state info, will refactor with real UI later.
        // Show state if viewModel exists, otherwise placeholder
        when (uiState) {
            is UiState.Loading ->
                Text("Item Browser Screen (loading...)")

            is UiState.Success ->
                Text("Item Browser Screen (${uiState.data.items.size} items)")

            is UiState.Error ->
                Text("Item Browser Screen (error: ${uiState.message})")
        }
    }
}

@Preview(showSystemUi = false, showBackground = true)
@Composable
private fun ItemBrowserScreenPreview() {
    // Preview without ViewModel
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Item Browser Screen (placeholder)")

        Button(
            onClick = {},
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Open Example Item 01")
        }

        Button(
            onClick = {},
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Go to Container Browser (last open)")
        }
    }
}