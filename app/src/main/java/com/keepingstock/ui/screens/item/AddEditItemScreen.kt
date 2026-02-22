package com.keepingstock.ui.screens.item

import android.R.attr.name
import android.R.attr.tag
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Tag
import com.keepingstock.core.contracts.TagId
import com.keepingstock.core.contracts.uistates.item.AddEditItemIntent
import com.keepingstock.core.contracts.uistates.item.AddEditItemUiState
import com.keepingstock.data.entities.ItemStatus
import com.keepingstock.ui.components.screen.ErrorContent
import com.keepingstock.ui.components.screen.LoadingContent
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

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
 *
 * TODO: This file contains many components/functions that are near identical to those found
 *  in the AddEditContainerScreen. Extract to shared file?
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

    // Presentation of content
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Container Details
        item {
            ItemFormCard(uiState = uiState, onIntent = onIntent)
        }

        // Container Image
        item {
            ItemImageCard(
                imageUri = uiState.imageUri,
                onPickImage = {
                    onIntent(AddEditItemIntent.PickImageClicked) // TODO: Add to AddEditContainerScreen - not necessary but good form
                    pickImageLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onRemoveImage = { onIntent(AddEditItemIntent.RemoveImageClicked) }
            )
        }

        // Tagging section
        item {
            TagEditorCard(
                uiState = uiState,
                onIntent = onIntent
            )
        }

        // Display user actions
        item {
            ActionsCard(
                isSaving = uiState.isSaving,
                onSave = { onIntent(AddEditItemIntent.SaveClicked) },
                onCancel = {
                    if (uiState.isDirty) showDiscardDialog = true else onNavigateBack
                }
            )
        }
    }
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

/**
 * Card section containing editable container fields (parent, name, description).
 *
 * - Displays CREATE vs EDIT title based on [uiState.mode].
 * - Shows parent selection UI when [uiState.canChangeParent] is true.
 * - Emits field-change intents as the user edits values.
 *
 * :param uiState: Current form values and validation state.
 * :param onIntent: Callback for emitting user intents (e.g., [AddEditItemIntent.NameChanged]).
 */
@Composable
private fun ItemFormCard(
    uiState: AddEditItemUiState.Ready,
    onIntent: (AddEditItemIntent) -> Unit
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Editable fields
            OutlinedTextField(
                value = uiState.name,
                onValueChange = {
                    onIntent(AddEditItemIntent.NameChanged(it))
                },
                label = { Text("Name") },
                isError = uiState.validation.nameError != null,
                supportingText = {
                    uiState.validation.nameError?.let { Text(it) }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = {
                    onIntent(AddEditItemIntent.DescriptionChanged(it))
                },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            HorizontalDivider()

            // Parent Container display/picker
            ParentSection(
                canChangeParent = uiState.canChangeParent,
                containerId = uiState.containerId,
                containerName = uiState.containerName,
                availableParents = uiState.availableParents,
                onParentChanged = {
                    onIntent(AddEditItemIntent.ContainerChanged(it))
                }
            )

            // Status toggle: disabled/hidden when container/Id == null
            if (uiState.containerId == null) {
                Text(
                    text = "Status: Taken Out (no container selected)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                uiState.validation.containerError?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                StatusToggle(
                    status = uiState.status,
                    checkoutDate = uiState.checkoutDate,
                    onChanged = { onIntent(AddEditItemIntent.StatusChanged(it)) }
                )
            }
        }
    }
}

/**
 * Displays parent container information and, when allowed, provides UI for changing it.
 *
 * :param canChangeParent: Whether the parent container can be changed in the current mode.
 * :param parentContainerId: Currently selected parent container id (null represents Root).
 * :param parentContainerName: Display name for the currently selected parent (nullable).
 * :param availableParents: Available parent options to choose from.
 * :param onParentChanged: Callback for the newly selected parent id (null = Root).
 */
