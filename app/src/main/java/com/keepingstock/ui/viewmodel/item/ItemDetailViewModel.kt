package com.keepingstock.ui.viewmodel.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.intents.ViewModelContract
import com.keepingstock.core.contracts.intents.item.ItemDetailIntent
import com.keepingstock.core.contracts.uistates.item.ItemDetailUiState
import com.keepingstock.data.repositories.ContainerRepository
import com.keepingstock.data.repositories.ItemRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Item Detail screen.
 *
 * Loads item info and resolves the parent container name on init. Exposes a [UiEffect] channel
 * that destinations should collect to handle navigation and snackbar side effects.
 *
 * @param itemId The item to display.
 * @param itemRepository Repository for item data.
 * @param containerRepository Repository for container data (used to resolve parent name).
 */
class ItemDetailViewModel(
    private val itemId: ItemId,
    private val itemRepository: ItemRepository,
    private val containerRepository: ContainerRepository
) : ViewModel(), ViewModelContract<ItemDetailUiState, ItemDetailIntent> {

    sealed interface UiEffect {
        data class ShowSnackbar(val message: String) : UiEffect
        data object NavigateBack : UiEffect
    }

    private val _effects = Channel<UiEffect>(Channel.BUFFERED)
    val effects: Flow<UiEffect> = _effects.receiveAsFlow()

    private var _loadedItem: Item? = null

    private val _uiState = MutableStateFlow<ItemDetailUiState>(ItemDetailUiState.Loading)
    override val uiState: StateFlow<ItemDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        _uiState.value = ItemDetailUiState.Loading
        try {
            val item = itemRepository.getItemById(itemId)
                ?: run {
                    _uiState.value = ItemDetailUiState.Error("Item not found")
                    return
                }
            _loadedItem = item

            val parentContainerName = item.containerId?.let { containerId ->
                containerRepository.getContainerById(containerId)?.name
            }

            _uiState.value = ItemDetailUiState.Ready(
                item = item,
                parentContainerName = parentContainerName
            )
        } catch (e: Exception) {
            _uiState.value = ItemDetailUiState.Error(
                message = "Failed to load item",
                cause = e
            )
        }
    }

    override fun onIntent(intent: ItemDetailIntent) {
        when (intent) {
            ItemDetailIntent.Retry ->
                viewModelScope.launch { load() }

            ItemDetailIntent.DeleteConfirmed ->
                viewModelScope.launch { deleteItem() }
        }
    }

    private suspend fun deleteItem() {
        val item = _loadedItem ?: return
        try {
            itemRepository.deleteItem(item)
            _effects.send(UiEffect.ShowSnackbar("Item deleted"))
            _effects.send(UiEffect.NavigateBack)
        } catch (e: Exception) {
            _effects.send(UiEffect.ShowSnackbar("Failed to delete item"))
        }
    }
}
