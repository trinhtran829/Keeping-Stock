package com.keepingstock.ui.navigation

import android.net.Uri
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
import com.keepingstock.ui.screens.media.CameraScreen
import com.keepingstock.ui.screens.media.GalleryScreen
import com.keepingstock.ui.screens.media.PhotoScreen
import com.keepingstock.ui.screens.container.AddEditContainerScreen
import com.keepingstock.ui.screens.container.ContainerBrowserScreen
import com.keepingstock.ui.screens.container.ContainerDetailScreen
import com.keepingstock.ui.screens.debug.DebugGalleryScreen
import com.keepingstock.ui.screens.item.AddEditItemScreen
import com.keepingstock.ui.screens.item.ItemBrowserScreen
import com.keepingstock.ui.screens.item.ItemDetailsScreen
import com.keepingstock.ui.screens.qr.QRScanScreen

@Composable
fun AppNavGraph() {
    // The navigation manager that tracks current screen and back stack
    val navController = rememberNavController()
    var lastContainerId by rememberSaveable { mutableStateOf<String?>(null) }

    // The place in UI where the active destination composable is displayed
    NavHost(
        navController = navController,
        startDestination = Routes.CONTAINER_BROWSER
    ) {

        // ----------------------
        // Register Core Browsers
        // ----------------------

        // Register the ItemBrowser destination: when route == "item_browser", show ItemBrowserScreen
        composable(route = NavRoute.ItemBrowser.route) {
            ItemBrowserScreen(
                onOpenItem = { itemId ->
                    navController.navigate(NavRoute.ItemDetails.createRoute(itemId))
                },
                onOpenContainerBrowser = {
                    navController.navigate(
                        NavRoute.ContainerBrowser.createRoute(lastContainerId)) {
                        popUpTo(Routes.CONTAINER_BROWSER) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
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

            lastContainerId = containerId

            ContainerBrowserScreen(
                containerId = containerId,
                onOpenSubcontainer = { subId ->
                    navController.navigate(NavRoute.ContainerBrowser.createRoute(subId))
                },
                onOpenItem = { itemId ->
                    navController.navigate(NavRoute.ItemDetails.createRoute(itemId))
                },
                onOpenContainerInfo = { id ->
                    navController.navigate(NavRoute.ContainerDetail.createRoute(id))
                },
                onGoToItemBrowser = {
                    navController.navigate(NavRoute.ItemBrowser.route) {
                        popUpTo(Routes.CONTAINER_BROWSER) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onAddContainer = { parentId ->
                    navController.navigate(
                        NavRoute.AddEditContainer.createRoute(parentContainerId = parentId)
                    )
                },
                onAddItem = { cid ->
                    navController.navigate(NavRoute.AddEditItem.createRoute(containerId = cid))
                },
                onScanQr = {
                    navController.navigate(NavRoute.QRScan.route)
                }
            )
        }

        // ------------------------
        // Register Details Screens
        // ------------------------

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
                onBack = { navController.popBackStack()},
                onEdit = { id ->
                    navController.navigate(NavRoute.AddEditItem.createRoute(itemId = id))
                }

            )
        }

        // Register the ContainerDetails destination: when route == "container_details" with
        // containerId, show ContainerDetailScreen
        composable(
            route = NavRoute.ContainerDetail.route,
            arguments = listOf(
                navArgument(Routes.Args.CONTAINER_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val containerId = backStackEntry.arguments?.getString(Routes.Args.CONTAINER_ID)
                ?: error("Missing containerId")

            ContainerDetailScreen(
                containerId = containerId,
                onBack = { navController.popBackStack() },
                onEdit = { id ->
                    navController.navigate(NavRoute.AddEditContainer.createRoute(containerId = id))
                }
            )
        }

        // -------------------------
        // Register Add/Edit Screens
        // -------------------------

        composable(
            route = NavRoute.AddEditContainer.route,
            arguments = listOf(
                navArgument(Routes.Args.CONTAINER_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(Routes.Args.PARENT_CONTAINER_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val containerId = backStackEntry.arguments?.getString(Routes.Args.CONTAINER_ID)
            val parentContainerId = backStackEntry.arguments?.getString(Routes.Args.PARENT_CONTAINER_ID)

            // TODO: onSave action not implemented yet
            AddEditContainerScreen(
                containerId = containerId,
                parentContainerId = parentContainerId,
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoute.AddEditItem.route,
            arguments = listOf(
                navArgument(Routes.Args.ITEM_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(Routes.Args.CONTAINER_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString(Routes.Args.ITEM_ID)
            val containerId = backStackEntry.arguments?.getString(Routes.Args.CONTAINER_ID)

            // TODO: onSave action not implemented yet
            AddEditItemScreen(
                itemId = itemId,
                containerId = containerId,
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        // -----------------------
        // Register QR Scan Screen
        // -----------------------

        composable(route = NavRoute.QRScan.route) {
            QRScanScreen(
                onScannedContainer = { scannedContainerId ->
                    navController.popBackStack()
                    // Navigate to the container contents after a successful scan
                    navController.navigate(NavRoute.ContainerBrowser.createRoute(scannedContainerId))
                },
                onCancel = { navController.popBackStack() }
            )
        }

        // -----------------------
        // Register Media Screens
        // -----------------------

        composable(route = NavRoute.Camera.route) {
            CameraScreen(
                onOpenGallery = {
                    navController.navigate(NavRoute.Gallery.route)
                },
                onPhotoCaptured = { uri ->
                    navController.navigate(NavRoute.Photo.createRoute(uri))
                }
            )
        }

        composable(route = NavRoute.Gallery.route) {
            GalleryScreen(
                onPhotoSelected = { uri ->
                    navController.navigate(NavRoute.Photo.createRoute(uri))
                },
                onBack = { navController.popBackStack() }
            )
        }

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

            PhotoScreen(
                photoUri = photoUri,
                onBack = { navController.popBackStack() }
            )
        }

        // ----------------------
        // Register Debug Screens
        // ----------------------

        composable(route = NavRoute.DebugGallery.route) {
            DebugGalleryScreen(
                onOpenContainerBrowser = {
                    navController.navigate(Routes.CONTAINER_BROWSER) {
                        popUpTo(Routes.DEBUG_GALLERY) { inclusive = true }
                    }
                },
                onOpenItemBrowser = {
                    navController.navigate(NavRoute.ItemBrowser.route) {
                        popUpTo(Routes.DEBUG_GALLERY) { inclusive = true }
                    }
                },
                onOpenQrScan = { navController.navigate(NavRoute.QRScan.route) },
                onOpenCamera = { navController.navigate(NavRoute.Camera.route) },
                onOpenGallery = { navController.navigate(NavRoute.Gallery.route) }
            )
        }

    }
}