@Composable
private fun ParentSection(
    canChangeParent: Boolean,
    containerId: ContainerId?,
    containerName: String?,
    availableParents: List<AddEditItemUiState.Ready.ParentOption>,
    onParentChanged: (ContainerId?) -> Unit
) {
    if (canChangeParent) {
        ParentPicker(
            selectedId = containerId,
            options = availableParents,
            onSelected = onParentChanged
        )
    } else {
        Text(
            text = "Parent: ${containerName ?: "Root"}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Demo parent selection UI.
 *
 * Current implementation:
 * - Displays the current parent name.
 * - Provides a "Change" button that cycles through [options] (no dropdown dependency).
 *
 * Future implementation:
 * - Replace cycling behavior with a dropdown or hierarchical picker when the repository-backed
 *   container tree is available.
 *
 * Assumption:
 * - [options] is non-empty.
 *
 * :param selectedId: Currently selected parent container id (null = Root).
 * :param options: List of selectable parent options.
 * :param onSelected: Callback invoked when the selection changes.
 */
@Composable
private fun ParentPicker(
    selectedId: ContainerId?,
    options: List<AddEditItemUiState.Ready.ParentOption>,
    onSelected: (ContainerId?) -> Unit
) {
    // TODO: Minimal picker: cycle via buttons (no ExposedDropdownMenu dependency).
    val current = options.firstOrNull { it.id == selectedId } ?: options.first()

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Container", style = MaterialTheme.typography.labelLarge)

        Row {
            Text(
                text = current.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(12.dp))

            OutlinedButton(
                onClick = {
                    val idx = options.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)
                    val next = options[(idx + 1) % options.size]
                    onSelected(next.id)
                }
            ) { Text("Change") }
        }

        Text(
            text = "Tap Change to cycle container (demo). Replace with dropdown later.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Item status toggle. On toggle, updates the item status between [ItemStatus.STORED] and
 * [ItemStatus.TAKEN_OUT].
 *
 * :param status: Current status of the item
 * :param onChanged: Invoked to toggle the current status of the item.
 */
@Composable
private fun StatusToggle(
    status: ItemStatus,
    checkoutDate: Date?,
    onChanged: (ItemStatus) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Status",
            style = MaterialTheme.typography.labelLarge
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Stored",
                modifier = Modifier
                    .padding(top = 12.dp)
                    .clickable { onChanged(ItemStatus.STORED) }
            )

            Switch(
                checked = status == ItemStatus.TAKEN_OUT,
                onCheckedChange = { checked ->
                    onChanged(if (checked) ItemStatus.TAKEN_OUT else ItemStatus.STORED)
                }
            )

            Text(
                text = "Taken Out",
                modifier = Modifier
                    .padding(top = 12.dp)
                    .clickable { onChanged(ItemStatus.TAKEN_OUT) }
            )
        }

        /*
        // TODO: Original alternate implemenation using radio buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row {
                RadioButton(
                    selected = status == ItemStatus.STORED,
                    onClick = { onChanged(ItemStatus.STORED) }
                )
                Text("Stored", modifier = Modifier.padding(top = 12.dp))
            }

            Row {
                RadioButton(
                    selected = status == ItemStatus.TAKEN_OUT,
                    onClick = { onChanged(ItemStatus.TAKEN_OUT) }
                )
                Text("Taken Out", modifier = Modifier.padding(top = 12.dp))
            }
        }

         */

        if (status == ItemStatus.TAKEN_OUT && checkoutDate != null) {
            Text(
                text = "Date checked out: " + DateTimeFormatter.ofPattern("MM-dd-yyyy")
                    .withZone(ZoneId.systemDefault()).format(checkoutDate.toInstant()),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Card section for viewing and editing the item image.
 *
 * - Displays a preview if [imageUri] is present, otherwise shows a placeholder message.
 * - Exposes actions for picking/changing and removing the image.
 *
 * :param imageUri: Current image URI string (nullable/blank indicates no image).
 * :param onPickImage: Invoked to launch the system image picker.
 * :param onRemoveImage: Invoked to remove the current image from the form state.
 */
@Composable
private fun ItemImageCard(
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

            // Get image preview or text indicated no image was selected
            ImagePreview(imageUri = imageUri)

            // Buttons for user action related to changing the picture
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onPickImage
                ) {
                    Text(
                        if (imageUri.isNullOrBlank()) "Pick image" else "Change image"
                    )
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
 * :param imageUri: Image URI string to display (nullable/blank indicates no image).
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
 * :param isSaving: Whether a save operation is in progress.
 * :param onSave: Callback invoked when the user taps Save.
 * :param onCancel: Callback invoked when the user taps Cancel.
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
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onSave,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isSaving) "Saving…" else "Save")
                }

                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

/**
 * Card section containing elements related to tagging item
 *
 * :param modifier:
 * :param uiState: Current form values and validation state.
 * :param onIntent: Contains callbacks for emitting user intents.
 */
@Composable
fun TagEditorCard(
    uiState: AddEditItemUiState.Ready,
    onIntent: (AddEditItemIntent) -> Unit
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Card title
                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.titleMedium
                )

                // Display selected / max
                Text(
                    text = "${uiState.selectedTags.size}/${uiState.maxTags}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Display current tags
            SelectedTagChips(
                tags = uiState.selectedTags,
                onRemove = { onIntent(AddEditItemIntent.RemoveTagClicked(it)) }
            )

            HorizontalDivider()

            // Display section for adding new tags (text field with suggested tags matching
            // current query)
            OutlinedTextField(
                value = uiState.tagQuery,
                onValueChange = { onIntent(AddEditItemIntent.QueryChanged(it)) },
                label = { Text("Add a tag") },
                supportingText = { uiState.inputError?.let { Text(it) } },
                isError = uiState.inputError != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Display suggested tags based on query
            TagSuggestionList(
                uiState = uiState,
                onAddQuery = { onIntent(AddEditItemIntent.AddQueryAsTagClicked) },
                onSelectExisting = { onIntent(AddEditItemIntent.ExistingTagSelected(it)) }
            )

            HorizontalDivider()

            // Display recommended tags
            RecommendedTagsRow(
                uiState = uiState,
                onSelect = { onIntent(AddEditItemIntent.RecommendedTagSelected(it)) },
                onRefresh = { onIntent(AddEditItemIntent.RefreshRecommendations) }
            )
        }
    }
}

/**
 * Chips displayed with FlowRow
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectedTagChips(
    modifier: Modifier = Modifier,
    tags: List<Tag>,
    onRemove: (TagId) -> Unit
) {
    if (tags.isEmpty()) {
        Text(
            text = "No tags selected.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            SelectedTagChip(
                name = tag.name,
                onRemove = { onRemove(tag.id) }
            )
        }
    }
}

/**
 * Individual chips for displaying a tag (took forever to get the presentation of this right).
 *
 * :param name: The name of the current tag
 * :param onRemove: The invoked intent  when the chip's close icon is clicked
 */
@Composable
private fun SelectedTagChip(
    modifier: Modifier = Modifier,
    name: String,
    onRemove: () -> Unit
) {
    // quick modifiers for easy adjustment
    val chipHeight = 32.dp
    val removeButtonSize = 22.dp
    val removeIconSize = 16.dp

    InputChip(
        modifier = modifier.height(chipHeight),
        selected = true,
        onClick = { /* TODO: Consult group on behavior, if any */ },
        label = {
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1
            )
        },
        trailingIcon = {
            // Used box instead of IconButton - spacing was weird
            Box(
                modifier = Modifier
                    .size(removeButtonSize)
                    .clip(CircleShape)
                    .clickable(onClick = onRemove), // TODO: snackbar "Tag removed" with "Undo" option
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove $name",
                    modifier = Modifier.size(removeIconSize)
                )
            }
        }
    )
}

