package com.keepingstock.ui.navigation.destinations.item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.UiState
import com.keepingstock.data.entities.ItemStatus
import com.keepingstock.ui.components.navigation.ChipOption
import com.keepingstock.ui.components.navigation.DemoMode
import com.keepingstock.ui.components.navigation.DemoModeToggleRow
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
        // TODO(REMOVE): Demo-only mode selector
        var demoMode by rememberSaveable() {
            mutableStateOf(DemoMode.READY)
        }

        // TODO(REPLACE): Replace this with ViewModel uiState
        val uiState = remember(demoMode) {
            when (demoMode) {
                DemoMode.LOADING -> UiState.Loading
                DemoMode.ERROR -> UiState.Error(
                    "Error encountered when attempting to display all items."
                )
                DemoMode.EMPTY -> demoItemBrowserReadyState(true)
                DemoMode.READY, DemoMode.POPULATED -> demoItemBrowserReadyState(false)
            }
        }

        // Build TopBarConfig from current UiState
        val topBarConfig = remember(uiState) { itemBrowserTopBarConfig(uiState) }

        // Push top bar updates to scaffold
        LaunchedEffect(topBarConfig) {
            deps.onTopBarChange(topBarConfig)
        }

        /*
         * Kept in the destination (not the screen) so ContainerBrowserScreen stays production-like
         * and purely state-driven.
         *
         * TODO(REMOVE): Demo-mode toggle UI. Remove when ViewModel is implemented and render
         *      screen directly instead.
         */
        Column(Modifier.fillMaxSize()) {
            DemoModeToggleRow(
                title = "Select demo mode:",
                selected = demoMode,
                options = listOf (
                    ChipOption(DemoMode.POPULATED, "Populated"),
                    ChipOption(DemoMode.EMPTY, "Empty"),
                    ChipOption(DemoMode.LOADING, "Loading"),
                    ChipOption(DemoMode.ERROR, "Error")
                ),
                onSelect = { demoMode = it }
            )

            ItemBrowserScreen(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                onOpenItem = { itemId ->
                    deps.navController.navigate(NavRoute.ItemDetails.createRoute(itemId))
                },
                onAddItem = {
                    deps.navController.navigate(NavRoute.AddEditItem.createRoute(containerId = null))
                }
            )
        }
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

/**
 * TODO(REMOVE): Demo-only Ready state builder.
 * ---
 * GenAI usage citation:
 * Sample item detail data auto-generated with the help of ChatGPT.
 * Prompt: "Please generate data for a sample object with the following class signature:"
 */
private fun demoItemBrowserReadyState(isEmpty: Boolean):
        UiState.Success<ItemBrowserUiData> {
    val containerId = ContainerId(1L)

    val items = if (isEmpty) emptyList<Item>() else listOf(
        Item(
            id = ItemId(containerId.value * 100 + 1),
            name = "Impact Driver",
            description = "18V brushless",
            containerId = containerId,
            status = ItemStatus.STORED
        ),
        Item(
            id = ItemId(containerId.value * 100 + 2),
            name = "Reciprocating Saw",
            description = "Corded",
            containerId = containerId,
            status = ItemStatus.STORED
        ),
    )

    return UiState.Success(
        data = ItemBrowserUiData(items = items)
    )
}