package com.keepingstock.ui.viewmodel.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.ImageLabelService
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.Tag
import com.keepingstock.core.contracts.TagId
import com.keepingstock.core.contracts.intents.ViewModelContract
import com.keepingstock.core.contracts.intents.item.AddEditItemIntent
import com.keepingstock.core.contracts.uistates.item.AddEditItemUiState
import com.keepingstock.data.entities.ItemStatus
import com.keepingstock.data.repositories.ContainerRepository
import com.keepingstock.data.repositories.ItemRepository
import com.keepingstock.data.repositories.TagRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel for the Add/Edit Item screen.
 *
 * Handles CREATE and EDIT modes:
 * - CREATE: [itemId] is null; form starts empty with optional [initialContainerId].
 * - EDIT: [itemId] is non-null; form is populated from the repository including tags.
 *
 * Tag management:
 * - All tags are loaded once from the repository on init and used for in-memory suggestions.
 * - New tags (those with negative IDs staged during editing) are created via the repo on save.
 * - On save, all tag associations are rebuilt (unlink all, then re-link selected).
 *
 * Exposes a [UiEffect] channel for destinations to collect navigation and snackbar events.
 *
 * @param itemId The item to edit, or null to create a new item.
 * @param initialContainerId Optional default container for CREATE mode.
 * @param itemRepository Repository for item data.
 * @param containerRepository Repository for container data.
 * @param tagRepository Repository for tag data.
 */
