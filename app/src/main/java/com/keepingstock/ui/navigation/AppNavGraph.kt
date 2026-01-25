package com.keepingstock.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.keepingstock.core.contracts.Routes
import com.keepingstock.ui.screens.container.ContainerBrowserScreen
import com.keepingstock.ui.screens.item.ItemBrowserScreen
import com.keepingstock.ui.screens.item.ItemDetailsScreen

@Composable
fun AppNavGraph() {
    // The navigation manager that tracks current screen and back stack
    val navController = rememberNavController()
    var lastContainerId by rememberSaveable { mutableStateOf<String?>(null) }

    // The place in UI where the active destination composable is displayed
    NavHost(
        navController = navController,
        startDestination = NavRoute.ContainerBrowser.route
    ) {

        // Register the ItemBrowser destination: when route == "item_browser", show ItemBrowserScreen
        composable(route = NavRoute.ItemBrowser.route) {
            ItemBrowserScreen(
                onOpenItem = { itemId ->
                    navController.navigate(NavRoute.ItemDetails.createRoute(itemId))
                },
                onOpenContainerBrowser = {
                    navController.navigate(
                        NavRoute.ContainerBrowser.createRoute(lastContainerId)) {
                            launchSingleTop = true
                    }
                }
            )
        }

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
        ) {
            backStackEntry ->
            val containerId = backStackEntry.arguments?.getString(Routes.Args.CONTAINER_ID)

            if (containerId != null) {
                lastContainerId = containerId
            } else if (lastContainerId == null) {
                lastContainerId = null // root
            }

            ContainerBrowserScreen(
                containerId = containerId,
                onOpenItem = { itemId ->
                    navController.navigate(NavRoute.ItemDetails.createRoute(itemId))
                },
                onGoToItemBrowser = {
                    navController.navigate(NavRoute.ItemBrowser.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

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

            ItemDetailsScreen(
                itemId = itemId,
                onBack = { navController.popBackStack()}
            )
        }
    }
}