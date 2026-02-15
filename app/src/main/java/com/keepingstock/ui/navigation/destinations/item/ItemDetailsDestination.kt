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
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.Routes
import com.keepingstock.core.contracts.uistates.item.ItemDetailUiState
import com.keepingstock.data.entities.ItemStatus
import com.keepingstock.ui.components.navigation.ChipOption
import com.keepingstock.ui.components.navigation.DemoMode
import com.keepingstock.ui.components.navigation.DemoModeToggleRow
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.navigation.itemIdOrNull
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.item.ItemDetailsScreen

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

        // TODO(REMOVE): Demo-only mode selector
        var demoMode by rememberSaveable(itemId.value) {
            mutableStateOf(DemoMode.READY)
        }

        // TODO(REMOVE): Replace this with ViewModel uiState.
        val uiState = remember(itemId, demoMode) {
            when (demoMode) {
                DemoMode.LOADING -> ItemDetailUiState.Loading
                DemoMode.READY, DemoMode.POPULATED -> demoItemDetailReadyState(itemId)
                DemoMode.ERROR, DemoMode.EMPTY ->
                    ItemDetailUiState.Error("Demo error loading item details.")
            }
        }

        val topBarConfig = remember(uiState) { itemDetailTopBarConfig(uiState) }

        LaunchedEffect(topBarConfig) {
            deps.onTopBarChange(topBarConfig)
        }

        Column(Modifier.fillMaxSize()) {
            // TODO(REMOVE): demo-only UI controls.
            DemoModeToggleRow(
                title = "Select demo mode:",
                selected = demoMode,
                options = listOf(
                    ChipOption(DemoMode.READY, "Ready"),
                    ChipOption(DemoMode.LOADING, "Loading"),
                    ChipOption(DemoMode.ERROR, "Error")
                ),
                onSelect = { demoMode = it }
            )
            ItemDetailsScreen(
                itemId = itemId,
                onBack = { deps.navController.popBackStack() },
                onEdit = { id ->
                    deps.navController.navigate(NavRoute.AddEditItem.createRoute(itemId = id))
                }
            )
        }
    }
}

/**
 * Builds top bar title/back behavior from ItemDetailUiState.
 */
private fun itemDetailTopBarConfig(uiState: ItemDetailUiState): TopBarConfig {
    val title = when (uiState) {
        is ItemDetailUiState.Ready ->  "Item Details"//uiState.containerName + " Details"
        is ItemDetailUiState.Loading -> "Loading…"
        is ItemDetailUiState.Error -> "Container details"
    }
    return TopBarConfig(title = title, showBack = true)
}

/**
 * TODO(REMOVE): Demo-only Ready state builder.
 * ---
 * GenAI usage citation:
 * Sample container detail data auto-generated with the help of ChatGPT.
 * Prompt: "Please generate data for a sample object with the following class signature:"
 */
private fun demoItemDetailReadyState(itemId: ItemId): ItemDetailUiState.Ready {
    return ItemDetailUiState.Ready(
        itemId = itemId,
        item = Item(
            id = itemId,
            name = "Container ${itemId.value}",
            description = "Example container detail description.",
            imageUri = "demo",
            containerId = null,
            status = ItemStatus.STORED
        ),
        parentContainerName = null
    )
}