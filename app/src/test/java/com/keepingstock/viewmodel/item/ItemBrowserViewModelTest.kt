package com.keepingstock.viewmodel.item

import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemBrowserFilter
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.intents.item.ItemBrowserIntent
import com.keepingstock.core.contracts.uistates.item.ItemBrowserUiState
import com.keepingstock.data.entities.ItemStatus
import com.keepingstock.data.repositories.ItemRepository
import com.keepingstock.testutil.MainDispatcherRule
import com.keepingstock.ui.viewmodel.item.ItemBrowserViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.Date

class ItemBrowserViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun init_emitsSuccess() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeItemRepository().apply {
            setItems(listOf(testItem(id = 1L, name = "Drill")))
        }
        val viewModel = ItemBrowserViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ItemBrowserUiState.Success
        assertEquals(1, state.items.size)
        assertEquals("Drill", state.items.first().name)
    }

    @Test
    fun queryChange_filtersVisibleItems() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeItemRepository().apply {
            setItems(listOf(
                testItem(id = 1L, name = "Drill"),
                testItem(id = 2L, name = "Saw")
            ))
        }
        val viewModel = ItemBrowserViewModel(repository)
        advanceUntilIdle()

        viewModel.onIntent(ItemBrowserIntent.QueryChange("drill"))
        advanceUntilIdle()

        val state = viewModel.uiState.value as ItemBrowserUiState.Success
        assertEquals(2, state.items.size)           // raw list unchanged
        assertEquals(1, state.visibleItems.size)
        assertEquals("Drill", state.visibleItems.first().name)
    }

    @Test
    fun clearQuery_restoresAllVisibleItems() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeItemRepository().apply {
            setItems(listOf(
                testItem(id = 1L, name = "Drill"),
                testItem(id = 2L, name = "Saw")
            ))
        }
        val viewModel = ItemBrowserViewModel(repository)
        advanceUntilIdle()

        viewModel.onIntent(ItemBrowserIntent.QueryChange("drill"))
        advanceUntilIdle()
        assertEquals(1, (viewModel.uiState.value as ItemBrowserUiState.Success).visibleItems.size)

        viewModel.onIntent(ItemBrowserIntent.ClearQuery)
        advanceUntilIdle()
        assertEquals(2, (viewModel.uiState.value as ItemBrowserUiState.Success).visibleItems.size)
    }

    @Test
    fun filter_storedInRootOnly_filtersCorrectly() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeItemRepository().apply {
            setItems(listOf(
                testItem(id = 1L, name = "In Container", containerId = ContainerId(1L)),
                testItem(id = 2L, name = "Root Item")
            ))
        }
        val viewModel = ItemBrowserViewModel(repository)
        advanceUntilIdle()

        viewModel.onIntent(ItemBrowserIntent.FilterChange(ItemBrowserFilter(storedInRootOnly = true)))
        advanceUntilIdle()

        val state = viewModel.uiState.value as ItemBrowserUiState.Success
        assertEquals(1, state.visibleItems.size)
        assertEquals("Root Item", state.visibleItems.first().name)
    }

    @Test
    fun repositoryFailure_emitsError() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeItemRepository().apply {
            shouldThrow = IllegalStateException("load failed")
        }
        val viewModel = ItemBrowserViewModel(repository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ItemBrowserUiState.Error)
    }

    @Test
    fun retry_afterError_reloadsSuccessfully() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeItemRepository().apply {
            shouldThrow = IllegalStateException("temporary failure")
        }
        val viewModel = ItemBrowserViewModel(repository)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is ItemBrowserUiState.Error)

        repository.shouldThrow = null
        repository.setItems(listOf(testItem(id = 1L, name = "Hammer")))

        viewModel.onIntent(ItemBrowserIntent.Retry)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ItemBrowserUiState.Success
        assertEquals("Hammer", state.items.first().name)
    }
}

// ---------------------------------------------------------------------------
// Fake repository backed by MutableStateFlow for testability
// ---------------------------------------------------------------------------

private class FakeItemRepository : ItemRepository {
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    var shouldThrow: Throwable? = null

    fun setItems(items: List<Item>) { _items.value = items }

    override fun observeItem(): Flow<List<Item>> = flow {
        shouldThrow?.let { throw it }
        emitAll(_items)
    }

    override fun observeItemInContainer(containerId: ContainerId): Flow<List<Item>> =
        _items.map { list -> list.filter { it.containerId == containerId } }

    override fun observeItemUnsorted(): Flow<List<Item>> =
        _items.map { list -> list.filter { it.containerId == null } }

    override fun searchItemsByNameOrTag(query: String): Flow<List<Item>> =
        _items.map { list -> list.filter { it.name.contains(query, ignoreCase = true) } }

    override fun searchItemsByName(query: String): Flow<List<Item>> =
        _items.map { list -> list.filter { it.name.contains(query, ignoreCase = true) } }

    override fun searchItemsByTagName(query: String): Flow<List<Item>> = flowOf(emptyList())

    override suspend fun createItem(
        name: String, description: String?, imageUri: String?, containerId: ContainerId?
    ): Item = error("unused in tests")

    override suspend fun updateItem(item: Item) = error("unused in tests")

    override suspend fun updateItemStatus(itemId: ItemId, status: ItemStatus) =
        error("unused in tests")

    override suspend fun deleteItem(item: Item) = error("unused in tests")

    override suspend fun getItemById(itemId: ItemId): Item? = null

    override fun observeItemById(itemId: ItemId): Flow<Item?> = flowOf(null)
}

// ---------------------------------------------------------------------------
// Test helpers
// ---------------------------------------------------------------------------

private fun testItem(
    id: Long,
    name: String,
    containerId: ContainerId? = null
): Item = Item(
    id = ItemId(id),
    name = name,
    containerId = containerId,
    status = if (containerId == null) ItemStatus.TAKEN_OUT else ItemStatus.STORED,
    createdDate = Date()
)
