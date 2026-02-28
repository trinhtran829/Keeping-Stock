package com.keepingstock.ui.viewmodel.container

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.intents.ViewModelContract
import com.keepingstock.core.contracts.intents.container.AddEditContainerIntent
import com.keepingstock.core.contracts.uistates.container.AddEditContainerUiState
import com.keepingstock.data.repositories.ContainerRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Add/Edit Container screen.
 *
 * Handles CREATE and EDIT modes:
 * - CREATE: [containerId] is null; form starts empty with optional [initialParentContainerId].
 * - EDIT: [containerId] is non-null; form is populated from the repository.
 *
 * Exposes a [UiEffect] channel for destinations to collect navigation and snackbar events.
 *
 * @param containerId The container to edit, or null to create a new container.
 * @param initialParentContainerId Optional default parent for CREATE mode.
 * @param containerRepository Repository for container data.
 */
class AddEditContainerViewModel(
    private val containerId: ContainerId?,
    private val initialParentContainerId: ContainerId?,
    private val containerRepository: ContainerRepository
) : ViewModel(), ViewModelContract<AddEditContainerUiState, AddEditContainerIntent> {

    sealed interface UiEffect {
        data class ShowSnackbar(val message: String) : UiEffect
        data object NavigateBack : UiEffect
    }

    private val _effects = Channel<UiEffect>(Channel.BUFFERED)
    val effects: Flow<UiEffect> = _effects.receiveAsFlow()

    // Original container kept for createdDate preservation in EDIT mode.
    private var _originalContainer: Container? = null

    private val _uiState = MutableStateFlow<AddEditContainerUiState>(AddEditContainerUiState.Loading)
    override val uiState: StateFlow<AddEditContainerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        _uiState.value = AddEditContainerUiState.Loading
        try {
            val mode = if (containerId == null)
                AddEditContainerUiState.Ready.Mode.CREATE
            else
                AddEditContainerUiState.Ready.Mode.EDIT

            // Build available parent options: Root + all root containers (excluding self in EDIT)
            val rootContainers = containerRepository.observeRootContainers().first()
            val parentOptions = buildList {
                add(AddEditContainerUiState.Ready.ParentOption(id = null, name = "Root"))
                rootContainers
                    .filter { it.id != containerId } // exclude self to avoid self-parenting
                    .mapTo(this) { container ->
                        AddEditContainerUiState.Ready.ParentOption(
                            id = container.id,
                            name = container.name
                        )
                    }
            }

            val readyState = if (mode == AddEditContainerUiState.Ready.Mode.EDIT) {
                val container = containerRepository.getContainerById(containerId!!)
                    ?: run {
                        _uiState.value = AddEditContainerUiState.Error("Container not found")
                        return
                    }
                _originalContainer = container

                val parentName = parentOptions.firstOrNull { it.id == container.parentContainerId }?.name

                AddEditContainerUiState.Ready(
                    mode = mode,
                    containerId = container.id,
                    parentContainerId = container.parentContainerId,
                    parentContainerName = parentName,
                    availableParents = parentOptions,
                    name = container.name,
                    description = container.description,
                    imageUri = container.imageUri,
                    isSaving = false,
                    isDirty = false,
                    canChangeParent = true
                )
            } else {
                val parentName = parentOptions.firstOrNull { it.id == initialParentContainerId }?.name

                AddEditContainerUiState.Ready(
                    mode = mode,
                    containerId = null,
                    parentContainerId = initialParentContainerId,
                    parentContainerName = parentName,
                    availableParents = parentOptions,
                    name = "",
                    description = null,
                    imageUri = null,
                    isSaving = false,
                    isDirty = false,
                    canChangeParent = true
                )
            }

            _uiState.value = readyState
        } catch (e: Exception) {
            _uiState.value = AddEditContainerUiState.Error(
                message = "Failed to load container",
                cause = e
            )
        }
    }

    override fun onIntent(intent: AddEditContainerIntent) {
        val current = _uiState.value
        if (current !is AddEditContainerUiState.Ready) {
            // Only allow navigation intents when not in Ready state.
            when (intent) {
                AddEditContainerIntent.BackClicked,
                AddEditContainerIntent.DiscardChangesConfirmed ->
                    viewModelScope.launch { _effects.send(UiEffect.NavigateBack) }
                else -> Unit
            }
            return
        }

        when (intent) {
            AddEditContainerIntent.SaveClicked ->
                viewModelScope.launch { save(current) }

            // Navigation/dialog intents handled by destination for MVP
            AddEditContainerIntent.BackClicked,
            AddEditContainerIntent.DiscardChangesConfirmed,
            AddEditContainerIntent.DismissDiscardDialog,
            AddEditContainerIntent.PickImageClicked -> Unit

            else -> _uiState.update { state ->
                if (state is AddEditContainerUiState.Ready)
                    validate(reduceIntent(state, intent, current.availableParents))
                else state
            }
        }
    }

    private suspend fun save(state: AddEditContainerUiState.Ready) {
        val validated = validate(state)
        _uiState.value = validated

        if (validated.validation.nameError != null) return

        _uiState.value = validated.copy(isSaving = true)
        try {
            if (state.mode == AddEditContainerUiState.Ready.Mode.CREATE) {
                containerRepository.createContainer(
                    name = state.name.trim(),
                    description = state.description?.trim()?.takeIf { it.isNotBlank() },
                    imageUri = state.imageUri,
                    parentContainerId = state.parentContainerId
                )
                _effects.send(UiEffect.ShowSnackbar("Container created"))
            } else {
                val original = _originalContainer!!
                containerRepository.updateContainer(
                    original.copy(
                        name = state.name.trim(),
                        description = state.description?.trim()?.takeIf { it.isNotBlank() },
                        imageUri = state.imageUri,
                        parentContainerId = state.parentContainerId
                    )
                )
                _effects.send(UiEffect.ShowSnackbar("Container updated"))
            }
            _effects.send(UiEffect.NavigateBack)
        } catch (e: Exception) {
            _uiState.value = validated.copy(isSaving = false)
            _effects.send(UiEffect.ShowSnackbar("Failed to save container"))
        }
    }
}

