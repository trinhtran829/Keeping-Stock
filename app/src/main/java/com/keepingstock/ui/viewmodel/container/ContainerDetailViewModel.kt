package com.keepingstock.ui.viewmodel.container

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.intents.ViewModelContract
import com.keepingstock.core.contracts.intents.container.ContainerDetailIntent
import com.keepingstock.core.contracts.uistates.container.ContainerDetailUiState
import com.keepingstock.data.repositories.ContainerRepository
import com.keepingstock.data.repositories.ItemRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Container Detail screen.
 *
 * Loads container info, parent name, and child counts on init. Exposes a [UiEffect] channel
 * that destinations should collect to handle navigation and snackbar side effects.
 *
 * @param containerId The container to display.
 * @param containerRepository Repository for container data.
 * @param itemRepository Repository for item data.
 */
class ContainerDetailViewModel(
    private val containerId: ContainerId,
    private val containerRepository: ContainerRepository,
    private val itemRepository: ItemRepository
) : ViewModel(), ViewModelContract<ContainerDetailUiState, ContainerDetailIntent> {

    sealed interface UiEffect {
        data class ShowSnackbar(val message: String) : UiEffect
        data object NavigateBack : UiEffect
    }

    private val _effects = Channel<UiEffect>(Channel.BUFFERED)
    val effects: Flow<UiEffect> = _effects.receiveAsFlow()

    private var _loadedContainer: Container? = null

    private val _uiState = MutableStateFlow<ContainerDetailUiState>(ContainerDetailUiState.Loading)
    override val uiState: StateFlow<ContainerDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        _uiState.value = ContainerDetailUiState.Loading
        try {
            val container = containerRepository.getContainerById(containerId)
                ?: run {
                    _uiState.value = ContainerDetailUiState.Error("Container not found")
                    return
                }
            _loadedContainer = container

            val parentContainerName = container.parentContainerId?.let { parentId ->
                containerRepository.getContainerById(parentId)?.name
            }

            val subcontainerCount = containerRepository
                .observeChildContainers(containerId)
                .first()
                .size

            val itemCount = itemRepository
                .observeItemInContainer(containerId)
                .first()
                .size

            val canDelete = subcontainerCount == 0 && itemCount == 0

            _uiState.value = ContainerDetailUiState.Ready(
                container = container,
                parentContainerName = parentContainerName,
                subcontainerCount = subcontainerCount,
                itemCount = itemCount,
                canDelete = canDelete,
                deleteBlockedReason = if (canDelete) null
                else "Container must be empty before it can be deleted."
            )
        } catch (e: Exception) {
            _uiState.value = ContainerDetailUiState.Error(
                message = "Failed to load container",
                cause = e
            )
        }
    }

    override fun onIntent(intent: ContainerDetailIntent) {
        when (intent) {
            ContainerDetailIntent.Retry ->
                viewModelScope.launch { load() }

            ContainerDetailIntent.DeleteConfirmed ->
                viewModelScope.launch { deleteContainer() }
        }
    }

    private suspend fun deleteContainer() {
        val container = _loadedContainer ?: return
        try {
            containerRepository.deleteContainer(container)
            _effects.send(UiEffect.ShowSnackbar("Container deleted"))
            _effects.send(UiEffect.NavigateBack)
        } catch (e: Exception) {
            _effects.send(UiEffect.ShowSnackbar("Failed to delete container"))
        }
    }

    fun onMoveParentSelected(newParentId: ContainerId?) {
        viewModelScope.launch {
            val container = _loadedContainer ?: return@launch

            if (container.parentContainerId == newParentId) {
                return@launch
            }

            try {
                containerRepository.updateContainer(container.copy(parentContainerId = newParentId))
                _effects.send(UiEffect.ShowSnackbar("Moved container"))
                load()
            } catch (e: Exception) {
                _effects.send(UiEffect.ShowSnackbar("Failed to move container"))
            }
        }
    }
}
