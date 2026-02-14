package com.keepingstock.ui.navigation.destinations.item

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.keepingstock.core.contracts.Routes
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.navigation.containerIdOrNull
import com.keepingstock.ui.navigation.itemIdOrNull
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.item.AddEditItemScreen

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

        LaunchedEffect(itemId, containerId) {
            deps.onTopBarChange(
                TopBarConfig(
                    title = if (itemId == null) "Add Item" else "Edit Item: $itemId",
                    showBack = true
                )
            )
        }

        // TODO: onSave action not implemented yet
        AddEditItemScreen(
            itemId = itemId,
            containerId = containerId,
            onSave = { deps.navController.popBackStack() },
            onCancel = { deps.navController.popBackStack() }
        )
    }
}