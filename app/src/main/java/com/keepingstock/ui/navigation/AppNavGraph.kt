package com.keepingstock.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.keepingstock.ui.screens.item.ItemBrowserScreen
import com.keepingstock.ui.screens.item.ItemDetailsScreen

@Composable
fun AppNavGraph() {
    // The navigation manager that tracks current screen and back stack
    val navController = rememberNavController()

    // The place in UI where the active destination composable is displayed
    NavHost(
        navController = navController,
        startDestination = NavRoute.ItemBrowser.route
    ) {

        // Register the ItemBrowser destination: when route == "item_browser", show ItemBrowserScreen
        composable(route = NavRoute.ItemBrowser.route) {
            ItemBrowserScreen(
                onOpenItem = { itemId ->
                    navController.navigate(NavRoute.ItemDetails.createRoute(itemId))
                }
            )
        }

        // Register the ItemDetails destination: when route == "item_details" with itemId,
        // show ItemDetailsScreen
        composable(
            route = NavRoute.ItemDetails.route,
            arguments = listOf(
                navArgument(NavRoute.ItemDetails.ITEM_ID) { type = NavType.StringType }
            )
        ) {
            backStackEntry ->
            val itemId = backStackEntry.arguments
                ?.getString(NavRoute.ItemDetails.ITEM_ID) ?: error("Missing itemId")

            ItemDetailsScreen(
                itemId = itemId,
                onBack = { navController.popBackStack()}
            )
        }
    }
}