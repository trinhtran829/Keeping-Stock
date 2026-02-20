package com.keepingstock.ui.screens.container

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.uistates.container.AddEditContainerIntent
import com.keepingstock.core.contracts.uistates.container.AddEditContainerUiState
import com.keepingstock.ui.components.screen.ErrorContent
import com.keepingstock.ui.components.screen.LoadingContent

@Composable
fun AddEditContainerScreen(
    modifier: Modifier = Modifier,
    uiState: AddEditContainerUiState,
    onIntent: (AddEditContainerIntent) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    when (uiState) {
        AddEditContainerUiState.Loading ->
            LoadingContent(modifier.fillMaxSize())

        is AddEditContainerUiState.Error ->
            ErrorContent(modifier = modifier.fillMaxSize(), message = uiState.message)

        is AddEditContainerUiState.Ready -> {

        }
    }

    /*
    // TODO: OLD PLACEHOLDER CODE: REMOVE WHEN UI IS UPDATED
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
    */
}

@Composable
private fun AddEditContainerReadyContent(
    modifier: Modifier,
    uiState: AddEditContainerUiState.Ready,
    onIntent: (AddEditContainerIntent) -> Unit,
    onNavigateBack: () -> Unit
) {
    // TODO: Local UI-only dialog state (kept out of UiState to keep demo simple).
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }

    // Intercept system back when form is dirty (so we can prompt for discard confirmation)
    BackHandler(enabled = uiState.isDirty) {
        showDiscardDialog = true
    }

    if (showDiscardDialog) {

    }
}

/**
 * Handles when the user presses the back button (system-back only right now?)
 */
@Composable
private fun AddEditContainerBackHandling(
    isDirty: Boolean,
    showDiscardDialog: Boolean,
    onShowDiscardDialog: (Boolean) -> Unit,
    onDiscardConfirmed: () -> Unit
) {
    // Intercept system back when dirty.
    BackHandler(enabled = isDirty) {
        onShowDiscardDialog(true)
    }

    // Show alert dialog requesting confirmation of discard
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { onShowDiscardDialog(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onShowDiscardDialog(false)
                        onDiscardConfirmed()
                    }
                ) { Text("Discard") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onShowDiscardDialog(false)
                    }) { Text("Cancel") }
            },
            title = {
                Text("Discard changes?")
            },
            text = {
                Text("You have unsaved changes. Discard them and leave this screen?")
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        )
    }
}