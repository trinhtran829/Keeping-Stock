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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.keepingstock.core.contracts.ContainerId
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
import com.keepingstock.ui.viewmodel.item.ItemDetailViewModel
import kotlinx.coroutines.flow.collectLatest
import java.util.Date

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
            modifier = Modifier.fillMaxSize()
            uiState = uiState,
            onIntent = vm::onIntent,
            onBack = { deps.navController.popBackStack() },
            onEdit = { id ->
                deps.navController.navigate(NavRoute.AddEditItem.createRoute(itemId = id))
            },
            onMove = {/* TODO: hook up when Move flow exists*/}
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