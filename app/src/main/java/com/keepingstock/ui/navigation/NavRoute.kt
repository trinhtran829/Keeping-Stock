package com.keepingstock.ui.navigation

import android.net.Uri
import com.keepingstock.core.contracts.Routes

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
        fun createRoute(containerId: String? = null): String =
            containerId?.let { "${Routes.CONTAINER_BROWSER}?${Routes.Args.CONTAINER_ID}=$it" }
                ?: Routes.CONTAINER_BROWSER
    }

    // ---------------
    // Details Screens
    // ---------------

    object ItemDetails : NavRoute {
        override val route: String = "${Routes.ITEM_DETAIL}/{${Routes.Args.ITEM_ID}}"

        // Function to build the actual route string; uses path param
        fun createRoute(itemId: String): String =
            "${Routes.ITEM_DETAIL}/$itemId"
    }

    object ContainerDetail : NavRoute {
        override val route: String =
            "${Routes.CONTAINER_DETAIL}/{${Routes.Args.CONTAINER_ID}}"

        // Function to build the actual route string; uses path param
        fun createRoute(containerId: String): String = "${Routes.CONTAINER_DETAIL}/$containerId"
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
            containerId: String? = null,
            parentContainerId: String? = null
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
            itemId: String? = null,
            containerId: String? = null
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
}