class AddEditItemViewModel(
    private val itemId: ItemId?,
    private val initialContainerId: ContainerId?,
    private val itemRepository: ItemRepository,
    private val containerRepository: ContainerRepository,
    private val tagRepository: TagRepository,
    private val imageLabelService: ImageLabelService,
) : ViewModel(), ViewModelContract<AddEditItemUiState, AddEditItemIntent> {

    sealed interface UiEffect {
        data class ShowSnackbar(val message: String) : UiEffect
        data object NavigateBack : UiEffect
    }

    private val _effects = Channel<UiEffect>(Channel.BUFFERED)
    val effects: Flow<UiEffect> = _effects.receiveAsFlow()

    // Original item kept for createdDate preservation in EDIT mode.
    private var _originalItem: Item? = null

    // All tags loaded from the repository; used as the source for in-memory suggestions.
    private var _allTags: List<Tag> = emptyList()

    private val _uiState = MutableStateFlow<AddEditItemUiState>(AddEditItemUiState.Loading)
    override val uiState: StateFlow<AddEditItemUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        _uiState.value = AddEditItemUiState.Loading
        try {
            val mode = if (itemId == null)
                AddEditItemUiState.Ready.Mode.CREATE
            else
                AddEditItemUiState.Ready.Mode.EDIT

            // Load all tags for in-memory suggestion filtering.
            _allTags = tagRepository.observeAllTags().first()

            // Build available parent (container) options: Root + all root containers
            val rootContainers = containerRepository.observeRootContainers().first()
            val parentOptions = buildList {
                add(AddEditItemUiState.Ready.ParentOption(id = null, name = "No Container"))
                rootContainers.mapTo(this) { container ->
                    AddEditItemUiState.Ready.ParentOption(id = container.id, name = container.name)
                }
            }

            val readyState = if (mode == AddEditItemUiState.Ready.Mode.EDIT) {
                val item = itemRepository.getItemById(itemId!!)
                    ?: run {
                        _uiState.value = AddEditItemUiState.Error("Item not found")
                        return
                    }
                _originalItem = item

                val containerName = when (item.containerId) {
                    null -> "No Container"
                    else -> containerRepository.getContainerById(item.containerId)?.name
                }

                AddEditItemUiState.Ready(
                    mode = mode,
                    itemId = item.id,
                    containerId = item.containerId,
                    containerName = containerName,
                    availableParents = parentOptions,
                    name = item.name,
                    description = item.description ?: "",
                    imageUri = item.imageUri,
                    status = item.status,
                    createdDate = item.createdDate,
                    checkoutDate = item.checkoutDate,
                    selectedTags = item.tags.sortedBy { it.name.lowercase() }
                )
            } else {
                val now = Date()
                val initialStatus = if (initialContainerId == null) ItemStatus.TAKEN_OUT else ItemStatus.STORED
                val initialCheckout = if (initialContainerId == null) now else null
                val containerName = when (initialContainerId) {
                    null -> "No Container"
                    else -> containerRepository.getContainerById(initialContainerId)?.name
                }

                AddEditItemUiState.Ready(
                    mode = mode,
                    itemId = null,
                    containerId = initialContainerId,
                    containerName = containerName,
                    availableParents = parentOptions,
                    name = "",
                    description = "",
                    imageUri = null,
                    status = initialStatus,
                    createdDate = now,
                    checkoutDate = initialCheckout
                )
            }

            _uiState.value = validate(readyState)
        } catch (e: Exception) {
            _uiState.value = AddEditItemUiState.Error(
                message = "Failed to load item",
                cause = e
            )
        }
    }

    override fun onIntent(intent: AddEditItemIntent) {
        val current = _uiState.value
        if (current !is AddEditItemUiState.Ready) {
            when (intent) {
                AddEditItemIntent.BackClicked,
                AddEditItemIntent.DiscardChangesConfirmed ->
                    viewModelScope.launch { _effects.send(UiEffect.NavigateBack) }
                else -> Unit
            }
            return
        }

        when (intent) {
            AddEditItemIntent.SaveClicked ->
                viewModelScope.launch { save(current) }

            is AddEditItemIntent.ContainerChanged ->
                viewModelScope.launch { applyContainerChanged(current, intent.containerId) }

            is AddEditItemIntent.ImagePicked ->
                viewModelScope.launch { applyImagePicked(current, intent.uriString) }

            AddEditItemIntent.RemoveImageClicked -> {
                val next = validate(reduceIntent(current, intent, _allTags))
                _uiState.value = next.copy(
                    tagRecommendations = emptyList(),
                    isRecommending = false
                )
            }

            AddEditItemIntent.RefreshRecommendations ->
                viewModelScope.launch { refreshRecommendations() }

            // Navigation/dialog intents handled by destination for MVP
            AddEditItemIntent.BackClicked,
            AddEditItemIntent.DiscardChangesConfirmed,
            AddEditItemIntent.DismissDiscardDialog,
            AddEditItemIntent.PickImageClicked -> Unit

            else -> {
                val next = validate(reduceIntent(current, intent, _allTags))
                _uiState.value = next
            }
        }
    }

    private suspend fun save(state: AddEditItemUiState.Ready) {
        val validated = validate(state)
        _uiState.value = validated

        if (validated.validation.nameError != null || validated.validation.containerError != null) return

        _uiState.value = validated.copy(isSaving = true)
        try {
            val savedItemId: ItemId = if (state.mode == AddEditItemUiState.Ready.Mode.CREATE) {
                val created = itemRepository.createItem(
                    name = state.name.trim(),
                    description = state.description.trim().takeIf { it.isNotBlank() },
                    imageUri = state.imageUri,
                    containerId = state.containerId
                )
                created.id
            } else {
                val original = _originalItem!!
                val updated = original.copy(
                    name = state.name.trim(),
                    description = state.description.trim().takeIf { it.isNotBlank() },
                    imageUri = state.imageUri,
                    containerId = state.containerId,
                    status = state.status,
                    checkoutDate = state.checkoutDate
                )
                itemRepository.updateItem(updated)
                updated.id
            }

            // Sync tags: unlink all existing, then re-link selected (creating new tags as needed).
            tagRepository.unlinkAllTagsFromItem(savedItemId)
            for (tag in state.selectedTags) {
                val actualTag = if (tag.id.value < 0) {
                    // Negative ID = tag staged during session; create or reuse via repo.
                    tagRepository.createTag(tag.name)
                } else {
                    tag
                }
                tagRepository.linkTagToItem(savedItemId, actualTag.id)
            }

            val message = if (state.mode == AddEditItemUiState.Ready.Mode.CREATE)
                "Item created" else "Item updated"
            _effects.send(UiEffect.ShowSnackbar(message))
            _effects.send(UiEffect.NavigateBack)
        } catch (e: Exception) {
            _uiState.value = validated.copy(isSaving = false)
            _effects.send(UiEffect.ShowSnackbar("Failed to save item"))
        }
    }

    private suspend fun applyContainerChanged(
        current: AddEditItemUiState.Ready,
        newContainerId: ContainerId?
    ) {
        if (!current.canChangeParent) return

        val containerName = when (newContainerId) {
            null -> "No Container"
            else -> containerRepository.getContainerById(newContainerId)?.name ?: "Unknown Container"
        }

        val nextState = if (newContainerId == null) {
            current.copy(
                containerId = null,
                containerName = containerName,
                status = ItemStatus.TAKEN_OUT,
                checkoutDate = current.checkoutDate ?: Date(),
                isDirty = true
            )
        } else {
            current.copy(
                containerId = newContainerId,
                containerName = containerName,
                isDirty = true
            )
        }

        _uiState.value = validate(nextState)
    }

    private suspend fun applyImagePicked(
        current: AddEditItemUiState.Ready,
        uriString: String
    ) {
        val nextState = validate(
            current.copy(
                imageUri = uriString,
                tagRecommendations = emptyList(),
                isRecommending = true,
                isDirty = true
            )
        )
        _uiState.value = nextState

        val recommendations = loadImageRecommendations(uriString, nextState.selectedTags)

        val latest = _uiState.value as? AddEditItemUiState.Ready ?: return
        _uiState.value = latest.copy(
            tagRecommendations = recommendations,
            isRecommending = false
        )
    }

    private suspend fun refreshRecommendations() {

    }
}