// ---------------------------------------------------------------------------
// Pure helpers (ported from AddEditContainerDestination demo logic)
// ---------------------------------------------------------------------------

private fun validate(
    state: AddEditContainerUiState.Ready
): AddEditContainerUiState.Ready {
    val nameError = if (state.name.trim().isBlank()) "Name is required." else null
    return state.copy(validation = state.validation.copy(nameError = nameError))
}

private fun reduceIntent(
    current: AddEditContainerUiState.Ready,
    intent: AddEditContainerIntent,
    availableParents: List<AddEditContainerUiState.Ready.ParentOption>
): AddEditContainerUiState.Ready = when (intent) {
    is AddEditContainerIntent.NameChanged ->
        current.copy(name = intent.value, isDirty = true)

    is AddEditContainerIntent.DescriptionChanged ->
        current.copy(description = intent.value, isDirty = true)

    is AddEditContainerIntent.ParentChanged -> {
        if (!current.canChangeParent) {
            current
        } else {
            val parentName = availableParents.firstOrNull { it.id == intent.parentId }?.name
            current.copy(
                parentContainerId = intent.parentId,
                parentContainerName = parentName,
                isDirty = true
            )
        }
    }

    is AddEditContainerIntent.ImagePicked ->
        current.copy(imageUri = intent.uriString, isDirty = true)

    AddEditContainerIntent.RemoveImageClicked ->
        current.copy(imageUri = null, isDirty = true)

    // These are handled as side effects; reducer returns current state unchanged.
    AddEditContainerIntent.SaveClicked,
    AddEditContainerIntent.BackClicked,
    AddEditContainerIntent.PickImageClicked,
    AddEditContainerIntent.DiscardChangesConfirmed,
    AddEditContainerIntent.DismissDiscardDialog -> current
}
