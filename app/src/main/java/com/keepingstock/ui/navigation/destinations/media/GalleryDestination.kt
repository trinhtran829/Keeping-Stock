package com.keepingstock.ui.navigation.destinations.media

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.media.GalleryScreen

internal fun NavGraphBuilder.addGalleryDestination(
    deps: NavDeps
) {
    composable(route = NavRoute.Gallery.route) {
        LaunchedEffect(Unit) {
            deps.onTopBarChange(
                TopBarConfig(
                    title = "Gallery",
                    showBack = true
                )
            )
        }

        GalleryScreen(
            onPhotoSelected = { uri ->
                deps.navController.navigate(NavRoute.Photo.createRoute(uri))
            },
            onBack = { deps.navController.popBackStack() }
        )
    }
}