package com.keepingstock.core.contracts

import com.keepingstock.data.entities.ItemStatus

/**
 * Container can filter out containers, filter out items, or filter based on item status.
 */
data class ContainerBrowserFilter(
    val includeContainers: Boolean = true,
    val includeItems: Boolean = true,
    val itemStatus: ItemStatus? = null // null = any
)

/**
 * Currently only two filters - show items that don't have a container (are in root) and item
 * status. You can add any others you think we'll need.
 */
data class ItemBrowserFilter(
    val storedInRootOnly: Boolean = false,
    val itemStatus: ItemStatus? = null // null = any
)

// Simple enums
enum class BrowserSort {
    NAME_ASC,
    NAME_DESC,
    CREATED_NEWEST,
    CREATED_OLDEST
}
enum class BrowserLayout {
    LIST,
    GRID,
    COMPACT
}

/**
 * EMPTY_CONTAINER: show message “There's nothing here yet!”
 * NO_RESULTS: show “No results found”
 * NONE: Not empty, render list
 */
enum class BrowserEmptyState {
    NONE,
    EMPTY,
    NO_RESULTS
}