// ---------------------------------------------------------------------------
// Pure helpers (ported from AddEditItemDestination demo logic)
// ---------------------------------------------------------------------------

private fun validate(state: AddEditItemUiState.Ready): AddEditItemUiState.Ready {
    val nameError = if (state.name.trim().isBlank()) "Name is required" else null
    val containerError =
        if (state.containerId == null && state.status != ItemStatus.TAKEN_OUT)
            "Items outside a container must be marked Taken Out."
        else null
    return state.copy(
        validation = state.validation.copy(
            nameError = nameError,
            containerError = containerError
        )
    )
}

private fun reduceIntent(
    currentState: AddEditItemUiState.Ready,
    intent: AddEditItemIntent,
    allTags: List<Tag>
): AddEditItemUiState.Ready = when (intent) {
    is AddEditItemIntent.NameChanged ->
        currentState.copy(name = intent.value, isDirty = true)

    is AddEditItemIntent.DescriptionChanged ->
        currentState.copy(description = intent.value, isDirty = true)

    is AddEditItemIntent.ImagePicked ->
        currentState.copy(imageUri = intent.uriString, isDirty = true)

    AddEditItemIntent.RemoveImageClicked ->
        currentState.copy(imageUri = null, isDirty = true)

    is AddEditItemIntent.ContainerChanged -> currentState

    is AddEditItemIntent.StatusChanged -> {
        if (currentState.containerId == null) {
            // No container forces TAKEN_OUT; status toggle is disallowed.
            currentState
        } else if (intent.status == currentState.status) {
            currentState
        } else {
            when (intent.status) {
                ItemStatus.STORED ->
                    currentState.copy(status = ItemStatus.STORED, checkoutDate = null, isDirty = true)
                ItemStatus.TAKEN_OUT ->
                    currentState.copy(
                        status = ItemStatus.TAKEN_OUT,
                        checkoutDate = currentState.checkoutDate ?: Date(),
                        isDirty = true
                    )
            }
        }
    }

    // Tag intents delegated to the tag reducer.
    AddEditItemIntent.AddQueryAsTagClicked,
    is AddEditItemIntent.ExistingTagSelected,
    is AddEditItemIntent.QueryChanged,
    is AddEditItemIntent.RecommendedTagSelected,
    AddEditItemIntent.RefreshRecommendations,
    is AddEditItemIntent.RemoveTagClicked ->
        reduceTagIntent(currentState, intent, allTags)

    // Side-effect intents consumed without state change.
    AddEditItemIntent.DiscardChangesConfirmed,
    AddEditItemIntent.DismissDiscardDialog,
    AddEditItemIntent.PickImageClicked,
    AddEditItemIntent.BackClicked,
    AddEditItemIntent.SaveClicked -> currentState
}

