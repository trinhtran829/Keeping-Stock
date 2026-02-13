package com.keepingstock.ui.navigation.destinations.container

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.keepingstock.core.contracts.Routes
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.container.ContainerBrowserScreen

internal fun NavGraphBuilder.addContainerBrowserDestination(
    deps: NavDeps,
    lastContainerIdState: MutableState<String?>
) {
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
    ) { backStackEntry ->
        val containerId = backStackEntry.arguments?.getString(Routes.Args.CONTAINER_ID)

        // TODO: Display container name in title instead of id
        LaunchedEffect(containerId) {
            deps.onTopBarChange(
                TopBarConfig(
                    title = (
                        if (containerId == null)
                            "Root Container Browser"
                        else
                            "Container $containerId Browser"
                    ),
                    showBack = containerId != null
                )
            )
        }

        // Track the last visited container for "Return to Containers" behavior.
        lastContainerIdState.value = containerId

        /*
        ContainerBrowserScreen(
            containerId = containerId,
            onOpenSubcontainer = { subId ->
                deps.navController.navigate(NavRoute.ContainerBrowser.createRoute(subId))
            },
            onOpenItem = { itemId ->
                deps.navController.navigate(NavRoute.ItemDetails.createRoute(itemId))
            },
            onOpenContainerInfo = { id ->
                deps.navController.navigate(NavRoute.ContainerDetail.createRoute(id))
            },
            onAddContainer = { parentId ->
                deps.navController.navigate(
                    NavRoute.AddEditContainer.createRoute(parentContainerId = parentId)
                )
            },
            onAddItem = { cid ->
                deps.navController.navigate(NavRoute.AddEditItem.createRoute(containerId = cid))
            }
        )
         */
    }
}