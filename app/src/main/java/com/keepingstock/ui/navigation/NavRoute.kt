package com.keepingstock.ui.navigation

import android.net.Uri
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.Routes

/**
 * All navigation routes used by the application.
 *
 * ---
 * GenAI usage citation:
 * This code was generated with the help of ChatGPT.
 * This transcript documents the GenAI interaction that led to this code:
 * https://chatgpt.com/share/6979a590-ad20-800f-84e4-df349b314ecb
 */
sealed interface NavRoute {
    val route: String

    // -------------
    // Core Browsers
    // -------------

    object ItemBrowser : NavRoute {
        override val route: String = Routes.ITEM_BROWSER
    }

    object ContainerBrowser : NavRoute {
        // Use query param for route since containerId is optional
        override val route: String =
            "${Routes.CONTAINER_BROWSER}?${Routes.Args.CONTAINER_ID}={${Routes.Args.CONTAINER_ID}}"

        // Function to build the actual route string; uses query param
        fun createRoute(containerId: ContainerId? = null): String =
            containerId?.let { "${Routes.CONTAINER_BROWSER}?${Routes.Args.CONTAINER_ID}=${it.value}" }
                ?: Routes.CONTAINER_BROWSER
    }

    // ---------------
    // Details Screens
    // ---------------

    object ItemDetails : NavRoute {
        override val route: String = "${Routes.ITEM_DETAIL}/{${Routes.Args.ITEM_ID}}"

        // Function to build the actual route string; uses path param
        fun createRoute(itemId: ItemId): String =
            "${Routes.ITEM_DETAIL}/$itemId"
    }

    object ContainerDetail : NavRoute {
        override val route: String =
            "${Routes.CONTAINER_DETAIL}/{${Routes.Args.CONTAINER_ID}}"

        // Function to build the actual route string; uses path param
        fun createRoute(containerId: ContainerId): String = "${Routes.CONTAINER_DETAIL}/$containerId"
    }

    // ----------------
    // Add/Edit Screens
    // ----------------

    object AddEditContainer : NavRoute {
        // Use query param for route since containerId and parentContainerId are optional
        override val route: String =
            "${Routes.ADD_EDIT_CONTAINER}?" +
                    "${Routes.Args.CONTAINER_ID}={${Routes.Args.CONTAINER_ID}}&" +
                    "${Routes.Args.PARENT_CONTAINER_ID}={${Routes.Args.PARENT_CONTAINER_ID}}"

        // Function to build the actual route string; uses query param, builds using params list
        // based on whether containerId and parentContainerId values were provided.
        fun createRoute(
            containerId: ContainerId? = null,
            parentContainerId: ContainerId? = null
        ): String {
            val base = Routes.ADD_EDIT_CONTAINER
            val params = buildList {
                if (containerId != null) add("${Routes.Args.CONTAINER_ID}=$containerId")
                if (parentContainerId != null) add("${Routes.Args.PARENT_CONTAINER_ID}=$parentContainerId")
            }
            return if (params.isEmpty()) base else base + "?" + params.joinToString("&")
        }
    }

    object AddEditItem : NavRoute {
        override val route: String =
            "${Routes.ADD_EDIT_ITEM}?" +
                    "${Routes.Args.ITEM_ID}={${Routes.Args.ITEM_ID}}&" +
                    "${Routes.Args.CONTAINER_ID}={${Routes.Args.CONTAINER_ID}}"

        // Function to build the actual route string; uses query param, builds using params list
        // based on whether itemId and containerId values were provided.
        fun createRoute(
            itemId: ItemId? = null,
            containerId: ContainerId? = null
        ): String {
            val base = Routes.ADD_EDIT_ITEM

            val params = buildList {
                if (itemId != null) add("${Routes.Args.ITEM_ID}=$itemId")
                if (containerId != null) add("${Routes.Args.CONTAINER_ID}=$containerId")
            }
            return if (params.isEmpty()) base else base + "?" + params.joinToString("&")
        }
    }

    // ---------------
    // Utility Screens
    // ---------------

    object QRScan : NavRoute {
        override val route: String = Routes.QR_SCAN
    }

    // -------------
    // Media Screens
    // -------------

    object Camera : NavRoute {
        override val route: String = Routes.CAMERA
    }

    object Gallery : NavRoute {
        override val route: String = Routes.GALLERY
    }

    object Photo : NavRoute {
        override val route: String = "${Routes.PHOTO}/{${Routes.Args.PHOTO_URI}}"

        fun createRoute(photoUri: Uri): String {
            val encoded = Uri.encode(photoUri.toString())
            return "${Routes.PHOTO}/$encoded"
        }
    }

    // ---------------------------------
    // CUSTOM SCREENs - TO BE FORMALIZED
    // ---------------------------------

    // Define custom/temporary screen routes here


    // -------------
    // Debug Screens
    // -------------

    object DebugGallery : NavRoute {
        override val route: String = Routes.DEBUG_GALLERY
    }
}