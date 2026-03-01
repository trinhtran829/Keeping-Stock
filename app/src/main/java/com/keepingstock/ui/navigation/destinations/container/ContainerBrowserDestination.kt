package com.keepingstock.ui.navigation.destinations.container

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.keepingstock.core.contracts.BrowserEmptyState
import com.keepingstock.core.contracts.BrowserLayout
import com.keepingstock.core.contracts.BrowserSort
import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerBrowserFilter
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.Routes
import com.keepingstock.core.contracts.intents.container.ContainerBrowserIntent
import com.keepingstock.core.contracts.uistates.container.ContainerBrowserUiState
import com.keepingstock.data.entities.ItemStatus
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.navigation.containerIdOrNull
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.container.ContainerBrowserScreen
import com.keepingstock.ui.components.navigation.ChipOption
import com.keepingstock.ui.components.navigation.DemoMode
import com.keepingstock.ui.components.navigation.DemoModeToggleRow
import com.keepingstock.ui.navigation.destinations.container.containerBrowserTopBarConfig
import com.keepingstock.ui.viewmodel.container.ContainerBrowserViewModel

/**
 * Registers the Container Browser destination in AppNavGraph.
 *
 * If no containerId arg is provided, root container is displayed, otherwise container's contents
 * are displayed
 *
 * Current temporary behavior:
 * - Uses a demo UiState generator (demoContainerBrowserReadyState) and a DemoMode toggle row.
 *   This allows the screen to be demonstrated without a ViewModel.
 *
 * :param deps: Shared navigation dependencies
 * :param lastContainerIdState: Mutable state used by app shell to remember the last visited
 *                          container.
 */
internal fun NavGraphBuilder.addContainerBrowserDestination(
    deps: NavDeps,
    lastContainerIdState: MutableState<ContainerId?>
) {
    // Register the ContainerBrowser destination: when route == "container_browser" with or
    // without the containerId, show ContainerBrowser of that container (or root)
    composable(
        route = NavRoute.ContainerBrowser.route,
        arguments = listOf(
            navArgument(Routes.Args.CONTAINER_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        // current container to display, where null = root container
        val containerId =
            backStackEntry.arguments?.containerIdOrNull(Routes.Args.CONTAINER_ID)

        val vm: ContainerBrowserViewModel = viewModel(
            factory = viewModelFactory {
                initializer {
                    ContainerBrowserViewModel(
                        containerId = containerId,
                        containerRepository = deps.containerRepo,
                        itemRepository = deps.itemRepo
                    )
                }
            }
        )

        val uiState by vm.uiState.collectAsStateWithLifecycle()

        // Build TopBarConfig from current UiState
        val topBarConfig = remember(uiState, containerId) {
            containerBrowserTopBarConfig(uiState = uiState, containerId = containerId)
        }

        // Push top bar updates to scaffold
        LaunchedEffect(topBarConfig) { deps.onTopBarChange(topBarConfig) }

        // Track the last visited container for "Return to Containers" behavior.
        lastContainerIdState.value = containerId

        /*
         * Kept in the destination (not the screen) so ContainerBrowserScreen stays production-like
         * and purely state-driven.
         */
        ContainerBrowserScreen(
            modifier = Modifier.fillMaxSize(),
            uiState = uiState,
            onIntent = vm::onIntent,
            onOpenSubcontainer = { subId ->
                deps.navController.navigate(NavRoute.ContainerBrowser.createRoute(subId))
            },
            onOpenItem = { itemId ->
                deps.navController.navigate(NavRoute.ItemDetails.createRoute(itemId))
            },
            onOpenContainerInfo = { id ->
                deps.navController.navigate(NavRoute.ContainerDetail.createRoute(id))
            },
            onAddContainer = { parentId ->
                deps.navController.navigate(
                    NavRoute.AddEditContainer.createRoute(parentContainerId = parentId)
                )
            },
            onAddItem = { cid ->
                deps.navController.navigate(NavRoute.AddEditItem.createRoute(containerId = cid))
            },
            onScan = {
                deps.navController.navigate(NavRoute.QRScan.route)
            }
        )
    }
}

/**
 * Builds the TopBarConfig for the Container Browser destination from UiState.
 *
 * Titles:
 * - Ready: uses the containerName from UiState
 * - Loading: "Loading…"
 * - Error: generic "Containers" // TODO: refine error title later
 *
 * Back button:
 * - Shown only when browsing a non-root container (containerId != null) // TODO: correct behavior?
 *
 * :param uiState: The current UI state for the Container Browser screen.
 * :return: TopBarConfig used by the app scaffold's top bar.
 */
private fun containerBrowserTopBarConfig(
    uiState: ContainerBrowserUiState,
    containerId: ContainerId?
): TopBarConfig {
    val title = when (uiState) {
        is ContainerBrowserUiState.Ready -> uiState.containerName
        is ContainerBrowserUiState.Loading -> "Loading…"
        is ContainerBrowserUiState.Error -> "Containers"
    }

    val showBack = (containerId != null)

    return TopBarConfig(title = title, showBack = showBack)
}