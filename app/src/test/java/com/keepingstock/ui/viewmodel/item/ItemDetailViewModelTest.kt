package com.keepingstock.ui.viewmodel.item

import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.intents.item.ItemDetailIntent
import com.keepingstock.core.contracts.uistates.item.ItemDetailUiState
import com.keepingstock.data.entities.ItemStatus
import com.keepingstock.data.repositories.ContainerRepository
import com.keepingstock.data.repositories.ItemRepository
import com.keepingstock.testutil.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.Date

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ItemDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ---------------------------------------------------------------------------
    // Load tests
    // ---------------------------------------------------------------------------

    @Test
    fun init_itemFound_emitsReady() = runTest(mainDispatcherRule.testDispatcher) {
        val item = testItem(id = 1L, status = ItemStatus.STORED)
        val (vm, _) = buildViewModel(item = item)
        advanceUntilIdle()

        val state = vm.uiState.value as ItemDetailUiState.Ready
        assertEquals(ItemId(1L), state.item.id)
        assertEquals("Test Item", state.item.name)
    }

    @Test
    fun init_itemNotFound_emitsError() = runTest(mainDispatcherRule.testDispatcher) {
        val (vm, _) = buildViewModel(item = null)
        advanceUntilIdle()

        assertTrue(vm.uiState.value is ItemDetailUiState.Error)
    }

    // ---------------------------------------------------------------------------
    // Toggle: STORED → TAKEN_OUT
    // ---------------------------------------------------------------------------

    @Test
    fun toggleCheckout_stored_toTakenOut_updatesStatusInState() =
        runTest(mainDispatcherRule.testDispatcher) {
            val item = testItem(id = 1L, status = ItemStatus.STORED)
            val (vm, _) = buildViewModel(item = item)
            advanceUntilIdle()

            vm.onIntent(ItemDetailIntent.ToggleCheckout(ItemStatus.TAKEN_OUT))
            advanceUntilIdle()

            val state = vm.uiState.value as ItemDetailUiState.Ready
            assertEquals(ItemStatus.TAKEN_OUT, state.item.status)
        }

    @Test
    fun toggleCheckout_stored_toTakenOut_setsCheckoutDate() =
        runTest(mainDispatcherRule.testDispatcher) {
            val item = testItem(id = 1L, status = ItemStatus.STORED)
            val (vm, _) = buildViewModel(item = item)
            advanceUntilIdle()

            vm.onIntent(ItemDetailIntent.ToggleCheckout(ItemStatus.TAKEN_OUT))
            advanceUntilIdle()

            val state = vm.uiState.value as ItemDetailUiState.Ready
            assertNotNull(state.item.checkoutDate)
        }

    @Test
    fun toggleCheckout_stored_toTakenOut_sendsCheckedOutSnackbar() =
        runTest(mainDispatcherRule.testDispatcher) {
            val item = testItem(id = 1L, status = ItemStatus.STORED)
            val (vm, _) = buildViewModel(item = item)
            advanceUntilIdle()

            val effects = mutableListOf<ItemDetailViewModel.UiEffect>()
            val job = launch { vm.effects.collect { effects.add(it) } }

            vm.onIntent(ItemDetailIntent.ToggleCheckout(ItemStatus.TAKEN_OUT))
            advanceUntilIdle()
            job.cancel()

            assertEquals(1, effects.size)
            assertEquals(
                "Item checked out",
                (effects[0] as ItemDetailViewModel.UiEffect.ShowSnackbar).message
            )
        }

    // ---------------------------------------------------------------------------
    // Toggle: TAKEN_OUT → STORED
    // ---------------------------------------------------------------------------

    @Test
    fun toggleCheckout_takenOut_toStored_updatesStatusInState() =
        runTest(mainDispatcherRule.testDispatcher) {
            val item = testItem(id = 1L, status = ItemStatus.TAKEN_OUT, checkoutDate = Date())
            val (vm, _) = buildViewModel(item = item)
            advanceUntilIdle()

            vm.onIntent(ItemDetailIntent.ToggleCheckout(ItemStatus.STORED))
            advanceUntilIdle()

            val state = vm.uiState.value as ItemDetailUiState.Ready
            assertEquals(ItemStatus.STORED, state.item.status)
        }

    @Test
    fun toggleCheckout_takenOut_toStored_clearsCheckoutDate() =
        runTest(mainDispatcherRule.testDispatcher) {
            val item = testItem(id = 1L, status = ItemStatus.TAKEN_OUT, checkoutDate = Date())
            val (vm, _) = buildViewModel(item = item)
            advanceUntilIdle()

            vm.onIntent(ItemDetailIntent.ToggleCheckout(ItemStatus.STORED))
            advanceUntilIdle()

            val state = vm.uiState.value as ItemDetailUiState.Ready
            assertNull(state.item.checkoutDate)
        }

    @Test
    fun toggleCheckout_takenOut_toStored_sendsStoredSnackbar() =
        runTest(mainDispatcherRule.testDispatcher) {
            val item = testItem(id = 1L, status = ItemStatus.TAKEN_OUT, checkoutDate = Date())
            val (vm, _) = buildViewModel(item = item)
            advanceUntilIdle()

            val effects = mutableListOf<ItemDetailViewModel.UiEffect>()
            val job = launch { vm.effects.collect { effects.add(it) } }

            vm.onIntent(ItemDetailIntent.ToggleCheckout(ItemStatus.STORED))
            advanceUntilIdle()
            job.cancel()

            assertEquals(1, effects.size)
            assertEquals(
                "Item stored",
                (effects[0] as ItemDetailViewModel.UiEffect.ShowSnackbar).message
            )
        }

    // ---------------------------------------------------------------------------
    // Error & guard
    // ---------------------------------------------------------------------------

    @Test
    fun toggleCheckout_repositoryThrows_sendsErrorSnackbar() =
        runTest(mainDispatcherRule.testDispatcher) {
            val item = testItem(id = 1L, status = ItemStatus.STORED)
            val (vm, repo) = buildViewModel(item = item)
            advanceUntilIdle()

            val stateBeforeToggle = vm.uiState.value

            val effects = mutableListOf<ItemDetailViewModel.UiEffect>()
            val job = launch { vm.effects.collect { effects.add(it) } }

            repo.toggleShouldThrow = IllegalStateException("db error")
            vm.onIntent(ItemDetailIntent.ToggleCheckout(ItemStatus.TAKEN_OUT))
            advanceUntilIdle()
            job.cancel()

            assertEquals(1, effects.size)
            assertEquals(
                "Failed to update status",
                (effects[0] as ItemDetailViewModel.UiEffect.ShowSnackbar).message
            )
            // State should be unchanged after failure
            assertEquals(stateBeforeToggle, vm.uiState.value)
        }

    @Test
    fun toggleCheckout_whenNotReady_isNoOp() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Gate getItemById so load() never completes — keeping state in Loading
            // while the ToggleCheckout coroutine runs.
            val loadGate = CompletableDeferred<Unit>()
            val item = testItem(id = 1L, status = ItemStatus.STORED)
            val repo = FakeItemRepository(storedItem = item, loadGate = loadGate)
            val vm = ItemDetailViewModel(
                itemId = item.id,
                itemRepository = repo,
                containerRepository = FakeContainerRepository()
            )

            val effects = mutableListOf<ItemDetailViewModel.UiEffect>()
            val job = launch { vm.effects.collect { effects.add(it) } }

            vm.onIntent(ItemDetailIntent.ToggleCheckout(ItemStatus.TAKEN_OUT))
            // advanceUntilIdle runs toggleCheckout (state=Loading → early return)
            // and leaves load() suspended on loadGate
            advanceUntilIdle()
            job.cancel()
            loadGate.cancel() // unblock so the test coroutine can clean up

            assertTrue(
                "Expected no effects when ToggleCheckout fires while state is Loading",
                effects.isEmpty()
            )
        }
}

