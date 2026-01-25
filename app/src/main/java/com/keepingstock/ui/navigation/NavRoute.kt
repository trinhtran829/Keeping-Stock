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

    // ---------------
    // Details Screens
    // ---------------

    object ItemDetails : NavRoute {
        override val route: String = "${Routes.ITEM_DETAIL}/{${Routes.Args.ITEM_ID}}"

        // Function to build the actual route string
        fun createRoute(itemId: String): String =
            "${Routes.ITEM_DETAIL}/$itemId"
    }
}