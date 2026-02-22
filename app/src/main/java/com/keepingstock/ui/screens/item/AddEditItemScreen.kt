package com.keepingstock.ui.screens.item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.ItemId

@Composable
fun AddEditItemScreen(
    itemId: ItemId?,
    containerId: ContainerId?,
    modifier: Modifier = Modifier,
    onSave: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
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
}
