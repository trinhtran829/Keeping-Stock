package com.keepingstock.ui.navigation.destinations.item

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Routes
import com.keepingstock.core.contracts.UiState
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.item.ItemBrowserScreen
import com.keepingstock.viewmodel.item.ItemBrowserUiData

/**
 * Registers the Item Browser destination in AppNavGraph.
 *
 * Acts as a global Item viewer.
 *
 * Current temporary behavior:
 * - Uses a demo UiState generator (demoContainerBrowserReadyState) and a DemoMode toggle row.
 *   This allows the screen to be demonstrated without a ViewModel.
 *
 * :param deps: Shared navigation dependencies
 * :param lastContainerIdState: Mutable state used by app shell to remember the last visited
 *                          container.
 *
 * TODO: Replace demoMode + demo UiState with a real
 *  ViewModel: val uiState by viewModel.uiState.collectAsStateWithLifecycle()
 */
internal fun NavGraphBuilder.addItemBrowserDestination(
    deps: NavDeps,
    lastContainerId: () -> ContainerId?
) {
    // Register the ItemBrowser destination: when route == "item_browser", show ItemBrowserScreen
    composable(
        route = NavRoute.ItemBrowser.route
    ) {
        val uiState = remember() {
            UiState.Error("Item Browser temporarily disabled")
        }

        // Build TopBarConfig from current UiState
        val topBarConfig = remember(uiState) { itemBrowserTopBarConfig(uiState) }

        // Push top bar updates to scaffold
        LaunchedEffect(topBarConfig) {
            deps.onTopBarChange(topBarConfig)
        }

        ItemBrowserScreen(
            modifier = Modifier.fillMaxSize(),
            uiState = uiState,
            onOpenItem = { itemId ->
                deps.navController.navigate(NavRoute.ItemDetails.createRoute(itemId))
            },
            onOpenContainerBrowser = {
                deps.navController.navigate(
                    NavRoute.ContainerBrowser.createRoute(lastContainerId())
                ) {
                    popUpTo(Routes.CONTAINER_BROWSER) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}

/**
 * Builds the TopBarConfig for the Item Browser destination from UiState.
 *
 * Titles:
 * - Ready/Error: "All Items" // TODO: different title for error state?
 * - Loading: "Loading…"
 *
 * :param uiState: The current UI state for the Item Browser screen.
 * :return: TopBarConfig used by the app scaffold's top bar.
 */
private fun itemBrowserTopBarConfig(uiState: UiState<ItemBrowserUiData>): TopBarConfig {
    val title = when (uiState) {
        is UiState.Success -> "All Items"
        is UiState.Loading -> "Loading…"
        is UiState.Error -> "All Items"
    }
    val showBack = false

    return TopBarConfig(title = title, showBack = showBack)
}