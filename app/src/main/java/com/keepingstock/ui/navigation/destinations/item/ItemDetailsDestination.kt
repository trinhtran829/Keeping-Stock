package com.keepingstock.ui.navigation.destinations.item

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Routes
import com.keepingstock.core.contracts.uistates.item.ItemDetailUiState
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavResultKeys
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.navigation.itemIdOrNull
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.item.ItemDetailsScreen
import com.keepingstock.ui.viewmodel.item.ItemDetailViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Registers the Item Details Screen destination in the AppNavGraph.
 *
 * Displays the details of the Item indicated in the path param argument itemId
 * in the route.
 *
 * TODO: Still need to implement MOVE flow in Intent file
 *
 * @param deps: Shared navigation dependencies.
 */
internal fun NavGraphBuilder.addItemDetailsDestination(
    deps: NavDeps
) {
    // Register the ItemDetails destination: when route == "item_details" with itemId,
    // show ItemDetailsScreen
    composable(
        route = NavRoute.ItemDetails.route,
        arguments = listOf(
            navArgument(Routes.Args.ITEM_ID) { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val itemId =
            backStackEntry.arguments?.itemIdOrNull(Routes.Args.ITEM_ID)
            ?: error("Missing itemId")

        val vm: ItemDetailViewModel = viewModel(
            viewModelStoreOwner = backStackEntry,
            factory = viewModelFactory {
                initializer {
                    ItemDetailViewModel(
                        itemId = itemId,
                        itemRepository = deps.itemRepo,
                        containerRepository = deps.containerRepo
                    )
                }
            },
        )

        val uiState by vm.uiState.collectAsStateWithLifecycle()

        val selectedContainerIdFlow =
            backStackEntry.savedStateHandle.getStateFlow<Long?>(
                NavResultKeys.SELECTED_CONTAINER_ID,
                null
            )

        LaunchedEffect(backStackEntry) {
            selectedContainerIdFlow.collectLatest { selectedContainerIdValue ->
                val hasResult =
                    backStackEntry.savedStateHandle.contains(NavResultKeys.SELECTED_CONTAINER_ID)

                if (!hasResult) return@collectLatest

                val newContainerId = selectedContainerIdValue?.let { ContainerId(it) }

                vm.onMoveContainerSelected(newContainerId)

                backStackEntry.savedStateHandle.remove<Long?>(NavResultKeys.SELECTED_CONTAINER_ID)
            }
        }

        LaunchedEffect(vm) {
            vm.effects.collectLatest { effect ->
                when (effect) {
                    is ItemDetailViewModel.UiEffect.ShowSnackbar ->
                        deps.showSnackbar(effect.message)

                    ItemDetailViewModel.UiEffect.NavigateBack ->
                        deps.navController.popBackStack()
                }
            }
        }

        val topBarConfig = itemDetailTopBarConfig(uiState)

        LaunchedEffect(topBarConfig.title, topBarConfig.showBack) {
            deps.onTopBarChange(topBarConfig)
        }

        ItemDetailsScreen(
            modifier = Modifier.fillMaxSize(),
            uiState = uiState,
            onIntent = vm::onIntent,
            onBack = { deps.navController.popBackStack() },
            onEdit = { id ->
                deps.navController.navigate(NavRoute.AddEditItem.createRoute(itemId = id))
            },
            onMove = { id ->
                deps.navController.navigate(
                    NavRoute.SelectContainer.createRoute(
                        subjectType = Routes.SubjectType.Item,
                        subjectId = id.value,
                        currentContainerId = (uiState as? ItemDetailUiState.Ready)
                            ?.item
                            ?.containerId
                    )
                )
            }
        )
    }
}

/**
 * Builds top bar title/back behavior from ItemDetailUiState.
 */
private fun itemDetailTopBarConfig(uiState: ItemDetailUiState): TopBarConfig {
    val title = when (uiState) {
        is ItemDetailUiState.Ready ->  "${uiState.item.name} Details"
        is ItemDetailUiState.Loading -> "Loading…"
        is ItemDetailUiState.Error -> "Item Details"
    }
    return TopBarConfig(title = title, showBack = true)
}