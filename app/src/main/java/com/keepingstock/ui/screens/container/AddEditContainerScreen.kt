package com.keepingstock.ui.screens.container

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.launch

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
            AddEditContainerReadyContent(
                modifier = modifier.fillMaxSize(),
                uiState = uiState,
                onIntent = onIntent,
                onNavigateBack = onNavigateBack
            )
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

    // What actions to emit if back is pressed (not system UI)
    val requestNavigateBack = remember(uiState.isDirty) {{
        if(uiState.isDirty)
            showDiscardDialog = true
        else
            onNavigateBack()
    }}

    // Intercept system back when form is dirty (so we can prompt for discard confirmation)
    AddEditContainerBackHandling(
        isDirty = uiState.isDirty,
        showDiscardDialog = showDiscardDialog,
        onShowDiscardDialog = { showDiscardDialog = it },
        onDiscardConfirmed = onNavigateBack
    )

    // Gets an object that can launch the system image picker.
    val pickImageLauncher = rememberPickImageLauncher(onIntent)

    // Presentation of content
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Display container details
        item {
            AddEditContainerFormCard(uiState = uiState, onIntent = onIntent)
        }

        // Display container image
        item {
            AddEditContainerImageCard(
                imageUri = uiState.imageUri,
                onPickImage = {
                    pickImageLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onRemoveImage = { onIntent(AddEditContainerIntent.RemoveImageClicked) }
            )
        }

        // Display user actions
        item {
            AddEditContainerActionsCard(
                isSaving = uiState.isSaving,
                onSave = { onIntent(AddEditContainerIntent.SaveClicked) },
                onCancel = requestNavigateBack
            )
        }
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
 * Gets an object that can launch the system image picker. When it finishes, calls the lambda with
 * the result.
 */
@Composable
private fun rememberPickImageLauncher(
    onIntent: (AddEditContainerIntent) -> Unit
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
) { uri: Uri? ->
    if (uri != null)
        onIntent(AddEditContainerIntent.ImagePicked(uri.toString()))
}

/**
 * Displays the Container editable fields
 */
@Composable
private fun AddEditContainerFormCard(
    uiState: AddEditContainerUiState.Ready,
    onIntent: (AddEditContainerIntent) -> Unit
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (uiState.mode == AddEditContainerUiState.Ready.Mode.CREATE)
                    "Add Container"
                else
                    "Edit Container",
                style = MaterialTheme.typography.titleLarge
            )

            // Parent data
            AddEditContainerParentSection(
                canChangeParent = uiState.canChangeParent,
                parentContainerId = uiState.parentContainerId,
                parentContainerName = uiState.parentContainerName,
                availableParents = uiState.availableParents,
                onParentChanged = { onIntent(AddEditContainerIntent.ParentChanged(it)) }
            )

            HorizontalDivider()

            // Name field
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { onIntent(AddEditContainerIntent.NameChanged(it)) },
                label = { Text("Name") },
                isError = uiState.validation.nameError != null,
                supportingText = {
                    uiState.validation.nameError?.let { Text(it) }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description field
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { onIntent(AddEditContainerIntent.DescriptionChanged(it)) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
    }
}

/**
 * Displays data related to the parent container. Call parent picker for move actions.
 */
@Composable
private fun AddEditContainerParentSection(
    canChangeParent: Boolean,
    parentContainerId: ContainerId?,
    parentContainerName: String?,
    availableParents: List<AddEditContainerUiState.Ready.ParentOption>,
    onParentChanged: (ContainerId?) -> Unit
) {
    if (canChangeParent) {
        ParentPicker(
            selectedId = parentContainerId,
            options = availableParents,
            onSelected = onParentChanged
        )
    } else {
        Text(
            text = "Parent: ${parentContainerName ?: "Root"}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Either minimal picker or dropdown menu.
 */
@Composable
private fun ParentPicker(
    selectedId: ContainerId?,
    options: List<AddEditContainerUiState.Ready.ParentOption>,
    onSelected: (ContainerId?) -> Unit
) {

}

/**
 * Displays the Container's image
 */
@Composable
private fun AddEditContainerImageCard(
    imageUri: String?,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit
) {

}

/**
 * The action buttons for user intent
 */
@Composable
private fun AddEditContainerActionsCard(
    isSaving: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {

}