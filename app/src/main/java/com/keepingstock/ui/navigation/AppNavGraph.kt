package com.keepingstock.ui.navigation

import com.keepingstock.core.DebugFlags
import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.keepingstock.core.contracts.Routes
import com.keepingstock.ui.scaffold.TopBarConfig
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

/**
 * Defines the top-level navigation graph for the application.
 *
 * This composable is responsible for:
 * - Selecting the start destination based on debug configuration flags.
 * - Registering all routes and mapping them to their corresponding screens.
 * - Wiring navigation callbacks between screens, including argument passing and back stack
 *   behavior
 *
 * The navigation graph includes:
 * - Core browser screens for containers and items
 * - Detail screens for viewing individual containers and items
 * - Add/Edit screens for containers and items
 * - QR scanning screen
 * - Media screens (camera, gallery, photo preview)
 * - Debug gallery screens
 *
 * ---
 * GenAI usage citation:
 * This code was generated with the help of ChatGPT.
 * This transcript documents the GenAI interaction that led to this code:
 * https://chatgpt.com/share/6979a590-ad20-800f-84e4-df349b314ecb
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    onTopBarChange: (TopBarConfig) -> Unit,
    showSnackbar: (String) -> Unit = {}
) {
    var lastContainerId by rememberSaveable { mutableStateOf<String?>(null) }
    val startDestination =
        if (DebugFlags.ENABLE_DEBUG_GALLERY) Routes.DEBUG_GALLERY else Routes.CONTAINER_BROWSER

    // The place in UI where the active destination composable is displayed
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.padding(contentPadding)
    ) {

        // TODO: AppNavGraph is becoming monolithic and unwieldy, too many lines of code;
        //      refactor to reduce length by moving destinations to individual
        //      NavGraphBuilder functions.

        // ----------------------
        // Register Core Browsers
        // ----------------------

        // Register the ItemBrowser destination: when route == "item_browser", show ItemBrowserScreen
        composable(route = NavRoute.ItemBrowser.route) {

            LaunchedEffect(Unit) {
                onTopBarChange(
                    TopBarConfig(
                        title = "All Items",
                        showBack = false
                    )
                )
            }

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

            // TODO: Display container name in title instead of id
            LaunchedEffect(containerId) {
                onTopBarChange(
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

            // TODO: Display item name in title instead of id
            LaunchedEffect(itemId) {
                onTopBarChange(
                    TopBarConfig(
                        title = "Item Details: $itemId",
                        showBack = true
                    )
                )
            }

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

            LaunchedEffect(containerId) {
                onTopBarChange(
                    TopBarConfig(
                        title = "Container $containerId Details",
                        showBack = true
                    )
                )
            }

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

            LaunchedEffect(containerId, parentContainerId) {
                onTopBarChange(
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

            LaunchedEffect(itemId, containerId) {
                onTopBarChange(
                    TopBarConfig(
                        title = if (itemId == null) "Add Item" else "Edit Item: $itemId",
                        showBack = true
                    )
                )
            }

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
            LaunchedEffect(Unit) {
                onTopBarChange(
                    TopBarConfig(
                        title = "Scan QR",
                        showBack = true
                    )
                )
            }

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
            LaunchedEffect(Unit) {
                onTopBarChange(
                    TopBarConfig(
                        title = "Camera",
                        showBack = true
                    )
                )
            }

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
            LaunchedEffect(Unit) {
                onTopBarChange(
                    TopBarConfig(
                        title = "Gallery",
                        showBack = true
                    )
                )
            }

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

            // TODO: Add photo name to title?
            LaunchedEffect(encoded) {
                onTopBarChange(
                    TopBarConfig(
                        title = "Photo",
                        showBack = true
                    )
                )
            }

            PhotoScreen(
                photoUri = photoUri,
                onBack = { navController.popBackStack() }
            )
        }

        // ---------------------------------
        // CUSTOM SCREENS - TO BE FORMALIZED
        // ---------------------------------

        // Register custom/temporary screens here


        // ----------------------
        // Register Debug Screens
        // ----------------------

        composable(route = NavRoute.DebugGallery.route) {
            LaunchedEffect(Unit) {
                onTopBarChange(
                    TopBarConfig(
                        title = "Debug Gallery",
                        showBack = false
                    )
                )
            }

            DebugGalleryScreen(
                onOpenContainerBrowser = {
                    navController.navigate(NavRoute.ContainerBrowser.createRoute(null))
                },
                onOpenItemBrowser = {
                    navController.navigate(NavRoute.ItemBrowser.route)
                },
                onOpenQrScan = { navController.navigate(NavRoute.QRScan.route) },
                onOpenCamera = { navController.navigate(NavRoute.Camera.route) },
                onOpenGallery = { navController.navigate(NavRoute.Gallery.route) },

                // TODO: placeholder URI. Swap with real demo photo Uri later
                onOpenPhotoDemo = {
                    val demo = android.net.Uri.parse("content://media/external/images/media/1")
                    navController.navigate(NavRoute.Photo.createRoute(demo))
                },
                onShowSnackbarDemo = {
                    showSnackbar("This is a demo snackbar message!")
                }
            )
        }

    }
}