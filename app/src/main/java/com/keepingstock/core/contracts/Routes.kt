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
        const val SUBJECT_TYPE = "subjectType"                  // "container" | "item"
        const val SUBJECT_ID = "subjectId"                      // Long
    }

    enum class SubjectType(val value: String) {
        Container("container"),
        Item("item");

        companion object {
            fun from(value: String): SubjectType =
                entries.firstOrNull { it.value == value } ?: error("Unknown SubjectType: $value")
        }
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
    const val SELECT_CONTAINER = "select_container"

    // Media
    const val CAMERA = "camera"
    const val GALLERY = "gallery"
    const val PHOTO = "photo"

    // Debug
    const val DEBUG_GALLERY = "debug_gallery"
    const val DEBUG_CONTAINER_BROWSER = "debug/container_browser"
    const val DEBUG_CONTAINER_DETAIL = "debug/container_detail"
    const val DEBUG_ADD_EDIT_CONTAINER = "debug/add_edit_container"
    const val DEBUG_ITEM_BROWSER = "debug/item_browser"
    const val DEBUG_ITEM_DETAIL = "debug/item_detail"
    const val DEBUG_ADD_EDIT_ITEM = "debug/add_edit_item"
}