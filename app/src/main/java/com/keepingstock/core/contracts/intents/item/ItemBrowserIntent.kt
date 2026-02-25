package com.keepingstock.core.contracts.intents.item

import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.data.entities.ItemStatus

sealed interface ItemBrowserIntent {
    /* ---------- Search related ---------- */
    data class QueryChange(val query: String) : ItemBrowserIntent
    data object ClearQuery : ItemBrowserIntent

    /* ---------- Filtering related ---------- */
    data class FilterChange(val filter: ItemBrowserFilter) : ItemBrowserIntent

    /* ---------- Sorting related ---------- */
    data class SortChange(val sort: ItemBrowserSort) : ItemBrowserIntent

    /* ---------- Layout related ---------- */
    data class LayoutChange(val layout: ItemBrowserLayout) : ItemBrowserIntent

    /* ---------- Error state related ---------- */
    data object Retry : ItemBrowserIntent
}

data class ItemBrowserFilter(
    val scope: ItemBrowserScope = ItemBrowserScope.ALL,
    val itemStatus: ItemStatus? = null // null = any
)

sealed interface ItemBrowserScope {
    data object ALL : ItemBrowserScope
    data object UNSORTED : ItemBrowserScope              // containerId == null
    data class IN_CONTAINER(val containerId: ContainerId) : ItemBrowserScope
}

enum class ItemBrowserSort {
    NAME_ASC,
    NAME_DESC,
    CREATED_NEWEST,
    CREATED_OLDEST
}

enum class ItemBrowserLayout { LIST, GRID, COMPACT }