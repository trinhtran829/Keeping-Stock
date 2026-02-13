package com.keepingstock.ui.navigation.destinations.container

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.keepingstock.core.contracts.Routes
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.container.AddEditContainerScreen

internal fun NavGraphBuilder.addAddEditContainerDestination(
    deps: NavDeps
) {
    composable(
        route = NavRoute.AddEditContainer.route,
        arguments = listOf(
            navArgument(Routes.Args.CONTAINER_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = -1L
            },
            navArgument(Routes.Args.PARENT_CONTAINER_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = -1L
            }
        )
    ) { backStackEntry ->
        val containerId =
            backStackEntry.arguments?.getString(Routes.Args.CONTAINER_ID)?.toLongOrNull()
        val parentContainerId =
            backStackEntry.arguments?.getString(Routes.Args.PARENT_CONTAINER_ID)?.toLongOrNull()

        LaunchedEffect(containerId, parentContainerId) {
            deps.onTopBarChange(
                TopBarConfig(
                    title = if (containerId == null) "Add Container" else "Edit Container $containerId",
                    showBack = true
                )
            )
        }

        // TODO: onSave action not implemented yet
        AddEditContainerScreen(
            containerId = containerId,
            parentContainerId = parentContainerId,
            onSave = { deps.navController.popBackStack() },
            onCancel = { deps.navController.popBackStack() }
        )
    }
}