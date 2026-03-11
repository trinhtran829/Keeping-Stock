package com.keepingstock.ui.viewmodel.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepingstock.core.contracts.BrowserEmptyState
import com.keepingstock.core.contracts.BrowserLayout
import com.keepingstock.core.contracts.BrowserSort
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemBrowserFilter
import com.keepingstock.core.contracts.intents.ViewModelContract
import com.keepingstock.core.contracts.intents.item.ItemBrowserIntent
import com.keepingstock.core.contracts.uistates.item.ItemBrowserUiState
import com.keepingstock.data.repositories.ItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Item Browser screen.
 *
 * The Item Browser is a global item viewer — it shows all items across all containers,
 * allowing users to search, filter, and sort without navigating the container hierarchy.
 *
 * Filter/sort/search are applied in-memory (MVP approach).
 *
 * @param itemRepository Repository for item data.
 */
class ItemBrowserViewModel(
    private val itemRepository: ItemRepository
) : ViewModel(), ViewModelContract<ItemBrowserUiState, ItemBrowserIntent> {

    private data class ControlState(
        val query: String = "",
        val filter: ItemBrowserFilter = ItemBrowserFilter(),
        val sort: BrowserSort = BrowserSort.NAME_ASC,
        val layout: BrowserLayout = BrowserLayout.LIST
    )

    private val _controlState = MutableStateFlow(ControlState())
    private val _loadVersion = MutableStateFlow(0)

    private val _uiState = MutableStateFlow<ItemBrowserUiState>(ItemBrowserUiState.Loading())
    override val uiState: StateFlow<ItemBrowserUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _loadVersion.collectLatest { startObserving() }
        }
    }

    private suspend fun startObserving() {
        _uiState.value = ItemBrowserUiState.Loading(query = _controlState.value.query)
        try {
            combine(itemRepository.observeItem(), _controlState) { allItems, controls ->
                buildSuccessState(allItems, controls)
            }.collect { state ->
                _uiState.value = state
            }
        } catch (e: Exception) {
            _uiState.value = ItemBrowserUiState.Error(
                query = _controlState.value.query,
                message = "Failed to load items",
                cause = e
            )
        }
    }

    override fun onIntent(intent: ItemBrowserIntent) {
        when (intent) {
            is ItemBrowserIntent.QueryChange ->
                _controlState.update { it.copy(query = intent.query) }

            ItemBrowserIntent.ClearQuery ->
                _controlState.update { it.copy(query = "") }

            is ItemBrowserIntent.FilterChange ->
                _controlState.update { it.copy(filter = intent.filter) }

            is ItemBrowserIntent.SortChange ->
                _controlState.update { it.copy(sort = intent.sort) }

            is ItemBrowserIntent.LayoutChange ->
                _controlState.update { it.copy(layout = intent.layout) }

            ItemBrowserIntent.Retry ->
                _loadVersion.update { it + 1 }
        }
    }

    private fun buildSuccessState(
        allItems: List<Item>,
        controls: ControlState
    ): ItemBrowserUiState.Success {
        val query = controls.query
        val filter = controls.filter
        val sort = controls.sort

        // Apply ItemBrowserFilter
        var filtered = allItems
        if (filter.storedInRootOnly) {
            filtered = filtered.filter { it.containerId == null }
        }
        if (filter.itemStatus != null) {
            filtered = filtered.filter { it.status == filter.itemStatus }
        }

        // Apply search query
        val visibleItems = if (query.isBlank()) {
            filtered
        } else {
            val queryLower = query.lowercase()
            filtered.filter { item ->
                item.name.lowercase().contains(queryLower) ||
                        item.tags.any { tag -> tag.name.lowercase().contains(queryLower) }
            }
        }

        // Apply sort
        val sortedItems = when (sort) {
            BrowserSort.NAME_ASC -> visibleItems.sortedBy { it.name.lowercase() }
            BrowserSort.NAME_DESC -> visibleItems.sortedByDescending { it.name.lowercase() }
            BrowserSort.CREATED_NEWEST -> visibleItems.sortedByDescending { it.createdDate }
            BrowserSort.CREATED_OLDEST -> visibleItems.sortedBy { it.createdDate }
        }

        // Determine empty state
        val emptyState = when {
            allItems.isEmpty() -> BrowserEmptyState.EMPTY
            sortedItems.isEmpty() -> BrowserEmptyState.NO_RESULTS
            else -> BrowserEmptyState.NONE
        }

        return ItemBrowserUiState.Success(
            items = allItems,
            visibleItems = sortedItems,
            query = query,
            filter = filter,
            sort = sort,
            layout = controls.layout,
            emptyState = emptyState
        )
    }
}
