package com.keepingstock.ui.viewmodel.container

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepingstock.core.contracts.BrowserEmptyState
import com.keepingstock.core.contracts.BrowserLayout
import com.keepingstock.core.contracts.BrowserSort
import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerBrowserFilter
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.intents.ViewModelContract
import com.keepingstock.core.contracts.intents.container.ContainerBrowserIntent
import com.keepingstock.core.contracts.uistates.container.ContainerBrowserUiState
import com.keepingstock.data.repositories.ContainerRepository
import com.keepingstock.data.repositories.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Container Browser screen.
 *
 * Loads and exposes the contents of a container (or root when [containerId] is null),
 * and handles search, filter, sort, and layout intents by recomputing derived visible lists
 * entirely in-memory (MVP approach — no additional DB queries for filtering/sorting).
 *
 * @param containerId The container to display, or null for the root level.
 * @param containerRepository Repository for container data.
 * @param itemRepository Repository for item data.
 */
class ContainerBrowserViewModel(
    private val containerId: ContainerId?,
    private val containerRepository: ContainerRepository,
    private val itemRepository: ItemRepository
) : ViewModel(), ViewModelContract<ContainerBrowserUiState, ContainerBrowserIntent> {

    private data class ControlState(
        val query: String = "",
        val filter: ContainerBrowserFilter = ContainerBrowserFilter(),
        val sort: BrowserSort = BrowserSort.NAME_ASC,
        val layout: BrowserLayout = BrowserLayout.LIST
    )

    private val _controlState = MutableStateFlow(ControlState())
    private val _loadVersion = MutableStateFlow(0)

    private val _uiState = MutableStateFlow<ContainerBrowserUiState>(ContainerBrowserUiState.Loading)
    override val uiState: StateFlow<ContainerBrowserUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // collectLatest ensures that when Retry triggers a new load version,
            // the previous startObserving() coroutine is cancelled before starting fresh.
            _loadVersion.collectLatest { startObserving() }
        }
    }

    private suspend fun startObserving() {
        _uiState.value = ContainerBrowserUiState.Loading
        try {
            val containerName: String = if (containerId == null) {
                "Home"
            } else {
                containerRepository.getContainerById(containerId)?.name ?: "Container"
            }

            val containersFlow: Flow<List<Container>> = if (containerId == null) {
                containerRepository.observeRootContainers()
            } else {
                containerRepository.observeChildContainers(containerId)
            }

            val itemsFlow: Flow<List<Item>> = if (containerId == null) {
                itemRepository.observeItemUnsorted()
            } else {
                itemRepository.observeItemInContainer(containerId)
            }

            combine(containersFlow, itemsFlow, _controlState) { containers, items, controls ->
                buildReadyState(containerName, containers, items, controls)
            }.collect { state ->
                _uiState.value = state
            }
        } catch (e: Exception) {
            _uiState.value = ContainerBrowserUiState.Error(
                message = "Failed to load container contents",
                cause = e
            )
        }
    }

    override fun onIntent(intent: ContainerBrowserIntent) {
        when (intent) {
            is ContainerBrowserIntent.QueryChange ->
                _controlState.update { it.copy(query = intent.query) }

            is ContainerBrowserIntent.QuerySubmit ->
                _controlState.update { it.copy(query = intent.query) }

            ContainerBrowserIntent.ClearQuery ->
                _controlState.update { it.copy(query = "") }

            is ContainerBrowserIntent.FilterChange ->
                _controlState.update { it.copy(filter = intent.filter) }

            is ContainerBrowserIntent.SortChange ->
                _controlState.update { it.copy(sort = intent.sort) }

            is ContainerBrowserIntent.LayoutChange ->
                _controlState.update { it.copy(layout = intent.layout) }

            ContainerBrowserIntent.Retry ->
                _loadVersion.update { it + 1 }
        }
    }

    private fun buildReadyState(
        containerName: String,
        subcontainers: List<Container>,
        items: List<Item>,
        controls: ControlState
    ): ContainerBrowserUiState.Ready {
        val query = controls.query
        val filter = controls.filter
        val sort = controls.sort

        // Apply includeContainers / includeItems flags
        val candidateContainers = if (filter.includeContainers) subcontainers else emptyList()
        val candidateItems = if (filter.includeItems) items else emptyList()

        // Apply itemStatus filter
        val statusFilteredItems = if (filter.itemStatus != null) {
            candidateItems.filter { it.status == filter.itemStatus }
        } else {
            candidateItems
        }

        // Apply search query (name contains, case-insensitive)
        val queryLower = query.lowercase()
        val visibleContainers = if (query.isBlank()) {
            candidateContainers
        } else {
            candidateContainers.filter { it.name.lowercase().contains(queryLower) }
        }
        val visibleItems = if (query.isBlank()) {
            statusFilteredItems
        } else {
            statusFilteredItems.filter { item ->
                item.name.lowercase().contains(queryLower) ||
                        item.tags.any { tag -> tag.name.lowercase().contains(queryLower) }
            }
        }

        // Apply sort to both lists independently
        val sortedContainers = sortContainers(visibleContainers, sort)
        val sortedItems = sortItems(visibleItems, sort)

        // Determine empty state:
        // - EMPTY: raw lists are empty (container truly has nothing)
        // - NO_RESULTS: raw lists are non-empty but visible lists are empty (search/filter)
        // - NONE: visible lists have content
        val emptyState = when {
            subcontainers.isEmpty() && items.isEmpty() -> BrowserEmptyState.EMPTY
            sortedContainers.isEmpty() && sortedItems.isEmpty() -> BrowserEmptyState.NO_RESULTS
            else -> BrowserEmptyState.NONE
        }

        return ContainerBrowserUiState.Ready(
            containerId = containerId,
            containerName = containerName,
            subcontainers = subcontainers,
            items = items,
            visibleSubcontainers = sortedContainers,
            visibleItems = sortedItems,
            query = query,
            filter = filter,
            sort = sort,
            layout = controls.layout,
            emptyState = emptyState
        )
    }

    private fun sortContainers(containers: List<Container>, sort: BrowserSort): List<Container> =
        when (sort) {
            BrowserSort.NAME_ASC -> containers.sortedBy { it.name.lowercase() }
            BrowserSort.NAME_DESC -> containers.sortedByDescending { it.name.lowercase() }
            BrowserSort.CREATED_NEWEST -> containers.sortedByDescending { it.createdDate }
            BrowserSort.CREATED_OLDEST -> containers.sortedBy { it.createdDate }
        }

    private fun sortItems(items: List<Item>, sort: BrowserSort): List<Item> =
        when (sort) {
            BrowserSort.NAME_ASC -> items.sortedBy { it.name.lowercase() }
            BrowserSort.NAME_DESC -> items.sortedByDescending { it.name.lowercase() }
            BrowserSort.CREATED_NEWEST -> items.sortedByDescending { it.createdDate }
            BrowserSort.CREATED_OLDEST -> items.sortedBy { it.createdDate }
        }
}
