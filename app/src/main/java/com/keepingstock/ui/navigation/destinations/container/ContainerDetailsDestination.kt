package com.keepingstock.ui.navigation.destinations.container

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
import com.keepingstock.core.contracts.Routes
import com.keepingstock.core.contracts.uistates.container.ContainerDetailUiState
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.navigation.containerIdOrNull
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.container.ContainerDetailScreen
import com.keepingstock.ui.viewmodel.container.ContainerDetailViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Registers the Container Details Screen destination in the AppNavGraph.
 *
 * Displays the details of the Container indicated in the path param argument containerId
 * in the route.
 *
 * TODO: Still need to implement MOVE flow in Intent file
 *
 * @param deps: Shared navigation dependencies.
 */
internal fun NavGraphBuilder.addContainerDetailsDestination(
    deps: NavDeps
) {
    // Register the ContainerDetails destination: when route == "container_details" with
    // containerId, show ContainerDetailScreen
    composable(
        route = NavRoute.ContainerDetail.route,
        arguments = listOf(
            navArgument(Routes.Args.CONTAINER_ID) { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val containerId =
            backStackEntry.arguments?.containerIdOrNull(Routes.Args.CONTAINER_ID)
                ?: error("Missing containerId")

        val vm: ContainerDetailViewModel = viewModel(
            viewModelStoreOwner = backStackEntry,
            factory = viewModelFactory {
                initializer {
                    ContainerDetailViewModel(
                        containerId = containerId,
                        containerRepository = deps.containerRepo,
                        itemRepository = deps.itemRepo
                    )
                }
            }
        )

        val uiState by vm.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(vm) {
            vm.effects.collectLatest { effect ->
                when (effect) {
                    is ContainerDetailViewModel.UiEffect.ShowSnackbar ->
                        deps.showSnackbar(effect.message)

                    ContainerDetailViewModel.UiEffect.NavigateBack ->
                        deps.navController.popBackStack()
                }
            }
        }

        val topBarConfig = containerDetailTopBarConfig(uiState)

        LaunchedEffect(topBarConfig.title, topBarConfig.showBack) {
            deps.onTopBarChange(topBarConfig)
        }

        ContainerDetailScreen(
            modifier = Modifier.fillMaxSize(),
            uiState = uiState,
            onIntent = vm::onIntent,
            onBack = { deps.navController.popBackStack() },
            onEdit = { id ->
                deps.navController.navigate(
                    NavRoute.AddEditContainer.createRoute(containerId = id)
                )
            },
            onMove = { /* TODO: hook up when Move flow exists */ }
        )
    }
}

/**
 * Builds top bar title/back behavior from ContainerDetailUiState.
 */
private fun containerDetailTopBarConfig(uiState: ContainerDetailUiState): TopBarConfig {
    val title = when (uiState) {
        is ContainerDetailUiState.Ready ->  "Container Details"//uiState.containerName + " Details"
        is ContainerDetailUiState.Loading -> "Loading…"
        is ContainerDetailUiState.Error -> "Container details"
    }
    return TopBarConfig(title = title, showBack = true)
}