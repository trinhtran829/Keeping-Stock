package com.keepingstock.core.contracts

import com.keepingstock.data.entities.ItemStatus

// TODO: Do we need other filters? I thought being able to filter by if it's taken out
//  or not would be a nice touch. Aside from that, I just put two boolean flags for
//  if visible results should include only containers/items
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