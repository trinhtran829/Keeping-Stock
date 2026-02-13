package com.keepingstock.ui.navigation.destinations.item

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Routes
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.item.ItemBrowserScreen

internal fun NavGraphBuilder.addItemBrowserDestination(
    deps: NavDeps,
    lastContainerId: () -> ContainerId?
) {
    // Register the ItemBrowser destination: when route == "item_browser", show ItemBrowserScreen
    composable(route = NavRoute.ItemBrowser.route) {

        LaunchedEffect(Unit) {
            deps.onTopBarChange(
                TopBarConfig(
                    title = "All Items",
                    showBack = false
                )
            )
        }

        ItemBrowserScreen(
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