// ---------------------------------------------------------------------------
// Test setup helper
// ---------------------------------------------------------------------------

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
private fun buildViewModel(
    item: Item?,
    container: Container? = null
): Pair<ItemDetailViewModel, FakeItemRepository> {
    val repo = FakeItemRepository(storedItem = item)
    val containerRepo = FakeContainerRepository(container = container)
    val vm = ItemDetailViewModel(
        itemId = item?.id ?: ItemId(1L),
        itemRepository = repo,
        containerRepository = containerRepo
    )
    return vm to repo
}

// ---------------------------------------------------------------------------
// Fake repositories
// ---------------------------------------------------------------------------

private class FakeItemRepository(
    private val storedItem: Item? = null,
    private val loadGate: CompletableDeferred<Unit>? = null
) : ItemRepository {
    var toggleShouldThrow: Throwable? = null

    override suspend fun getItemById(itemId: ItemId): Item? {
        loadGate?.await()
        return storedItem
    }

    override suspend fun updateItemStatus(itemId: ItemId, status: ItemStatus) {
        toggleShouldThrow?.let { throw it }
    }

    override fun observeItem(): Flow<List<Item>> = flowOf(emptyList())
    override fun observeItemInContainer(containerId: ContainerId): Flow<List<Item>> = flowOf(emptyList())
    override fun observeItemUnsorted(): Flow<List<Item>> = flowOf(emptyList())
    override fun searchItemsByNameOrTag(query: String): Flow<List<Item>> = flowOf(emptyList())
    override fun searchItemsByName(query: String): Flow<List<Item>> = flowOf(emptyList())
    override fun searchItemsByTagName(query: String): Flow<List<Item>> = flowOf(emptyList())
    override suspend fun createItem(name: String, description: String?, imageUri: String?, containerId: ContainerId?): Item = error("unused in tests")
    override suspend fun updateItem(item: Item) = error("unused in tests")
    override suspend fun deleteItem(item: Item) = error("unused in tests")
}

private class FakeContainerRepository(
    private val container: Container? = null
) : ContainerRepository {
    override suspend fun getContainerById(containerId: ContainerId): Container? = container
    override suspend fun createContainer(name: String, description: String?, imageUri: String?, parentContainerId: ContainerId?): Container = error("unused in tests")
    override suspend fun updateContainer(container: Container) = error("unused in tests")
    override suspend fun deleteContainer(container: Container) = error("unused in tests")
    override fun observeRootContainers(): Flow<List<Container>> = flowOf(emptyList())
    override fun observeChildContainers(parentContainerId: ContainerId): Flow<List<Container>> = flowOf(emptyList())
    override fun searchChildContainers(parentContainerId: ContainerId?, query: String): Flow<List<Container>> = flowOf(emptyList())
}

// ---------------------------------------------------------------------------
// Test helpers
// ---------------------------------------------------------------------------

private fun testItem(
    id: Long = 1L,
    status: ItemStatus = ItemStatus.STORED,
    checkoutDate: Date? = null
): Item = Item(
    id = ItemId(id),
    name = "Test Item",
    containerId = if (status == ItemStatus.STORED) ContainerId(10L) else null,
    status = status,
    checkoutDate = checkoutDate,
    createdDate = Date()
)
