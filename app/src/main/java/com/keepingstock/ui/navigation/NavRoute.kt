package com.keepingstock.ui.navigation

sealed interface NavRoute {
    val route: String

    object ItemBrowser : NavRoute {
        override val route: String = "item_browser"
    }
}