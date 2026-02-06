package com.keepingstock.ui.navigation.destinations

import android.net.Uri
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.keepingstock.core.contracts.Routes
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.media.PhotoScreen

internal fun NavGraphBuilder.addPhotoViewerDestination(
    deps: NavDeps
) {
    composable(
        route = NavRoute.Photo.route,
        arguments = listOf(
            navArgument(Routes.Args.PHOTO_URI) {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val encoded = backStackEntry.arguments?.getString(Routes.Args.PHOTO_URI)
            ?: return@composable

        val photoUri = Uri.parse(Uri.decode(encoded))

        // TODO: Add photo name to title?
        LaunchedEffect(encoded) {
            deps.onTopBarChange(
                TopBarConfig(
                    title = "Photo",
                    showBack = true
                )
            )
        }

        PhotoScreen(
            photoUri = photoUri,
            onBack = { deps.navController.popBackStack() }
        )
    }
}