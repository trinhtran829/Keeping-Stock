package com.keepingstock.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.keepingstock.core.contracts.Routes
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
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
        val itemId = backStackEntry.arguments?.getString(Routes.Args.ITEM_ID)
            ?: error("Missing itemId")

        // TODO: Display item name in title instead of id
        LaunchedEffect(itemId) {
            deps.onTopBarChange(
                TopBarConfig(
                    title = "Item Details: $itemId",
                    showBack = true
                )
            )
        }

        ItemDetailsScreen(
            itemId = itemId,
            onBack = { deps.navController.popBackStack()},
            onEdit = { id ->
                deps.navController.navigate(NavRoute.AddEditItem.createRoute(itemId = id))
            }

        )
    }
}