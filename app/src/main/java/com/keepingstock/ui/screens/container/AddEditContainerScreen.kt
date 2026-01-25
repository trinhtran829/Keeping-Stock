package com.keepingstock.ui.screens.container

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddEditContainerScreen(
    containerId: String?,
    parentContainerId: String?,
    modifier: Modifier = Modifier,
    onSave: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val mode = if (containerId == null) "ADD" else "EDIT"

    Column (modifier = modifier.padding(16.dp)) {
        Text("Add/Edit Container Screen (placeholder)")
        Text("mode = $mode")
        Text("containerId = ${containerId ?: "null"}")
        Text("parentContainerId = ${parentContainerId ?: "null"}")

        Button(onClick = onSave, modifier = Modifier.padding(top = 12.dp)) {
            Text("Save (placeholder)")
        }
        Button(onClick = onCancel, modifier = Modifier.padding(top = 12.dp)) {
            Text("Cancel")
        }
    }
}