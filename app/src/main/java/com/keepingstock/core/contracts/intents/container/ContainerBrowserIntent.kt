package com.keepingstock.core.contracts.intents.container

import com.keepingstock.data.entities.ItemStatus

sealed interface ContainerBrowserIntent {
    /* ---------- Search related ---------- */
    /**
     * TODO: We probably only need one of these two. Depends on the behavior we want:
     *  - QueryChange for "Search as you type"
     *  - QuerySubmit for "Search on enter"
     */
    data class QueryChange(val query: String) : ContainerBrowserIntent
    data class QuerySubmit(val query: String) : ContainerBrowserIntent

    /* ---------- Filtering related ---------- */
    data class FilterChange(val filter: ContainerBrowserFilter) : ContainerBrowserIntent

    /* ---------- Sorting related ---------- */
    data class SortChange(val sort: ContainerBrowserSort) : ContainerBrowserIntent

    /* ---------- Layout related ---------- */
    data class LayoutChange(val layout: ContainerBrowserLayout) : ContainerBrowserIntent
}

// TODO: Do we need other filters? I thought being able to filter by if it's taken out
//  or not would be a nice touch. Aside from that, I just put two boolean flags for
//  if visible results should include only containers/items
data class ContainerBrowserFilter(
    val includeContainers: Boolean = true,
    val includeItems: Boolean = true,
    val itemStatus: ItemStatus? = null // null = any
)

// Simple enums
enum class ContainerBrowserSort {
    NAME_ASC,
    NAME_DESC,
    CREATED_NEWEST,
    CREATED_OLDEST
}
enum class ContainerBrowserLayout {
    LIST,
    GRID,
    COMPACT
}