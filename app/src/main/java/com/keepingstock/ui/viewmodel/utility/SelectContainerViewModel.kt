package com.keepingstock.ui.viewmodel.utility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.ContainerRepository
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.Routes
import com.keepingstock.core.contracts.intents.ViewModelContract
import com.keepingstock.core.contracts.intents.utility.SelectContainerIntent
import com.keepingstock.core.contracts.uistates.utility.SelectContainerUiState
import com.keepingstock.data.repositories.ContainerRepositoryImpl
import com.keepingstock.data.repositories.ItemRepositoryImpl
import com.keepingstock.ui.navigation.NavResultKeys
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SelectContainerViewModel(
    private val subjectType: Routes.SubjectType,
    private val subjectId: Long,
    private val currentContainerId: ContainerId?,
    private val containerRepository: ContainerRepositoryImpl,
    private val itemRepository: ItemRepositoryImpl
) : ViewModel(), ViewModelContract<SelectContainerUiState, SelectContainerIntent> {

    sealed interface UiEffect {
        data class ShowSnackbar(val message: String) : UiEffect
        data class ReturnSelection(
            val resultKey: String = NavResultKeys.SELECTED_CONTAINER_ID,
            val selectedContainerId: ContainerId?
        ) : UiEffect
        data object NavigateBack : UiEffect
    }

    private val _effects = Channel<UiEffect>(Channel.BUFFERED)
    val effects: Flow<UiEffect> = _effects.receiveAsFlow()

    private val _uiState = MutableStateFlow<SelectContainerUiState>(SelectContainerUiState.Loading)
    override val uiState: StateFlow<SelectContainerUiState> = _uiState.asStateFlow()

    private var browsingParentId: ContainerId? = null
    private var selectedContainerId: ContainerId? = currentContainerId

    private val containerCache = mutableMapOf<Long, Container?>()
    private var subjectName: String = ""
    private var currentContainer: Container? = null

    init {
        viewModelScope.launch { initialize() }
    }

    override fun onIntent(intent: SelectContainerIntent) {
        when (intent) {
            is SelectContainerIntent.EnterContainer -> {
                browsingParentId = intent.containerId
                viewModelScope.launch { render() }
            }

            is SelectContainerIntent.ChangeSelection -> {
                selectedContainerId = intent.containerId
                viewModelScope.launch { render() }
            }

            is SelectContainerIntent.ClickBreadcrumb -> {
                browsingParentId = intent.containerId
                viewModelScope.launch { render() }
            }

            SelectContainerIntent.Cancel ->
                viewModelScope.launch { _effects.send(UiEffect.NavigateBack) }

            SelectContainerIntent.Confirm ->
                viewModelScope.launch { confirm() }
        }
    }

    private suspend fun initialize() {
        _uiState.value = SelectContainerUiState.Loading
        try {
            subjectName = loadSubjectName()
            currentContainer = getContainerCached(currentContainerId)
            render()
        } catch (e: Exception) {
            _uiState.value = SelectContainerUiState.Error("Failed to load destinations", e)
        }
    }

    private suspend fun render() {
        // Build breadcrumb for browsingParentId
        val breadcrumbs = buildBreadcrumb(browsingParentId)

        // Get subcontainers
        val containers: List<Container> =
            if (browsingParentId == null) {
                containerRepository.observeRootContainers().first()
            } else {
                containerRepository.observeChildContainers(browsingParentId!!).first()
            }

        // Build Display Rows
        val rows = containers
            .sortedBy { it.name.lowercase() }
            .map { subcontainer ->
                val disabled = (
                        (subjectType == Routes.SubjectType.Container) &&
                                (subcontainer.id.value == subjectId)
                        )
                SelectContainerUiState.Ready.ContainerSelectRow(
                    container = subcontainer,
                    isSelected = (selectedContainerId?.value == subcontainer.id.value),
                    isCurrent = (currentContainerId?.value == subcontainer.id.value),
                    isDisabled = disabled,
                )
            }
        
        // Build ready state
        _uiState.value = SelectContainerUiState.Ready(
            subjectType = subjectType,
            subjectId = subjectId,
            subjectName = subjectName,
            currentAssignedContainer = currentContainer,
            selectedDestinationContainer = selectedContainerId?.let { getContainerCached(it) },
            browsingContainer = browsingParentId?.let { getContainerCached(it) },
            breadcrumbs = breadcrumbs,
            rows = rows,
        )
    }

    private suspend fun getContainerCached(containerId: ContainerId?): Container? {
        if (containerId == null) return null

        if (containerCache.containsKey(containerId.value)) {
            return containerCache[containerId.value]
        }

        val container = containerRepository.getContainerById(containerId)
        containerCache[containerId.value] = container
        return container
    }

    private suspend fun loadSubjectName(): String {
        return when (subjectType) {
            Routes.SubjectType.Container -> {
                getContainerCached(ContainerId(subjectId))?.name
                    ?: "Unknown Container"
            }
            Routes.SubjectType.Item -> {
                itemRepository.getItemById(ItemId(subjectId))?.name
                    ?: "Unknown Item"
            }
        }
    }

    private suspend fun confirm() {
        // TODO: Validate to confirm not creating cycle?
        _effects.send(UiEffect.ReturnSelection(selectedContainerId = selectedContainerId))
    }

    private suspend fun buildBreadcrumb(
        parentId: ContainerId?
    ): List<SelectContainerUiState.Ready.Breadcrumb> {
        // Root
        if (parentId == null) {
            return listOf(SelectContainerUiState.Ready.Breadcrumb(id = null, label = "Root"))
        }

        // Walk up parents to root
        val chain = mutableListOf<Container>()
        var current: Container? = containerRepository.getContainerById(parentId)
        while (current != null) {
            chain.add(current)
            current = current.parentContainerId?.let { containerRepository.getContainerById(it) }
        }
        chain.reverse()

        // fencepost
        val crumbs = mutableListOf(
            SelectContainerUiState.Ready.Breadcrumb(id = null, label = "Root")
        )

        // Build crumb objects from path walk
        crumbs += chain.map {
            SelectContainerUiState.Ready.Breadcrumb(id = it.id, label = it.name)
        }

        return crumbs
    }
}