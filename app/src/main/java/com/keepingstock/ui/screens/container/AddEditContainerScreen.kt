package com.keepingstock.ui.screens.container

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.intents.container.AddEditContainerIntent
import com.keepingstock.core.contracts.uistates.container.AddEditContainerUiState
import com.keepingstock.platform.storage.copyImageToAppStorage
import com.keepingstock.ui.components.screen.ErrorContent
import com.keepingstock.ui.components.screen.LoadingContent

/**
 * Add/Edit Container screen that renders based on uiState.
 *
 * State handling:
 * - [AddEditContainerUiState.Loading] shows a loading indicator.
 * - [AddEditContainerUiState.Error] shows an error message.
 * - [AddEditContainerUiState.Ready] shows the editable form and emits [AddEditContainerIntent]
 *   events via [onIntent].
 *
 * Navigation:
 * - [onNavigateBack] is called when the user confirms leaving (e.g. discard changes) or taps
 *   Cancel when the form is not dirty.
 *
 * TODO: Extract back handling to VM/effects
 *
 * @param modifier: Modifier applied to the screen container.
 * @param uiState: Current UI state for the Add/Edit Container flow.
 * @param onIntent: Callback for user intents (field edits, save, image changes, etc.).
 * @param onNavigateBack: Callback to navigate up/back out of this screen.
 * @param onMove: Navigate to Select Container Screen
 */
@Composable
fun AddEditContainerScreen(
    modifier: Modifier = Modifier,
    uiState: AddEditContainerUiState,
    onIntent: (AddEditContainerIntent) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onMove: () -> Unit = {}
) {
    Column(modifier = modifier.padding(16.dp)) {
        when (uiState) {
            AddEditContainerUiState.Loading ->
                LoadingContent(modifier.fillMaxSize())

            is AddEditContainerUiState.Error ->
                ErrorContent(modifier = modifier.fillMaxSize(), message = uiState.message)

            is AddEditContainerUiState.Ready -> {
                ReadyContent(
                    modifier = modifier.fillMaxSize(),
                    uiState = uiState,
                    onIntent = onIntent,
                    onNavigateBack = onNavigateBack,
                    onMove = onMove
                )
            }
        }
    }
}

/**
 * Renders the editable Add/Edit Container form for the [AddEditContainerUiState.Ready] state.
 *
 * UI responsibilities:
 * - Owns local, UI-only discard confirmation dialog state (not part of UiState).
 * - Intercepts system back when [uiState.isDirty] and prompts for discard confirmation.
 * - Hosts an Activity Result launcher to pick an image and emits [AddEditContainerIntent.ImagePicked].
 *
 * @param modifier: Modifier applied to the scrolling content container.
 * @param uiState: Ready state containing current field values, validation, and flags.
 * @param onIntent: Callback for emitting user intents to the state owner (demo controller / ViewModel).
 * @param onNavigateBack: Callback invoked when navigation away from the screen is confirmed.
 * @param onMove: Navigate to Select Container Screen
 */
@Composable
private fun ReadyContent(
    modifier: Modifier,
    uiState: AddEditContainerUiState.Ready,
    onIntent: (AddEditContainerIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onMove: () -> Unit
) {
    // TODO: Local UI-only dialog state (kept out of UiState to keep demo simple).
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }

    // What actions to emit if back is pressed (not system UI)
    val requestNavigateBack = {
        if (uiState.isDirty) showDiscardDialog = true else onNavigateBack()
    }

    // Intercept system back when form is dirty (so we can prompt for discard confirmation)
    BackHandling(
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
            FormCard(
                uiState = uiState,
                onIntent = onIntent,
                onMove = onMove
            )
        }

        // Display container image
        item {
            ImageCard(
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
            ActionsCard(
                isSaving = uiState.isSaving,
                onSave = { onIntent(AddEditContainerIntent.SaveClicked) },
                onCancel = requestNavigateBack
            )
        }
    }
}

/**
 * Handles back navigation behavior for the Add/Edit Container form.
 *
 * Behavior:
 * - When [isDirty] is true, intercepts the system back button and shows a discard confirmation dialog.
 * - When [showDiscardDialog] is true, displays an [AlertDialog] allowing the user to discard changes
 *   or cancel and stay on the screen.
 *
 * @param isDirty: Whether the form has unsaved changes.
 * @param showDiscardDialog: Controls whether the discard confirmation dialog is visible.
 * @param onShowDiscardDialog: Setter for [showDiscardDialog].
 * @param onDiscardConfirmed: Callback invoked when the user confirms discarding changes.
 */
