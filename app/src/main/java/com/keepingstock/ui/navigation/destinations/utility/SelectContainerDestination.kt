package com.keepingstock.ui.navigation.destinations.utility

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.keepingstock.core.contracts.Routes
import com.keepingstock.core.contracts.Routes.SubjectType
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.navigation.containerIdOrNull

internal fun NavGraphBuilder.addSelectContainerDestination(
    deps: NavDeps
) {
    composable(
        route = NavRoute.SelectContainer.route,
        arguments = listOf(
            navArgument(Routes.Args.SUBJECT_TYPE) { type = NavType.StringType },
            navArgument(Routes.Args.SUBJECT_ID) { type = NavType.LongType },
            navArgument(Routes.Args.CONTAINER_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val args = backStackEntry.arguments ?: error("Missing args")
        val subjectType = SubjectType.from(args.getString(Routes.Args.SUBJECT_TYPE)!!)
        val subjectId = args.getLong(Routes.Args.SUBJECT_ID)
        val currentContainerId = args.containerIdOrNull(Routes.Args.CONTAINER_ID)
    }
}