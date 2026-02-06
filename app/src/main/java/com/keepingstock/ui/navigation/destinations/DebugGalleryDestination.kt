package com.keepingstock.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.debug.DebugGalleryScreen

internal fun NavGraphBuilder.addDebugGalleryDestination(
    deps: NavDeps
) {
    composable(route = NavRoute.DebugGallery.route) {
        LaunchedEffect(Unit) {
            deps.onTopBarChange(
                TopBarConfig(
                    title = "Debug Gallery",
                    showBack = false
                )
            )
        }

        DebugGalleryScreen(
            onOpenContainerBrowser = {
                deps.navController.navigate(NavRoute.ContainerBrowser.createRoute(null))
            },
            onOpenItemBrowser = {
                deps.navController.navigate(NavRoute.ItemBrowser.route)
            },
            onOpenQrScan = { deps.navController.navigate(NavRoute.QRScan.route) },
            onOpenCamera = { deps.navController.navigate(NavRoute.Camera.route) },
            onOpenGallery = { deps.navController.navigate(NavRoute.Gallery.route) },

            // TODO: placeholder URI. Swap with real demo photo Uri later
            onOpenPhotoDemo = {
                val demo = android.net.Uri.parse("content://media/external/images/media/1")
                deps.navController.navigate(NavRoute.Photo.createRoute(demo))
            },
            onShowSnackbarDemo = {
                deps.showSnackbar("This is a demo snackbar message!")
            }
        )
    }
}