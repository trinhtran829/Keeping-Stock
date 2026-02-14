package com.keepingstock.ui.navigation.destinations.container

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.keepingstock.core.contracts.Routes
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.navigation.containerIdOrNull
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.container.ContainerDetailScreen

internal fun NavGraphBuilder.addContainerDetailsDestination(
    deps: NavDeps
) {
    // Register the ContainerDetails destination: when route == "container_details" with
    // containerId, show ContainerDetailScreen
    composable(
        route = NavRoute.ContainerDetail.route,
        arguments = listOf(
            navArgument(Routes.Args.CONTAINER_ID) { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val containerId =
            backStackEntry.arguments?.containerIdOrNull(Routes.Args.CONTAINER_ID)
            ?: error("Missing containerId")

        LaunchedEffect(containerId) {
            deps.onTopBarChange(
                TopBarConfig(
                    title = "Container $containerId Details",
                    showBack = true
                )
            )
        }

        ContainerDetailScreen(
            containerId = containerId,
            onBack = { deps.navController.popBackStack() },
            onEdit = { id ->
                deps.navController.navigate(NavRoute.AddEditContainer.createRoute(containerId = id))
            }
        )
    }
}