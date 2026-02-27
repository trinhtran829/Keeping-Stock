package com.keepingstock.ui.viewmodel.media

import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.ContainerRepository
import com.keepingstock.core.contracts.QrService
import com.keepingstock.core.contracts.UiState
import com.keepingstock.testutil.MainDispatcherRule
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QrScanViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun scanSuccess_emitsLoadingThenSuccess() = runTest(mainDispatcherRule.testDispatcher) {
        val qrService = FakeQrService(
            scanResult = Result.success(ContainerId(7L)),
            scanDelayMs = 1_000L
        )
        val containerRepository = FakeContainerRepository(
            containers = mutableMapOf(
                7L to Container(
                    id = ContainerId(7L),
                    name = "Garage"
                )
            )
        )
        val viewModel = QrScanViewModel(qrService, containerRepository)

        viewModel.onContainerDetected(ContainerId(7L))
        runCurrent()
        assertTrue(viewModel.uiState.value is UiState.Loading)

        advanceTimeBy(1_000L)
        advanceUntilIdle()

        val success = viewModel.uiState.value as UiState.Success
        assertEquals(7L, success.data.response?.containerId)
        assertEquals("Garage", success.data.response?.containerName)
    }

    @Test
    fun scanMissingContainer_emitsError() = runTest(mainDispatcherRule.testDispatcher) {
        val qrService = FakeQrService(
            scanResult = Result.success(ContainerId(99L))
        )
        val containerRepository = FakeContainerRepository()
        val viewModel = QrScanViewModel(qrService, containerRepository)

        viewModel.onContainerDetected(ContainerId(99L))
        advanceUntilIdle()

        val error = viewModel.uiState.value as UiState.Error
        assertTrue(error.message.contains("99") || error.message.contains("not found"))
    }

    @Test
    fun reset_clearsStateAndAllowsNextScan() = runTest(mainDispatcherRule.testDispatcher) {
        val qrService = FakeQrService(
            scanResult = Result.success(ContainerId(1L)),
            scanDelayMs = 1_000L
        )
        val containerRepository = FakeContainerRepository(
            containers = mutableMapOf(
                1L to Container(
                    id = ContainerId(1L),
                    name = "Garage"
                )
            )
        )
        val viewModel = QrScanViewModel(qrService, containerRepository)

        viewModel.onContainerDetected(ContainerId(1L))
        viewModel.reset()

        val resetState = viewModel.uiState.value as UiState.Success
        assertNull(resetState.data.response)

        viewModel.onContainerDetected(ContainerId(1L))
        advanceUntilIdle()

        val finalState = viewModel.uiState.value as UiState.Success
        assertEquals(1L, finalState.data.response?.containerId)
    }
}

private class FakeQrService(
    private val scanResult: Result<ContainerId>,
    private val scanDelayMs: Long = 0L
) : QrService {
    var scanCallCount: Int = 0

    override suspend fun scanContainerQr(): ContainerId {
        scanCallCount += 1
        if (scanDelayMs > 0) delay(scanDelayMs)
        return scanResult.getOrThrow()
    }

    override fun generateContainerQr(containerId: ContainerId): String {
        return "keepingstock://container/${containerId.value}"
    }
}

private class FakeContainerRepository(
    val containers: MutableMap<Long, Container> = mutableMapOf()
) : ContainerRepository {

    override suspend fun getContainer(id: ContainerId): Container? = containers[id.value]

    override suspend fun getRootContainers(): List<Container> {
        return containers.values.filter { it.parentContainerId == null }.sortedBy { it.name }
    }

    override suspend fun getChildContainers(parentId: ContainerId): List<Container> {
        return containers.values.filter { it.parentContainerId == parentId }.sortedBy { it.name }
    }

    override suspend fun upsertContainer(container: Container): Container {
        containers[container.id.value] = container
        return container
    }

    override suspend fun deleteContainer(id: ContainerId) {
        containers.remove(id.value)
    }
}
