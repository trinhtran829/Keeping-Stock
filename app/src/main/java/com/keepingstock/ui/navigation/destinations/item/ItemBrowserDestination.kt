package com.keepingstock.ui.navigation.destinations.item

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.uistates.item.ItemBrowserUiState
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.item.ItemBrowserScreen
import com.keepingstock.ui.viewmodel.item.ItemBrowserViewModel

/**
 * Registers the Item Browser destination in AppNavGraph.
 *
 * Acts as a global Item viewer.
 *
 * Current temporary behavior:
 * - Uses a demo UiState generator (demoContainerBrowserReadyState) and a DemoMode toggle row.
 *   This allows the screen to be demonstrated without a ViewModel.
 *
 * @param deps: Shared navigation dependencies
 * @param lastContainerIdState: Mutable state used by app shell to remember the last visited
 *                          container.
 */
internal fun NavGraphBuilder.addItemBrowserDestination(
    deps: NavDeps
) {
    // Register the ItemBrowser destination: when route == "item_browser", show ItemBrowserScreen
    composable(
        route = NavRoute.ItemBrowser.route
    ) {
        val vm: ItemBrowserViewModel = viewModel(
            factory = viewModelFactory {
                initializer {
                    ItemBrowserViewModel(
                        itemRepository = deps.itemRepo
                    )
                }
            }
        )

        val uiState by vm.uiState.collectAsStateWithLifecycle()

        // Build TopBarConfig from current UiState
        val topBarConfig = remember(uiState) { itemBrowserTopBarConfig(uiState) }

        // Push top bar updates to scaffold
        LaunchedEffect(topBarConfig) { deps.onTopBarChange(topBarConfig) }

        /*
         * Kept in the destination (not the screen) so ContainerBrowserScreen stays production-like
         * and purely state-driven.
         */
        ItemBrowserScreen(
            modifier = Modifier.fillMaxSize(),
            uiState = uiState,
            onIntent = vm::onIntent,
            onOpenItem = { itemId ->
                deps.navController.navigate(NavRoute.ItemDetails.createRoute(itemId))
            },
            onAddItem = {
                deps.navController.navigate(NavRoute.AddEditItem.createRoute(containerId = null))
            },
            onScan = {
                deps.navController.navigate(NavRoute.QRScan.route)
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
 * @param uiState: The current UI state for the Item Browser screen.
 * @return: TopBarConfig used by the app scaffold's top bar.
 */
private fun itemBrowserTopBarConfig(uiState: ItemBrowserUiState): TopBarConfig {
    val title = when (uiState) {
        is ItemBrowserUiState.Success -> "All Items"
        is ItemBrowserUiState.Loading -> "Loading…"
        is ItemBrowserUiState.Error -> "All Items"
    }
    val showBack = false

    return TopBarConfig(title = title, showBack = showBack)
}