@Composable
private fun BackHandling(
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
 * Creates and remembers an Activity Result launcher for selecting an image from the system picker.
 *
 * When the picker returns a non-null [Uri], this copies the selected image into app-private
 * storage and emits [AddEditContainerIntent.ImagePicked] with the copied file URI.
 *
 * @param onIntent: Callback used to emit [AddEditContainerIntent] events.
 * @return A launcher that can start the visual media picker and deliver a picked image [Uri].
 */
@Composable
private fun rememberPickImageLauncher(
    onIntent: (AddEditContainerIntent) -> Unit
) = run {
    val context = LocalContext.current

    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val storedUri = copyImageToAppStorage(context, uri)
                onIntent(AddEditContainerIntent.ImagePicked(storedUri.toString()))
            } catch (_: Exception) {
                // TODO: emit a snackbar/event.
            }
        }
    }
}

/**
 * Card section containing editable container fields (parent, name, description).
 *
 * - Displays CREATE vs EDIT title based on [uiState.mode].
 * - Shows parent selection UI when [uiState.canChangeParent] is true.
 * - Emits field-change intents as the user edits values.
 *
 * @param uiState: Current form values and validation state.
 * @param onIntent: Callback for emitting user intents (e.g., [AddEditContainerIntent.NameChanged]).
 * @param onMove: Navigate to Select Container Screen
 */
@Composable
private fun FormCard(
    uiState: AddEditContainerUiState.Ready,
    onIntent: (AddEditContainerIntent) -> Unit,
    onMove: () -> Unit
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
            ParentSection(
                canChangeParent = uiState.canChangeParent,
                parentContainerId = uiState.parentContainerId,
                parentContainerName = uiState.parentContainerName,
                onMove = onMove
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
                value = uiState.description ?: "",
                onValueChange = { onIntent(AddEditContainerIntent.DescriptionChanged(it)) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
    }
}

/**
 * Displays parent container information and, when allowed, provides UI for changing it.
 *
 * @param canChangeParent: Whether the parent container can be changed in the current mode.
 * @param parentContainerId: Currently selected parent container id (null represents Root).
 * @param parentContainerName: Display name for the currently selected parent (nullable).
 * @param onMove: Navigate to Select Container Screen
 */
@Composable
private fun ParentSection(
    canChangeParent: Boolean,
    parentContainerId: ContainerId?,
    parentContainerName: String?,
    onMove: () -> Unit
) {
    if (canChangeParent) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Parent", style = MaterialTheme.typography.labelLarge)

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(
                    text = parentContainerName ?: "Root",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(12.dp))

                OutlinedButton(
                    onClick = onMove
                ) {
                    Text("Change")
                }
            }

            Text(
                text = "Open container picker to choose a parent.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Text(
            text = "Parent: ${parentContainerName ?: "Root"}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Card section for viewing and editing the container image.
 *
 * - Displays a preview if [imageUri] is present, otherwise shows a placeholder message.
 * - Exposes actions for picking/changing and removing the image.
 *
 * @param imageUri Current image URI string (nullable/blank indicates no image).
 * @param onPickImage Invoked to launch the system image picker.
 * @param onRemoveImage Invoked to remove the current image from the form state.
 */
@Composable
private fun ImageCard(
    imageUri: String?,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Field title
            Text("Image", style = MaterialTheme.typography.titleMedium)

            // Get image preview (or text indicating no image was selected
            ImagePreview(imageUri = imageUri)

            // Buttons for user action related to changing the picture
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onPickImage
                ) {
                    Text(if (imageUri.isNullOrBlank()) "Pick image" else "Change image")
                }

                OutlinedButton(
                    onClick = onRemoveImage,
                    enabled = !imageUri.isNullOrBlank()
                ) {
                    Text("Remove")
                }
            }
        }
    }
}

/**
 * Displays an image preview for the provided [imageUri], or a placeholder message when absent.
 *
 * @param imageUri Image URI string to display (nullable/blank indicates no image).
 */
@Composable
private fun ImagePreview(
    imageUri: String?
) {
    // If URI is not available, show text
    if (imageUri.isNullOrBlank()) {
        Text(
            text = "No image selected.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    // Show image
    AsyncImage(
        model = Uri.parse(imageUri),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop
    )
}

/**
 * Card section containing primary form actions (Save/Cancel).
 *
 * - Save is disabled while [isSaving] is true.
 * - Cancel delegates to [onCancel], which may prompt for discard confirmation when dirty.
 *
 * @param isSaving Whether a save operation is in progress.
 * @param onSave Callback invoked when the user taps Save.
 * @param onCancel Callback invoked when the user taps Cancel.
 */
@Composable
private fun ActionsCard(
    isSaving: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onSave,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f)
                ) { Text(if (isSaving) "Saving…" else "Save") }

                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) { Text("Cancel") }
            }
        }
    }
}