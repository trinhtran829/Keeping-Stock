package com.keepingstock.ui.navigation

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
}