package com.keepingstock.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.media.CameraScreen

internal fun NavGraphBuilder.addCameraDestination(
    deps: NavDeps
) {
    composable(route = NavRoute.Camera.route) {
        LaunchedEffect(Unit) {
            deps.onTopBarChange(
                TopBarConfig(
                    title = "Camera",
                    showBack = true
                )
            )
        }

        CameraScreen(
            onOpenGallery = {
                deps.navController.navigate(NavRoute.Gallery.route)
            },
            onPhotoCaptured = { uri ->
                deps.navController.navigate(NavRoute.Photo.createRoute(uri))
            }
        )
    }
}