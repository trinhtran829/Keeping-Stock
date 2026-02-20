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

        // Demo form state (rememberSaveable so screen survives config changes while testing)
        // TODO: Demo currently, to be owned by ViewModel
        var uiState by rememberSaveable(containerId?.value, parentContainerId?.value) {
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

        // TODO: onSave function for demo purposes - handled by ViewModel
        fun onSave() {
            // Validate the uiState
            val validated = validate(uiState)
            uiState = validated

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

        // TODO: onSave action not implemented yet
        AddEditContainerScreen(
            containerId = containerId,
            parentContainerId = parentContainerId,
            onSave = { deps.navController.popBackStack() },
            onCancel = { deps.navController.popBackStack() }
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

private class AddEditContainerDemoController(
    private val deps: NavDeps,
    private val mode: AddEditContainerUiState.Ready.Mode,
    private val parentOptions: List<AddEditContainerUiState.Ready.ParentOption>,
    private val getUiState: () -> AddEditContainerUiState.Ready,
    private val setUiState: (AddEditContainerUiState.Ready) -> Unit
) {
    fun onSave() {

    }

    fun onIntent() {

    }
}

/**
 * Form validation for the current state.
 *
 * This might be able to be moved directly into the ViewModel later.
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