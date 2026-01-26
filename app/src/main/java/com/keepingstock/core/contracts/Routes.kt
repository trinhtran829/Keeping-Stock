package com.keepingstock.core.contracts

/**
 * Navigation contract:
 * - Route names and argument keys.
 */
object Routes {

    object Args {
        const val CONTAINER_ID = "containerId"
        const val ITEM_ID = "itemId"
        const val PARENT_CONTAINER_ID = "parentContainerId"
        const val PHOTO_URI = "photoUri"
    }

    // Core Browsers
    const val CONTAINER_BROWSER = "container_browser"
    const val ITEM_BROWSER = "item_browser"

    // Detail Screens
    const val CONTAINER_DETAIL = "container_detail"
    const val ITEM_DETAIL = "item_detail"

    // Add/Edit Forms
    const val ADD_EDIT_CONTAINER = "add_edit_container"
    const val ADD_EDIT_ITEM = "add_edit_item"

    // Utility
    const val QR_SCAN = "qr_scan"

    // Media
    const val CAMERA = "camera"
    const val GALLERY = "gallery"
    const val PHOTO = "photo"
}