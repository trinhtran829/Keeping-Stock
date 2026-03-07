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

/**
 * ViewModel for the Select Container screen.
 *
 * Loads subject metadata, resolves the current and selected containers, maintains browsing state
 * for hierarchical navigation, and emits [UiEffect] values for navigation and result return.
 *
 * @param subjectType: Indicates whether the subject being moved is a container or an item.
 * @param subjectId: Identifier of the subject being moved.
 * @param currentContainerId: The subject's currently assigned container, or null for Root.
 * @param containerRepository: Repository used to resolve and browse container data.
 * @param itemRepository: Repository used to resolve item names when the subject is an item.
 */
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

    private var browsingParentId: ContainerId? = currentContainerId
    private var selectedContainerId: ContainerId? = currentContainerId

    private val containerCache = mutableMapOf<Long, Container?>()
    private var subjectName: String = ""
    private var currentContainer: Container? = null

    init {
        viewModelScope.launch { initialize() }
    }

    /**
     * Handles user intents emitted from the Select Container screen.
     *
     * Browsing and selection intents update local ViewModel state and trigger re-rendering, while
     * cancel and confirm intents emit one-time [UiEffect] side effects.
     *
     * @param intent: User intent describing the requested browsing, selection, or flow action.
     */
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

    /**
     * Performs initial loading for the Select Container flow.
     *
     * Resolves the subject display name, caches the currently assigned container, and renders the
     * initial ready state for the current browsing location.
     */
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

    /**
     * Recomputes the ready-state UI model for the current browsing and selection state.
     *
     * Resolves breadcrumbs, visible child containers, and row-level selection/disabled flags, then
     * publishes a new [SelectContainerUiState.Ready] snapshot.
     */
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

    /**
     * Resolves a container by id using an in-memory cache to avoid repeated repository lookups.
     *
     * Null container ids are treated as Root and return null without querying the repository.
     *
     * @param containerId: Identifier of the container to resolve, or null for Root.
     * @return The resolved [Container], or null when [containerId] is null or no container exists.
     */
    private suspend fun getContainerCached(containerId: ContainerId?): Container? {
        if (containerId == null) return null

        if (containerCache.containsKey(containerId.value)) {
            return containerCache[containerId.value]
        }

        val container = containerRepository.getContainerById(containerId)
        containerCache[containerId.value] = container
        return container
    }

    /**
     * Resolves the display name of the subject being moved.
     *
     * Container subjects are resolved through [containerRepository], while item subjects are resolved
     * through [itemRepository].
     *
     * @return The display name of the subject, or a fallback label when it cannot be resolved.
     */
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

    /**
     * Confirms the current destination selection and emits a one-time return result.
     *
     * The selected container id is returned through [UiEffect.ReturnSelection], where null represents
     * Root.
     */
    private suspend fun confirm() {
        // TODO: Validate to confirm not creating cycle?
        _effects.send(UiEffect.ReturnSelection(selectedContainerId = selectedContainerId))
    }

    /**
     * Builds the breadcrumb path from Root to the specified browsing container.
     *
     * The returned list always begins with a Root breadcrumb and includes each ancestor container in
     * order down to the current browsing location.
     *
     * @param parentId: Identifier of the currently browsed container, or null for Root.
     * @return The ordered breadcrumb path for the current browsing location.
     */
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