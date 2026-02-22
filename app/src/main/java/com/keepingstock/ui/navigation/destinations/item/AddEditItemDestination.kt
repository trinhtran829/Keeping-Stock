package com.keepingstock.ui.navigation.destinations.item

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.Routes
import com.keepingstock.core.contracts.Tag
import com.keepingstock.core.contracts.TagId
import com.keepingstock.core.contracts.uistates.container.AddEditContainerUiState
import com.keepingstock.core.contracts.uistates.item.AddEditItemIntent
import com.keepingstock.core.contracts.uistates.item.AddEditItemUiState
import com.keepingstock.data.entities.ItemStatus
import com.keepingstock.ui.components.navigation.DemoMode
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.navigation.containerIdOrNull
import com.keepingstock.ui.navigation.itemIdOrNull
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.item.AddEditItemScreen
import java.util.Date

internal fun NavGraphBuilder.addAddEditItemDestination(
    deps: NavDeps
) {
    composable(
        route = NavRoute.AddEditItem.route,
        arguments = listOf(
            navArgument(Routes.Args.ITEM_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(Routes.Args.CONTAINER_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val args = backStackEntry.arguments
        val itemId = args?.itemIdOrNull(Routes.Args.ITEM_ID)
        val containerId = args?.containerIdOrNull(Routes.Args.CONTAINER_ID)

        // TODO(REMOVE): Demo-only mode selector
        var demoMode by rememberSaveable(itemId?.value) {
            mutableStateOf(DemoMode.READY)
        }

        // TODO(REMOVE): Demo tags (replace with repo/VM)
        val knownTags = remember {
            listOf(
                Tag(TagId(1L), "Tools"),
                Tag(TagId(2L), "Electrical"),
                Tag(TagId(3L), "Plumbing"),
                Tag(TagId(4L), "Outdoor"),
                Tag(TagId(5L), "Spare"),
                Tag(TagId(6L), "Fragile"),
                Tag(TagId(7L), "Seasonal"),
                Tag(TagId(8L), "Automotive")
            )
        }

        // TODO(REMOVE): Demo containers list (replace with repo/VM)
        val parentOptions = remember {
            listOf(
                AddEditItemUiState.Ready.ParentOption(null, "Root"),
                AddEditItemUiState.Ready.ParentOption(ContainerId(1L), "Garage"),
                AddEditItemUiState.Ready.ParentOption(ContainerId(2L), "Kitchen"),
                AddEditItemUiState.Ready.ParentOption(ContainerId(3L), "Shed")
            )
        }

        var readyState by remember(itemId, containerId) {
            mutableStateOf(
                demoInitialUiState(
                    itemId = itemId,
                    containerId = containerId,
                    parentOptions = parentOptions,
                    knownTags = knownTags
                )
            )
        }

        // TODO: Demo mode only, to be covered by VM
        var uiState by remember(itemId, containerId, demoMode) {
            mutableStateOf(
                when (demoMode) {
                    DemoMode.LOADING -> AddEditItemUiState.Loading
                    DemoMode.ERROR, DemoMode.EMPTY ->
                        AddEditItemUiState.Error("Demo error loading item.")
                    DemoMode.READY, DemoMode.POPULATED -> readyState
                }
            )
        }

        LaunchedEffect(itemId, containerId) {
            deps.onTopBarChange(addEditItemTopBarConfig(uiState))
        }

        AddEditItemScreen(
            itemId = itemId,
            containerId = containerId,
            onSave = { deps.navController.popBackStack() },
            onCancel = { deps.navController.popBackStack() }
        )
    }
}

private fun addEditItemTopBarConfig(uiState: AddEditItemUiState): TopBarConfig {
    val title = when (uiState) {
        AddEditItemUiState.Loading -> "Loading"
        is AddEditItemUiState.Error -> "Item"
        is AddEditItemUiState.Ready ->
            if (uiState.mode == AddEditItemUiState.Ready.Mode.CREATE) "Add Item" else "Edit Item"
    }

    return TopBarConfig(title = title, showBack = true)
}

private class AddEditItemDemoController(
    private val deps: NavDeps,
    private val knownTags: List<Tag>,
    private val getUiState: () -> AddEditItemUiState,
    private val setUiState: (AddEditItemUiState) -> Unit
) {
    fun onIntent(intent: AddEditItemIntent) {
        val current = getUiState()

        when (intent) {
            // Navigation related
            AddEditItemIntent.SaveClicked -> onSave(current)
            AddEditItemIntent.BackClicked,
            AddEditItemIntent.DiscardChangesConfirmed -> deps.navController.popBackStack()

            // Screen handles, destination currently consumes only
            AddEditItemIntent.PickImageClicked,
            AddEditItemIntent.DismissDiscardDialog -> Unit

            // Set UI State based on intent - call reducer function
            else -> {
                if (current is AddEditItemUiState.Ready) {
                    val updated = reduceAddEditItemIntent(
                        current = current,
                        intent = intent,
                        knownTags = knownTags
                    )
                    setUiState(updated)
                }
            }
        }
    }

    fun onSave(current: AddEditItemUiState) {

    }
}

/**
 * Validates the Add/Edit Item form state and returns a copy containing validation errors.
 *
 * Current validation rules:
 * - Name is required (non-blank after trimming).
 *
 * :param currentState The current form state.
 * :return A copy of [currentState] with updated validation fields.
 *
 * TODO: This might be able to be moved directly into the ViewModel later.
 */
private fun validate(
    currentState: AddEditItemUiState.Ready
): AddEditItemUiState.Ready {
    // TODO: Name error validation here is just "is it blank". ViewModel might include if it's
    //  unique in the container? Update UiState model if additional validation is needed.
    val nameError = if(currentState.name.trim().isBlank()) "Name is required" else null

    val containerError =
        if ((currentState.containerId == null) && (currentState.status != ItemStatus.TAKEN_OUT))
            "Items outside a container must be marked Taken Out."
        else null

    return currentState.copy(
        validation = currentState.validation.copy(
            nameError = nameError,
            containerError = containerError
        )
    )
}

/**
 * TODO: for demo purposes only; could be moved into ViewModel later if matches intended structure
 */
private fun reduceAddEditItemIntent(
    current: AddEditItemUiState.Ready,
    intent: AddEditItemIntent,
    knownTags: List<Tag>
): AddEditItemUiState.Ready {
    // What to do for each emitted user action
    val updated = when (intent) {
        // Editable fields
        is AddEditItemIntent.NameChanged ->
            current.copy(name = intent.value, isDirty = true)

        is AddEditItemIntent.DescriptionChanges ->
            current.copy(description = intent.value, isDirty = true)

        is AddEditItemIntent.ImagePicked ->
            current.copy(imageUri = intent.uriString, isDirty = true)

        AddEditItemIntent.RemoveImageClicked ->
            current.copy(imageUri = null, isDirty = true)

        // Container related
        is AddEditItemIntent.ContainerChanged ->
            if (intent.containerId == null) {
                // if container is null, set checkout date and status as taken out
                current.copy(
                    containerId = null,
                    status = ItemStatus.TAKEN_OUT,
                    checkoutDate = current.checkoutDate ?: Date(),
                    isDirty = true
                )
            } else {
                // TODO: behavior check change status to stored if currently taken out?
                current.copy(
                    containerId = intent.containerId,
                    isDirty = true
                )
            }

        is AddEditItemIntent.StatusChanged ->
            if (current.containerId == null) {
                // Shouldn't happen, since no container = status toggle disabled
                current.copy(
                    status = ItemStatus.TAKEN_OUT,
                    checkoutDate = current.checkoutDate ?: Date(),
                )
            } else {
                when (intent.status) {
                    ItemStatus.STORED ->
                        current.copy(
                            status = ItemStatus.TAKEN_OUT,
                            checkoutDate = Date(),
                            isDirty = true
                        )
                    ItemStatus.TAKEN_OUT ->
                        current.copy(
                            status = ItemStatus.STORED,
                            checkoutDate = null,
                            isDirty = true
                        )
                }
            }

        // Tag Related
        AddEditItemIntent.AddQueryAsTagClicked -> TODO()
        AddEditItemIntent.ClearQuery -> TODO()
        is AddEditItemIntent.ExistingTagSelected -> TODO()
        is AddEditItemIntent.QueryChanged -> TODO()
        is AddEditItemIntent.RecommendedTagSelected -> TODO()
        AddEditItemIntent.RefreshRecommendations -> TODO()
        is AddEditItemIntent.RemoveTagClicked -> TODO()

        // Handled by UI/Controller
        AddEditItemIntent.DiscardChangesConfirmed,
        AddEditItemIntent.DismissDiscardDialog,
        AddEditItemIntent.PickImageClicked,
        AddEditItemIntent.BackClicked,
        AddEditItemIntent.SaveClicked -> current
    }

    return validate(updated)
}

/**
 * Builds a demo [AddEditItemUiState.Ready] for previews/manual testing.
 *
 * CREATE mode:
 * - Empty name/description.
 * - Optional [containerId] preselected when provided.
 *
 * EDIT mode:
 * - Uses placeholder values for name/description (replace with repository-loaded values later).
 *
 * :param mode: CREATE vs EDIT. // TODO: removed, can be derived from itemId presence
 * :param itemId: Item being edited (null for CREATE).
 * :param containerId: Optional initial parent selection.
 * :param parentOptions: Demo parent options list, used to resolve parent display name.
 * :param knownTags: Set of tags suitable for demo purposes
 *
 * :return A [AddEditItemUiState.Ready] suitable for demo rendering.
 *
 * TODO: For demo purposes only
 */
private fun demoInitialUiState(
    itemId: ItemId?,
    containerId: ContainerId?,
    parentOptions: List<AddEditItemUiState.Ready.ParentOption>,
    knownTags: List<Tag>
): AddEditItemUiState.Ready {
    val parentName = parentOptions.firstOrNull { it.id == containerId }?.name
    val now = Date()
    val mode =
        if (itemId == null) AddEditItemUiState.Ready.Mode.CREATE
        else AddEditItemUiState.Ready.Mode.EDIT

    val initialStatus =
        if (containerId == null) ItemStatus.TAKEN_OUT
        else ItemStatus.STORED

    val initialCheckout = if (containerId == null) now else null

    return validate(
        AddEditItemUiState.Ready(
            mode = mode,
            itemId = itemId,
            containerId = containerId,
            containerName = parentName,
            availableParents = parentOptions,

            name =
                if (mode == AddEditItemUiState.Ready.Mode.EDIT)
                    "Impact Driver"
                else "",
            description =
                if (mode == AddEditItemUiState.Ready.Mode.EDIT)
                    "18V brushless"
                else "",
            imageUri = null,
            status = initialStatus,
            createdDate = now,
            checkoutDate = initialCheckout,

            selectedTags =
                if (mode == AddEditItemUiState.Ready.Mode.EDIT)
                    listOf(knownTags[0], knownTags[1])
                else emptyList(),
            tagQuery = "",
            tagSuggestions = emptyList(),
            tagRecommendations = emptyList(),
            isRecommending = false,
            inputError = null,
            maxTags = 20,
            suggestionsLimit = 8,

            isSaving = false,
            isDirty = false,
            validation = AddEditItemUiState.Ready.Validation(),
            canChangeParent = true
        )
    )
}