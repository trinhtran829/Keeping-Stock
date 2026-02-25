package com.keepingstock.core.contracts.intents.item

import com.keepingstock.core.contracts.uistates.item.ItemBrowserFilter
import com.keepingstock.core.contracts.uistates.item.ItemBrowserLayout
import com.keepingstock.core.contracts.uistates.item.ItemBrowserSort

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
