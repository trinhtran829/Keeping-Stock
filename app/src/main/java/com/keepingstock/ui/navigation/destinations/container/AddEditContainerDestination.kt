package com.keepingstock.ui.navigation.destinations.container

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
import com.keepingstock.core.contracts.Routes
import com.keepingstock.core.contracts.uistates.container.AddEditContainerIntent
import com.keepingstock.core.contracts.uistates.container.AddEditContainerUiState
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.navigation.containerIdOrNull
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.container.AddEditContainerScreen

internal fun NavGraphBuilder.addAddEditContainerDestination(
    deps: NavDeps
) {
    composable(
        route = NavRoute.AddEditContainer.route,
        arguments = listOf(
            navArgument(Routes.Args.CONTAINER_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(Routes.Args.PARENT_CONTAINER_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val args = backStackEntry.arguments
        val containerId = args?.containerIdOrNull(Routes.Args.CONTAINER_ID)
        val parentContainerId = args?.containerIdOrNull(Routes.Args.PARENT_CONTAINER_ID)

        // TODO: ultimately handled by ViewModel
        val mode = if (containerId == null)
            AddEditContainerUiState.Ready.Mode.CREATE
        else
            AddEditContainerUiState.Ready.Mode.EDIT

        // Demo parent options (replace by ViewModel/Repository later) -
        // TODO: later handled by repo lookup in ViewModel
        // TODO: Update for hierarchical browsing of available move flow containers?
        val parentOptions = remember {
            listOf(
                AddEditContainerUiState.Ready.ParentOption(null, "Root"),
                AddEditContainerUiState.Ready.ParentOption(ContainerId(1L), "Garage"),
                AddEditContainerUiState.Ready.ParentOption(ContainerId(2L), "Kitchen"),
                AddEditContainerUiState.Ready.ParentOption(ContainerId(3L), "Shed")
            )
        }

        // TODO: Demo currently, to be owned by ViewModel
        var uiState by remember(containerId, parentContainerId) {
            mutableStateOf(
                demoInitialUiState(
                    mode = mode,
                    containerId = containerId,
                    parentContainerId = parentContainerId,
                    parentOptions = parentOptions
                )
            )
        }

        val topBarConfig = remember(uiState) { containerAddEditTopBarConfig(uiState) }
        LaunchedEffect(uiState) {
            deps.onTopBarChange(topBarConfig)
        }

        // TODO: Demo only: Controller will be replaced by ViewModel
        val controller = remember(deps, mode, parentOptions) {
            AddEditContainerDemoController(
                deps = deps,
                mode = mode,
                parentOptions = parentOptions,
                getUiState = { uiState },
                setUiState = { uiState = it }
            )
        }

        // TODO: onSave action not implemented yet
        AddEditContainerScreen(
            uiState = uiState,
            onIntent = controller::onIntent,
            onNavigateBack = { deps.navController.popBackStack() }
        )
    }
}

/**
 * Builds top bar title/back behavior from ContainerDetailUiState.
 */
private fun containerAddEditTopBarConfig(uiState: AddEditContainerUiState): TopBarConfig {
    val title = when (uiState) {
        is AddEditContainerUiState.Loading -> "Loading…"
        is AddEditContainerUiState.Error -> "Container" // TODO: What should error title be?
        is AddEditContainerUiState.Ready ->  {
            if (uiState.mode == AddEditContainerUiState.Ready.Mode.CREATE)
                "Add Container"
            else
                "Edit Container"
        }
    }
    return TopBarConfig(title = title, showBack = true)
}

/**
 * A simple controller object for user intent + side effects
 *
 * TODO: For demo purposes only; replace with ViewModel functions
 */
private class AddEditContainerDemoController(
    private val deps: NavDeps,
    private val mode: AddEditContainerUiState.Ready.Mode,
    private val parentOptions: List<AddEditContainerUiState.Ready.ParentOption>,
    private val getUiState: () -> AddEditContainerUiState.Ready,
    private val setUiState: (AddEditContainerUiState.Ready) -> Unit
) {
    // TODO: onSave function for demo purposes - handled by ViewModel
    fun onSave() {
        val current = getUiState()

        // Validate the uiState
        val validated = validate(current)
        setUiState(validated)

        // If UiState is valid (right now, means nameError == null; if more validation is used,
        // update UiState model
        if (validated.validation.nameError == null) {
            // Show success message
            deps.showSnackbar(
                if (mode == AddEditContainerUiState.Ready.Mode.CREATE)
                    "Container created"
                else
                    "Container updated"
            )

            // Editing is finished, navigate to previous screen
            deps.navController.popBackStack()
        }
    }

    // TODO: onIntent functions for demo purposes - handled by ViewModel
    fun onIntent(intent: AddEditContainerIntent) {
        when (intent) {
            AddEditContainerIntent.SaveClicked -> onSave()

            AddEditContainerIntent.BackClicked,
            AddEditContainerIntent.DiscardChangesConfirmed -> deps.navController.popBackStack()

            // Screen handles launching the picker; destination only consumes the result.
            AddEditContainerIntent.PickImageClicked,
            AddEditContainerIntent.DismissDiscardDialog -> Unit

            else -> setUiState(reduceIntent(getUiState(), intent, parentOptions))
        }
    }
}

/**
 * Form validation for the current state.
 *
 * TODO: This might be able to be moved directly into the ViewModel later.
 */
private fun validate(
    currentState: AddEditContainerUiState.Ready
): AddEditContainerUiState.Ready {
    // TODO: Name error validation here is just "is it blank". ViewModel might include if it's
    //  unique in the container? Update UiState model if additional validation is needed.
    val nameError = if (currentState.name.trim().isBlank()) "Name is required." else null

    // Copy current state, but update the nameError message (
    return currentState.copy(
        validation = currentState.validation.copy(nameError = nameError)
    )
}

/**
 * State transition: applies an intent to the current uistate and returns the next uistate.
 *
 * TODO: for demo purposes only; could be moved into ViewModel later if matches intended structure
 */
private fun reduceIntent(
    current: AddEditContainerUiState.Ready,
    intent: AddEditContainerIntent,
    parentOptions: List<AddEditContainerUiState.Ready.ParentOption>
): AddEditContainerUiState.Ready {
    val updated = when (intent) {
        // If user edits name
        is AddEditContainerIntent.NameChanged ->
            current.copy(name = intent.value, isDirty = true)

        // If user edits description
        is AddEditContainerIntent.DescriptionChanged ->
            current.copy(description = intent.value, isDirty = true)

        // If user moved parent container
        is AddEditContainerIntent.ParentChanged -> {
            if (!current.canChangeParent)
                current
            else {
                // Get parent name, update current state's parent id and name
                val parentName = parentOptions.firstOrNull { it.id == intent.parentId }?.name
                current.copy(
                    parentContainerId = intent.parentId,
                    parentContainerName = parentName,
                    isDirty = true
                )
            }
        }

        // If user selected a new image
        is AddEditContainerIntent.ImagePicked ->
            current.copy(imageUri = intent.uriString, isDirty = true)

        // If user removed the current image
        AddEditContainerIntent.RemoveImageClicked ->
            current.copy(imageUri = null, isDirty = true)

        // These intents are handled as side-effects in the destination.
        // when requires them to be accounted for.
        AddEditContainerIntent.SaveClicked,
        AddEditContainerIntent.BackClicked,
        AddEditContainerIntent.PickImageClicked,
        AddEditContainerIntent.DiscardChangesConfirmed,
        AddEditContainerIntent.DismissDiscardDialog -> current
    }

    return validate(updated)
}


private fun demoInitialUiState(
    mode: AddEditContainerUiState.Ready.Mode,
    containerId: ContainerId?,
    parentContainerId: ContainerId?,
    parentOptions: List<AddEditContainerUiState.Ready.ParentOption>
): AddEditContainerUiState.Ready {
    val parentName = parentOptions.firstOrNull { it.id == parentContainerId }?.name

    return AddEditContainerUiState.Ready(
        mode = mode,
        containerId = containerId,
        parentContainerId = parentContainerId,
        parentContainerName = parentName,
        availableParents = parentOptions,
        name = if (mode == AddEditContainerUiState.Ready.Mode.EDIT) "Garage" else "",
        description = if (mode == AddEditContainerUiState.Ready.Mode.EDIT) "Tools and hardware" else "",
        imageUri = null,
        isSaving = false,
        isDirty = false,
        canChangeParent = true
    )
}