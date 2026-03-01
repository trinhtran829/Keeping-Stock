package com.keepingstock.ui.navigation.destinations.container

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.keepingstock.core.contracts.Routes
import com.keepingstock.core.contracts.uistates.container.AddEditContainerUiState
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.navigation.containerIdOrNull
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.container.AddEditContainerScreen
import com.keepingstock.ui.viewmodel.container.AddEditContainerViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Registers the Add/Edit Container destination and wires demo state + navigation callbacks.
 *
 * Navigation args:
 * - [Routes.Args.CONTAINER_ID]: when present, screen is in EDIT mode; otherwise CREATE mode.
 * - [Routes.Args.PARENT_CONTAINER_ID]: optional initial parent for CREATE mode (or preselect in EDIT).
 *
 * @param deps: Navigation and UI dependencies (NavController, top bar updater, snackbar helper).
 */
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

        val vm: AddEditContainerViewModel = viewModel(
            viewModelStoreOwner = backStackEntry,
            factory = viewModelFactory {
                initializer {
                    AddEditContainerViewModel(
                        containerId = containerId,
                        initialParentContainerId = parentContainerId,
                        containerRepository = deps.containerRepo
                    )
                }
            }
        )

        val uiState by vm.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(vm) {
            vm.effects.collectLatest { effect ->
                when (effect) {
                    is AddEditContainerViewModel.UiEffect.ShowSnackbar ->
                        deps.showSnackbar(effect.message)

                    AddEditContainerViewModel.UiEffect.NavigateBack ->
                        deps.navController.popBackStack()
                }
            }
        }

        val topBarConfig = containerAddEditTopBarConfig(uiState)

        LaunchedEffect(topBarConfig.title, topBarConfig.showBack) {
            deps.onTopBarChange(topBarConfig)
        }

        AddEditContainerScreen(
            uiState = uiState,
            onIntent = vm::onIntent,
            onNavigateBack = { deps.navController.popBackStack() }
        )
    }
}

/**
 * Builds top bar title/back behavior from AddEditContainerUiState.
 *
 * @param uiState: The current UI state for the Add/Edit Container screen.
 * @return: A [TopBarConfig] describing the top app bar title and back button visibility.
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