package com.keepingstock.ui.navigation

sealed interface NavRoute {
    val route: String

    // -------------
    // Core Browsers
    // -------------

    object ItemBrowser : NavRoute {
        override val route: String = "item_browser"
    }

    // ---------------
    // Details Screens
    // ---------------

    object ItemDetails : NavRoute {
        const val ITEM_ID = "itemId"
        override val route: String = "item_details/{$ITEM_ID}"

        // Function to build the actual route string
        fun createRoute(itemId: String): String = "item_details/$itemId"
    }
}