package com.keepingstock.ui.viewmodel.media

import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.data.repositories.ContainerRepository
import com.keepingstock.core.contracts.QrService
import com.keepingstock.core.contracts.UiState
import com.keepingstock.testutil.MainDispatcherRule
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
            scanResult = Result.success(ContainerId(7L))
        )
        val containerRepository = FakeContainerRepository(
            containers = mutableMapOf(
                7L to Container(
                    id = ContainerId(7L),
                    name = "Garage"
                )
            ),
            delayMs = 1_000L
        )
        val viewModel = QrScanViewModel(qrService, containerRepository)

        viewModel.onContainerDetected(ContainerId(7L))
        runCurrent()
        assertTrue("State should be Loading after onContainerDetected", viewModel.uiState.value is UiState.Loading)

        advanceTimeBy(1_000L)
        runCurrent()

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
            scanResult = Result.success(ContainerId(1L))
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
        advanceUntilIdle()
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
    val containers: MutableMap<Long, Container> = mutableMapOf(),
    private val delayMs: Long = 0L
) : ContainerRepository {

    override suspend fun createContainer(
        name: String,
        description: String?,
        imageUri: String?,
        parentContainerId: ContainerId?
    ): Container {
        TODO("Not yet implemented")
    }

    override suspend fun updateContainer(container: Container) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteContainer(container: Container) {
        TODO("Not yet implemented")
    }

    override suspend fun getContainerById(containerId: ContainerId): Container? {
        if (delayMs > 0) delay(delayMs)
        return containers[containerId.value]
    }

    override fun observeContainerById(containerId: ContainerId): Flow<Container?> {
        return flowOf(containers[containerId.value])
    }

    override fun observeRootContainers(): Flow<List<Container>> {
        return flowOf(containers.values.filter { it.parentContainerId == null }.sortedBy { it.name })
    }

    override fun observeChildContainers(parentContainerId: ContainerId): Flow<List<Container>> {
        return flowOf(containers.values.filter { it.parentContainerId == parentContainerId }.sortedBy { it.name })
    }

    override fun searchChildContainers(
        parentContainerId: ContainerId?,
        query: String
    ): Flow<List<Container>> {
        TODO("Not yet implemented")
    }
}
