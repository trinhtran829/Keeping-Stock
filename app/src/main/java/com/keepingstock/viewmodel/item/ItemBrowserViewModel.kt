package com.keepingstock.ui.viewmodel.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemRepository
import com.keepingstock.core.contracts.UiState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Data needed to render the item browser UI
data class ItemBrowserUiData(
    val items: List<Item> = emptyList(),
    val containerId: String? = null
)

@OptIn(FlowPreview::class)
class ItemBrowserViewModel(
    private val repository: ItemRepository
) : ViewModel() {

    // Current Search Text
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Optional filter to only show items in current container
    private val _activeContainerId = MutableStateFlow<String?>(null)

    // Exposed UI state (Loading, Success, or Error)
    private val _uiState = MutableStateFlow<UiState<ItemBrowserUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<ItemBrowserUiData>> = _uiState.asStateFlow()

    init {
        observeSearch()
    }

    // Combines search query and container filter, then loads when changed
    private fun observeSearch() {
        viewModelScope.launch {
            combine(
                _searchQuery.debounce(300),
                _activeContainerId
            ) { query, containerId ->
                query to containerId
            }.collect { (query, containerId) ->
                loadItems(query, containerId)
            }
        }
    }

    // Update when user types in search field
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Filter by container
    fun setContainer(containerId: String?) {
        _activeContainerId.value = containerId
    }

    // Get items from repository based on current filters
    private suspend fun loadItems(query: String, containerId: String?) {
        _uiState.value = UiState.Loading
        try {
            val items = when {
                query.isNotBlank() -> repository.searchItems(query, emptyList())
                containerId != null -> repository.getItemsInContainer(containerId)
                else -> emptyList()
            }
            _uiState.value = UiState.Success(
                ItemBrowserUiData(items = items, containerId = containerId)
            )
        } catch (e: Exception) {
            _uiState.value = UiState.Error(
                message = "Failed to load items",
                cause = e
            )
        }
    }
}