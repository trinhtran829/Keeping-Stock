package com.keepingstock.ui.navigation

import com.keepingstock.core.DebugFlags
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.keepingstock.core.contracts.Routes
import com.keepingstock.ui.navigation.destinations.container.addAddEditContainerDestination
import com.keepingstock.ui.navigation.destinations.item.addAddEditItemDestination
import com.keepingstock.ui.navigation.destinations.media.addCameraDestination
import com.keepingstock.ui.navigation.destinations.container.addContainerBrowserDestination
import com.keepingstock.ui.navigation.destinations.container.addContainerDetailsDestination
import com.keepingstock.ui.navigation.destinations.debug.addDebugGalleryDestination
import com.keepingstock.ui.navigation.destinations.media.addGalleryDestination
import com.keepingstock.ui.navigation.destinations.item.addItemBrowserDestination
import com.keepingstock.ui.navigation.destinations.item.addItemDetailsDestination
import com.keepingstock.ui.navigation.destinations.media.addPhotoViewerDestination
import com.keepingstock.ui.navigation.destinations.media.addQRScanDestination
import com.keepingstock.ui.scaffold.TopBarConfig

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
    val lastContainerIdState = rememberSaveable { mutableStateOf<String?>(null) }
    val startDestination =
        if (DebugFlags.ENABLE_DEBUG_GALLERY) Routes.DEBUG_GALLERY else Routes.CONTAINER_BROWSER

    val deps = NavDeps(
        navController = navController,
        onTopBarChange = onTopBarChange,
        showSnackbar = showSnackbar
    )

    // The place in UI where the active destination composable is displayed
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.padding(contentPadding)
    ) {
        // ----------------------
        // Register Core Browsers
        // ----------------------
        addItemBrowserDestination(deps, lastContainerId = { lastContainerIdState.value })
        addContainerBrowserDestination(deps, lastContainerIdState)

        // ------------------------
        // Register Details Screens
        // ------------------------
        addItemDetailsDestination(deps)
        addContainerDetailsDestination(deps)

        // -------------------------
        // Register Add/Edit Screens
        // -------------------------
        addAddEditContainerDestination(deps)
        addAddEditItemDestination(deps)

        // -----------------------
        // Register QR Scan Screen
        // -----------------------
        addQRScanDestination(deps)

        // -----------------------
        // Register Media Screens
        // -----------------------
        addCameraDestination(deps)
        addGalleryDestination(deps)
        addPhotoViewerDestination(deps)

        // ---------------------------------
        // CUSTOM SCREENS - TO BE FORMALIZED
        // ---------------------------------
        // Register custom/temporary screens here

        // ----------------------
        // Register Debug Screens
        // ----------------------
        addDebugGalleryDestination(deps)

    }
}