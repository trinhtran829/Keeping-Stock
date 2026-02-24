package com.keepingstock.viewmodel.item

import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.ItemRepository
import com.keepingstock.core.contracts.Tag
import com.keepingstock.core.contracts.UiState
import com.keepingstock.data.entities.ItemStatus
import com.keepingstock.testutil.MainDispatcherRule
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ItemBrowserViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun searchQuery_emitsSuccessFromSearch() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeItemRepository().apply {
            searchResultsByQuery["drill"] = listOf(
                testItem(id = 1L, name = "Drill")
            )
        }
        val viewModel = ItemBrowserViewModel(repository)

        viewModel.updateSearchQuery("drill")
        advanceTimeBy(301L)
        advanceUntilIdle()

        val successState = viewModel.uiState.value as UiState.Success
        assertEquals(1, successState.data.items.size)
        assertEquals("Drill", successState.data.items.first().name)
        assertEquals(1, repository.searchCallCount)
    }

    @Test
    fun containerFilter_emitsSuccessFromContainerItems() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeItemRepository().apply {
            containerResults[10L] = listOf(
                testItem(id = 2L, name = "Saw", containerId = ContainerId(10L))
            )
        }
        val viewModel = ItemBrowserViewModel(repository)

        viewModel.setContainer(ContainerId(10L))
        advanceTimeBy(301L)
        advanceUntilIdle()

        val successState = viewModel.uiState.value as UiState.Success
        assertEquals(1, successState.data.items.size)
        assertEquals(10L, successState.data.containerId?.value)
        assertEquals(1, repository.containerCallCount)
    }

    @Test
    fun repositoryFailure_emitsError() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeItemRepository().apply {
            searchErrorsByQuery["broken"] = IllegalStateException("search failed")
        }
        val viewModel = ItemBrowserViewModel(repository)

        viewModel.updateSearchQuery("broken")
        advanceTimeBy(301L)
        advanceUntilIdle()

        val errorState = viewModel.uiState.value as UiState.Error
        assertTrue(errorState.cause is IllegalStateException)
    }

    @Test
    fun rapidQueryChanges_onlyLatestResultWins() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeItemRepository().apply {
            searchDelayByQuery["old"] = 5_000L
            searchDelayByQuery["new"] = 10L
            searchResultsByQuery["old"] = listOf(testItem(id = 1L, name = "Old"))
            searchResultsByQuery["new"] = listOf(testItem(id = 2L, name = "New"))
        }
        val viewModel = ItemBrowserViewModel(repository)

        viewModel.updateSearchQuery("old")
        advanceTimeBy(301L)
        runCurrent()

        viewModel.updateSearchQuery("new")
        advanceTimeBy(301L)
        advanceUntilIdle()

        val successState = viewModel.uiState.value as UiState.Success
        assertEquals("New", successState.data.items.first().name)
        assertTrue(repository.startedSearchQueries.contains("old"))
        assertTrue(repository.completedSearchQueries.contains("new"))
        assertTrue(!repository.completedSearchQueries.contains("old"))
    }

    @Test
    fun retry_afterError_reloadsLastRequest() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeItemRepository().apply {
            searchErrorsByQuery["hammer"] = IllegalStateException("temporary failure")
        }
        val viewModel = ItemBrowserViewModel(repository)

        viewModel.updateSearchQuery("hammer")
        advanceTimeBy(301L)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is UiState.Error)

        repository.searchErrorsByQuery.remove("hammer")
        repository.searchResultsByQuery["hammer"] = listOf(
            testItem(id = 3L, name = "Hammer")
        )

        viewModel.retry()
        advanceUntilIdle()

        val successState = viewModel.uiState.value as UiState.Success
        assertEquals("Hammer", successState.data.items.first().name)
        assertTrue(repository.searchCallCount >= 2)
    }
}

private class FakeItemRepository : ItemRepository {
    val searchResultsByQuery = mutableMapOf<String, List<Item>>()
    val searchErrorsByQuery = mutableMapOf<String, Throwable>()
    val searchDelayByQuery = mutableMapOf<String, Long>()
    val containerResults = mutableMapOf<Long, List<Item>>()

    var searchCallCount: Int = 0
    var containerCallCount: Int = 0

    val startedSearchQueries = mutableListOf<String>()
    val completedSearchQueries = mutableListOf<String>()

    override suspend fun getItem(id: ItemId): Item? = null

    override suspend fun searchItems(query: String, tags: List<Tag>): List<Item> {
        searchCallCount += 1
        startedSearchQueries += query

        searchErrorsByQuery[query]?.let { throw it }

        val delayMs = searchDelayByQuery[query] ?: 0L
        if (delayMs > 0) delay(delayMs)

        val result = searchResultsByQuery[query] ?: emptyList()
        completedSearchQueries += query
        return result
    }

    override suspend fun getItemsInContainer(containerId: ContainerId): List<Item> {
        containerCallCount += 1
        return containerResults[containerId.value] ?: emptyList()
    }

    override suspend fun upsertItem(item: Item): Item = item

    override suspend fun deleteItem(id: ItemId) = Unit
}

private fun testItem(
    id: Long,
    name: String,
    containerId: ContainerId? = null
): Item {
    return Item(
        id = ItemId(id),
        name = name,
        containerId = containerId,
        status = if (containerId == null) ItemStatus.TAKEN_OUT else ItemStatus.STORED
    )
}
