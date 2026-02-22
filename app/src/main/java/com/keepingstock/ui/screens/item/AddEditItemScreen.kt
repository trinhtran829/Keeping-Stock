package com.keepingstock.ui.screens.item

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.uistates.item.AddEditItemIntent
import com.keepingstock.core.contracts.uistates.item.AddEditItemUiState
import com.keepingstock.ui.components.screen.ErrorContent
import com.keepingstock.ui.components.screen.LoadingContent

/**
 * Add/Edit Item screen that renders based on uiState.
 *
 * State handling:
 * - [AddEditItemUiState.Loading] shows a loading indicator.
 * - [AddEditItemUiState.Error] shows an error message.
 * - [AddEditItemUiState.Ready] shows the editable form and emits [AddEditItemIntent]
 *   events via [onIntent].
 *
 * Navigation:
 * - [onNavigateBack] is called when the user confirms leaving (e.g. discard changes) or taps
 *   Cancel when the form is not dirty.
 *
 * :param modifier: Modifier applied to the screen.
 * :param uiState: Current UI state for the Add/Edit Item flow.
 * :param onIntent: Callback for user intents (field edits, save, image changes, etc.).
 * :param onNavigateBack: Callback to navigate up/back out of this screen.
 */
@Composable
fun AddEditItemScreen(
    modifier: Modifier = Modifier,
    uiState: AddEditItemUiState,
    onIntent: (AddEditItemIntent) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    Column(modifier = modifier.padding(16.dp)) {
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

/**
 * Renders the editable Add/Edit Item form for the [AddEditItemUiState.Ready] state.
 *
 * UI responsibilities:
 * - Owns local, UI-only discard confirmation dialog state (not part of UiState).
 * - Intercepts system back when [uiState.isDirty] and prompts for discard confirmation.
 * - Hosts an Activity Result launcher to pick an image and emits [AddEditItemIntent.ImagePicked].
 *
 * :param modifier: Modifier applied to the scrolling content container.
 * :param uiState: Ready state containing current field values, validation, and flags.
 * :param onIntent: Callback for emitting user intents to the state owner (demo controller / VM).
 * :param onNavigateBack: Callback invoked when navigation away from the screen is confirmed.
 */
@Composable
private fun AddEditItemReadyContent(
    modifier: Modifier,
    uiState: AddEditItemUiState.Ready,
    onIntent: (AddEditItemIntent) -> Unit,
    onNavigateBack: () -> Unit
) {
    // TODO: Local UI-only dialog state (kept out of UiState to keep demo simple).
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }

    // Intercept system back when form is dirty (so we can prompt for discard confirmation)
    AddEditItemBackHandling(
        isDirty = uiState.isDirty,
        onIntent = onIntent,
        showDiscardDialog = showDiscardDialog,
        onShowDiscardDialog = { showDiscardDialog = it },
        onDiscardConfirmed = onNavigateBack
    )

    // Gets an object that can launch the system image picker.
    val pickImageLauncher = rememberPickImageLauncher(onIntent)
}

/**
 * Handles back navigation behavior for the Add/Edit Item form.
 *
 * Behavior:
 * - When [isDirty] is true, intercepts the system back button and shows a discard confirmation dialog.
 * - When [showDiscardDialog] is true, displays an [AlertDialog] allowing the user to discard changes
 *   or cancel and stay on the screen.
 *
 * :param isDirty: Whether the form has unsaved changes.
 * :param onIntent: Callback for emitting user intents to the state owner (demo controller / VM).
 * :param showDiscardDialog: Controls whether the discard confirmation dialog is visible.
 * :param onShowDiscardDialog: Setter for [showDiscardDialog].
 * :param onDiscardConfirmed: Callback invoked when the user confirms discarding changes.
 */
@Composable
private fun AddEditItemBackHandling(
    isDirty: Boolean,
    onIntent: (AddEditItemIntent) -> Unit,
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
                        onIntent(AddEditItemIntent.DiscardChangesConfirmed)
                        onDiscardConfirmed()
                    }
                ) { Text("Discard") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onShowDiscardDialog(false)
                    }
                ) { Text("Cancel") }
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

/**
 * Creates and remembers an Activity Result launcher for selecting an image from the system picker.
 *
 * When the picker returns a non-null [Uri], this emits [AddEditItemIntent.ImagePicked]
 * through [onIntent]. The caller is responsible for invoking `launch(...)` on the returned
 * launcher.
 *
 * :param onIntent: Callback used to emit [AddEditItemIntent] events.
 *
 * :return: A launcher that can start the visual media picker and deliver a picked image [Uri].
 */
@Composable
private fun rememberPickImageLauncher(
    onIntent: (AddEditItemIntent) -> Unit
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
) { uri: Uri? ->
    if (uri != null)
        onIntent(AddEditItemIntent.ImagePicked(uri.toString()))
}