private val ALLOWED_TAG_REGEX = Regex("""^[A-Za-z0-9 &-]+$""")

private fun normalizeForTyping(value: String): String =
    value.replace(Regex("\\s{2,}"), " ")

private fun normalizeForCommit(value: String): String =
    value.trim().replace(Regex("\\s+"), " ")

private fun reduceTagIntent(
    currentState: AddEditItemUiState.Ready,
    intent: AddEditItemIntent,
    allTags: List<Tag>
): AddEditItemUiState.Ready {

    fun updateSuggestions(state: AddEditItemUiState.Ready): AddEditItemUiState.Ready {
        val query = normalizeForTyping(state.tagQuery)
        if (query.isBlank()) {
            return state.copy(tagSuggestions = emptyList(), inputError = null, tagQuery = query)
        }

        val inputError = if (!ALLOWED_TAG_REGEX.matches(query))
            "Use only letters, numbers, spaces, '-', and '&'."
        else null

        val queryKey = query.lowercase()
        val selectedIds: Set<TagId> = state.selectedTags.map { it.id }.toSet()

        val newSuggestions = if (inputError == null) {
            allTags
                .filterNot { it.id in selectedIds }
                .filter { it.name.lowercase().contains(queryKey) }
                .sortedBy { it.name.lowercase() }
                .take(state.suggestionsLimit)
        } else emptyList()

        return state.copy(inputError = inputError, tagSuggestions = newSuggestions)
    }

    fun addTagByName(state: AddEditItemUiState.Ready, rawName: String): AddEditItemUiState.Ready {
        if (!state.canAddMore) return state

        val tagName = normalizeForCommit(rawName)
        if (!ALLOWED_TAG_REGEX.matches(tagName)) {
            return state.copy(inputError = "Use only letters, numbers, spaces, '-', and '&'.")
        }

        val tagKey = tagName.lowercase()

        // Reuse an existing tag if the normalized name matches.
        val existing = allTags.firstOrNull {
            normalizeForCommit(it.name).lowercase() == tagKey
        }

        // Reject duplicates already in selectedTags (case-insensitive).
        val currentSelectedKeys: Set<String> =
            state.selectedTags.map { normalizeForCommit(it.name).lowercase() }.toSet()
        if (tagKey in currentSelectedKeys) {
            return state.copy(tagQuery = "", tagSuggestions = emptyList(), inputError = null)
        }

        // Negative IDs mark tags staged for creation on save.
        val tagToAdd = existing ?: Tag(
            id = TagId(-tagKey.hashCode().toLong()),
            name = tagName
        )

        val newSelected = (state.selectedTags + tagToAdd).sortedBy { it.name.lowercase() }
        return state.copy(
            selectedTags = newSelected,
            tagQuery = "",
            tagSuggestions = emptyList(),
            inputError = null,
            isDirty = true
        )
    }

    return when (intent) {
        is AddEditItemIntent.QueryChanged -> {
            if (intent.value.isBlank())
                currentState.copy(tagQuery = "", tagSuggestions = emptyList(), inputError = null)
            else
                updateSuggestions(currentState.copy(tagQuery = intent.value))
        }

        AddEditItemIntent.AddQueryAsTagClicked ->
            addTagByName(currentState, currentState.tagQuery)

        is AddEditItemIntent.ExistingTagSelected -> {
            val tag = allTags.firstOrNull { it.id == intent.tagId } ?: return currentState
            addTagByName(currentState, tag.name)
        }

        is AddEditItemIntent.RemoveTagClicked -> {
            val newSelected = currentState.selectedTags.filterNot { it.id == intent.tagId }
            currentState.copy(selectedTags = newSelected, inputError = null, isDirty = true)
        }

        is AddEditItemIntent.RecommendedTagSelected ->
            addTagByName(currentState, intent.name)

        AddEditItemIntent.RefreshRecommendations ->
            currentState.copy(isRecommending = true)

        else -> currentState
    }
}
