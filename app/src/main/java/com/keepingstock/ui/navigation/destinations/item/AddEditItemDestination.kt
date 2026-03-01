package com.keepingstock.ui.navigation.destinations.item

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
import com.keepingstock.core.contracts.uistates.item.AddEditItemUiState
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.navigation.containerIdOrNull
import com.keepingstock.ui.navigation.itemIdOrNull
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.item.AddEditItemScreen
import com.keepingstock.ui.viewmodel.item.AddEditItemViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Registers the Add/Edit Item destination and wires demo navigation arguments, demo state,
 * and screen callbacks.
 *
 * Navigation args:
 * - [Routes.Args.ITEM_ID]: when present, screen is in EDIT mode; otherwise CREATE mode.
 * - [Routes.Args.CONTAINER_ID]: optional initial parent for CREATE mode (or preselect in EDIT).
 *
 * @param deps: Navigation and UI dependencies (NavController, top bar updater, snackbar helper).
 */
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

        val vm: AddEditItemViewModel = viewModel(
            viewModelStoreOwner = backStackEntry,
            factory = viewModelFactory {
                initializer {
                    AddEditItemViewModel(
                        itemId = itemId,
                        initialContainerId = containerId,
                        itemRepository = deps.itemRepo,
                        containerRepository = deps.containerRepo,
                        tagRepository = deps.tagRepo
                    )
                }
            }
        )

        val uiState by vm.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(vm) {
            vm.effects.collectLatest { effect ->
                when (effect) {
                    is AddEditItemViewModel.UiEffect.ShowSnackbar ->
                        deps.showSnackbar(effect.message)

                    AddEditItemViewModel.UiEffect.NavigateBack ->
                        deps.navController.popBackStack()
                }
            }
        }

        val topBarConfig = addEditItemTopBarConfig(uiState)

        /**
         * Updates the global top bar whenever the destination arguments change.
         */
        LaunchedEffect(topBarConfig.title, topBarConfig.showBack) {
            deps.onTopBarChange(topBarConfig)
        }

        AddEditItemScreen(
            uiState = uiState,
            onIntent = vm::onIntent,
            onNavigateBack = { deps.navController.popBackStack() }
        )
    }
}

/**
 * Builds top bar configuration for the AddEdit Item Screen.
 *
 * @param uiState: The current UI state for the Add/Edit Item screen.
 * @return: A [TopBarConfig] with title and back button visibility.
 */
private fun addEditItemTopBarConfig(uiState: AddEditItemUiState): TopBarConfig {
    val title = when (uiState) {
        AddEditItemUiState.Loading -> "Loading"
        is AddEditItemUiState.Error -> "Item"
        is AddEditItemUiState.Ready ->
            if (uiState.mode == AddEditItemUiState.Ready.Mode.CREATE) "Add Item" else "Edit Item"
    }

    return TopBarConfig(title = title, showBack = true)
}