/**
 * Displays the list of tags that appear as the user types a tag query.
 */
@Composable
private fun TagSuggestionList(
    uiState: AddEditItemUiState.Ready,
    onAddQuery: () -> Unit,
    onSelectExisting: (TagId) -> Unit
) {
    if (uiState.tagQuery.isBlank() && uiState.tagSuggestions.isEmpty()) return

    // TODO: improve presentation - hard to test without functions
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Add as-is affordance
        if (uiState.tagQuery.isNotBlank()) {
            OutlinedButton(
                onClick = onAddQuery,
                enabled = uiState.canAddMore && uiState.inputError == null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add \"${uiState.tagQuery}\"")
            }
        }

        // Existing suggestions
        uiState.tagSuggestions.take(uiState.suggestionsLimit).forEach { tag ->
            TextButton(
                onClick = { onSelectExisting(tag.id) },
                enabled = uiState.canAddMore,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(tag.name)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecommendedTagsRow(
    uiState: AddEditItemUiState.Ready,
    onSelect: (String) -> Unit,
    onRefresh: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Recommended", style = MaterialTheme.typography.labelLarge)
            TextButton(onClick = onRefresh, enabled = !uiState.isRecommending) {
                Text(if (uiState.isRecommending) "Refreshing…" else "Refresh")
            }
        }

        if (uiState.tagRecommendations.isEmpty()) {
            Text(
                text = "No recommendations yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chipHeight = 32.dp
            uiState.tagRecommendations.forEach { tagRec ->
                InputChip(
                    modifier = Modifier.height(chipHeight),
                    selected = true,
                    onClick = { onSelect(tagRec) },
                    label = {
                        Text(
                            text = tagRec,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    }
                )
            }
        }
    }
}