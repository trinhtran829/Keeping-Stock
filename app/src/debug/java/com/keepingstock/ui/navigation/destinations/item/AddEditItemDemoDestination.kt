package com.keepingstock.ui.navigation.destinations.item


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.Routes
import com.keepingstock.core.contracts.Tag
import com.keepingstock.core.contracts.TagId
import com.keepingstock.core.contracts.intents.item.AddEditItemIntent
import com.keepingstock.core.contracts.uistates.item.AddEditItemUiState
import com.keepingstock.data.entities.ItemStatus
import com.keepingstock.ui.components.navigation.ChipOption
import com.keepingstock.ui.components.navigation.DemoMode
import com.keepingstock.ui.components.navigation.DemoModeToggleRow
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.navigation.containerIdOrNull
import com.keepingstock.ui.navigation.itemIdOrNull
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.item.AddEditItemScreen
import java.util.Date

/**
 * Registers the Add/Edit Item destination and wires demo navigation arguments, demo state,
 * and screen callbacks.
 *
 * Navigation args:
 * - [Routes.Args.ITEM_ID]: when present, screen is in EDIT mode; otherwise CREATE mode.
 * - [Routes.Args.CONTAINER_ID]: optional initial parent for CREATE mode (or preselect in EDIT).
 *
 * Current behavior (pre-ViewModel):
 * - Creates a demo [AddEditItemUiState.Ready] via [demoInitialUiState].
 * - Uses [AddEditItemDemoController] to reduce UI intents into state changes and to perform
 *   side effects (snackbar + popBackStack) on save.
 *
 * @param deps: Navigation and UI dependencies (NavController, top bar updater, snackbar helper).
 */
internal fun NavGraphBuilder.addAddEditItemDemoDestination(
    deps: NavDeps
) {
    composable(
        route = NavRoute.AddEditItemDebug.route,
        arguments = listOf(
            navArgument(Routes.Args.ITEM_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(Routes.Args.CONTAINER_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val args = backStackEntry.arguments
        val itemId = args?.itemIdOrNull(Routes.Args.ITEM_ID)
        val containerId = args?.containerIdOrNull(Routes.Args.CONTAINER_ID)

        var demoMode by rememberSaveable(itemId?.value) {
            mutableStateOf(DemoMode.READY)
        }

        val knownTags = remember {
            listOf(
                Tag(TagId(1L), "Tools"),
                Tag(TagId(2L), "Electrical"),
                Tag(TagId(3L), "Plumbing"),
                Tag(TagId(4L), "Outdoor"),
                Tag(TagId(5L), "Spare"),
                Tag(TagId(6L), "Fragile"),
                Tag(TagId(7L), "Seasonal"),
                Tag(TagId(8L), "Automotive")
            )
        }

        val parentOptions = remember {
            listOf(
                AddEditItemUiState.Ready.ParentOption(null, "Root"),
                AddEditItemUiState.Ready.ParentOption(ContainerId(1L), "Garage"),
                AddEditItemUiState.Ready.ParentOption(ContainerId(2L), "Kitchen"),
                AddEditItemUiState.Ready.ParentOption(ContainerId(3L), "Shed")
            )
        }

        var readyState by remember(itemId, containerId) {
            mutableStateOf(
                demoInitialUiState(
                    itemId = itemId,
                    containerId = containerId,
                    parentOptions = parentOptions,
                    knownTags = knownTags
                )
            )
        }

        var uiState by remember(itemId, containerId, demoMode) {
            mutableStateOf(
                when (demoMode) {
                    DemoMode.LOADING -> AddEditItemUiState.Loading
                    DemoMode.ERROR, DemoMode.EMPTY ->
                        AddEditItemUiState.Error("Demo error loading item.")
                    DemoMode.READY, DemoMode.POPULATED -> readyState
                }
            )
        }

        // Updates the global top bar whenever the destination arguments change.
        LaunchedEffect(itemId, containerId) {
            deps.onTopBarChange(addEditItemTopBarConfig(uiState))
        }

        val controller = remember(deps, knownTags) {
            AddEditItemDemoController(
                deps = deps,
                parentOptions = parentOptions,
                knownTags = knownTags,
                getUiState = { uiState },
                setUiState = { uiState = it }
            )
        }

        Column(Modifier.fillMaxSize()) {
            DemoModeToggleRow(
                title = "Select demo mode:",
                selected = demoMode,
                options = listOf(
                    ChipOption(DemoMode.READY, "Ready"),
                    ChipOption(DemoMode.LOADING, "Loading"),
                    ChipOption(DemoMode.ERROR, "Error")
                ),
                onSelect = { demoMode = it }
            )

            AddEditItemScreen(
                uiState = uiState,
                onIntent = { intent ->
                    /**
                     * Demo behavior:
                     * - When Ready, forward intents to the demo controller.
                     * - When Loading/Error, ignore edit intents and only allow navigation intents.
                     */
                    if (uiState is AddEditItemUiState.Ready) {
                        controller.onIntent(intent)
                    } else {
                        when (intent) {
                            AddEditItemIntent.BackClicked,
                            AddEditItemIntent.DiscardChangesConfirmed ->
                                deps.navController.popBackStack()
                            else -> Unit
                        }
                    }
                },
                onNavigateBack = { deps.navController.popBackStack() }
            )
        }
    }
}

/**
 * Builds top bar configuration for the AddEdit Item Screen.
 *
 * @param uiState: The current UI state for the Add/Edit Item screen.
 * @return: A [TopBarConfig] with title and back button visibility.
 */
private fun addEditItemTopBarConfig(uiState: AddEditItemUiState): TopBarConfig {
    val title = when (uiState) {
        AddEditItemUiState.Loading -> "Loading"
        is AddEditItemUiState.Error -> "Item"
        is AddEditItemUiState.Ready ->
            if (uiState.mode == AddEditItemUiState.Ready.Mode.CREATE) "Add Item" else "Edit Item"
    }

    return TopBarConfig(title = title, showBack = true)
}

/**
 * A simple demo controller that centralizes intent handling and demo-only side effects.
 *
 * This exists to keep the destination composable thinner while the real ViewModel is introduced.
 *
 * The final implementation should move:
 * - state ownership (UiState),
 * - validation,
 * - saving,
 * - and side-effect emission (snackbar/navigation)
 * into the ViewModel layer.
 *
 * :param deps: Navigation and snackbar helpers.
 * :param parentOptions: Demo list of available parent containers for selection.
 * :param knownTags: Set of tags suitable for demo purposes
 * :param getUiState: Getter for the current [AddEditItemUiState.Ready] form state.
 * :param setUiState: Setter for the updated [AddEditItemUiState.Ready] form state.
 *
 * TODO: For demo purposes only; replace with ViewModel + effect channel. Can be potentially moved
 *  in full to VM, so long as functions are updated
 */
private class AddEditItemDemoController(
    private val deps: NavDeps,
    private val parentOptions: List<AddEditItemUiState.Ready.ParentOption>,
    private val knownTags: List<Tag>,
    private val getUiState: () -> AddEditItemUiState,
    private val setUiState: (AddEditItemUiState) -> Unit
) {
    /**
     * Handles user intents emitted by [AddEditItemScreen].
     *
     * Routing rules:
     * - [AddEditItemIntent.SaveClicked] triggers [onSave].
     * - Back/discard intents pop the back stack.
     * - Image picker launch intent is ignored here (the screen launches the picker)
     * - All other intents are reduced into a new form state via [reduceIntent].
     *
     * :param intent: The user intent to process.
     *
     * TODO: ViewModel should own this and expose state + effects.
     */
    fun onIntent(intent: AddEditItemIntent) {
        val currentState = getUiState()

        when (intent) {
            // Navigation related
            AddEditItemIntent.SaveClicked -> onSave(currentState)
            AddEditItemIntent.BackClicked,
            AddEditItemIntent.DiscardChangesConfirmed -> deps.navController.popBackStack()

            // Screen handles launching the picker; destination consumes results via ImagePicked.
            AddEditItemIntent.PickImageClicked,
            AddEditItemIntent.DismissDiscardDialog -> Unit

            // Reduce remaining intents into the next UI state.
            else -> {
                if (currentState is AddEditItemUiState.Ready) {
                    val updated = reduceIntent(
                        currentState = currentState,
                        intent = intent,
                        knownTags = knownTags,
                        parentOptions = parentOptions
                    )
                    setUiState(updated)
                }
            }
        }
    }

    /**
     * Demo save handler:
     * - Validates the current form state via [validate].
     * - If valid, shows a success snackbar and pops the back stack.
     *
     * This demo implementation does not persist data.
     *
     * :param currentState: The current [AddEditItemUiState] (only Ready is saveable).
     *
     * TODO: onSave function for demo purposes - handled by ViewModel
     *  currently doesn't actually save anything
     */
    fun onSave(currentState: AddEditItemUiState) {
        // Only Ready state can be saved.
        if (currentState !is AddEditItemUiState.Ready) return

        // Validate and push validation results back into state.
        val validated = validate(currentState)
        setUiState(validated)

        // Proceed only if validation passes.
        if (
            (validated.validation.nameError == null) &&
            (validated.validation.containerError == null)
        ) {
            /**
             * NOTE: This demo controller calls snackbar and navigation directly.
             *
             * TODO: When moving to ViewModel, emit one-off effects (e.g., ShowSnackbar,
             *  NavigateBack) instead of passing NavController/snackbar callbacks into the VM.
             */
            deps.showSnackbar(
                if (validated.mode == AddEditItemUiState.Ready.Mode.CREATE)
                    "Item created"
                else
                    "Item updated"
            )

            // Editing is finished, navigate to previous screen
            deps.navController.popBackStack()
        }
    }
}

/**
 * Validates the Add/Edit Item form state and returns a copy containing validation errors.
 *
 * Current validation rules:
 * - Name is required (non-blank after trimming).
 * - If [AddEditItemUiState.Ready.containerId] is null (Root), the item must be
 *   [ItemStatus.TAKEN_OUT].
 *
 * :param currentState: The current form state.
 * :return: A copy of [currentState] with updated validation fields.
 *
 * TODO: Move into ViewModel if validation rules expand or become repository-backed.
 */
private fun validate(
    currentState: AddEditItemUiState.Ready
): AddEditItemUiState.Ready {
    // // TODO: Expand validation (e.g., unique name within container) once repository is available.
    val nameError = if(currentState.name.trim().isBlank()) "Name is required" else null

    // TODO: Revisit rule if we allow items in Root with status Stored in the future.
    val containerError =
        if ((currentState.containerId == null) && (currentState.status != ItemStatus.TAKEN_OUT))
            "Items outside a container must be marked Taken Out."
        else null

    return currentState.copy(
        validation = currentState.validation.copy(
            nameError = nameError,
            containerError = containerError
        )
    )
}

/**
 * Pure state transition: applies a user [intent] to [currentState] and returns the next state.
 *
 * Side effects (navigation, snackbars, persistence) are intentionally not performed here.
 *
 * :param currentState: Current form state.
 * :param intent: User intent to apply.
 * :param parentOptions: Demo container options used to resolve container display name.
 * :param knownTags: Demo tag set used for suggestions and existing-tag resolution.
 * :return: The next form state after applying [intent] and running [validate].
 *
 * TODO: for demo purposes only; could be moved into ViewModel later if matches intended structure
 */
private fun reduceIntent(
    currentState: AddEditItemUiState.Ready,
    intent: AddEditItemIntent,
    parentOptions: List<AddEditItemUiState.Ready.ParentOption>,
    knownTags: List<Tag>
): AddEditItemUiState.Ready {
    // What to do for each emitted user action
    val updated = when (intent) {
        // Editable fields
        is AddEditItemIntent.NameChanged ->
            currentState.copy(name = intent.value, isDirty = true)

        is AddEditItemIntent.DescriptionChanged ->
            currentState.copy(description = intent.value, isDirty = true)

        is AddEditItemIntent.ImagePicked ->
            currentState.copy(imageUri = intent.uriString, isDirty = true)

        AddEditItemIntent.RemoveImageClicked ->
            currentState.copy(imageUri = null, isDirty = true)

        // Container related
        is AddEditItemIntent.ContainerChanged ->
            if (!currentState.canChangeParent)
                currentState
            else if (intent.containerId == null) {
                // Root container implies Taken Out; ensure checkoutDate is set.
                currentState.copy(
                    containerId = null,
                    status = ItemStatus.TAKEN_OUT,
                    checkoutDate = currentState.checkoutDate ?: Date(),
                    isDirty = true
                )
            } else {
                val parentName = parentOptions.firstOrNull { it.id == intent.containerId }?.name
                // TODO: Decide whether selecting a container should force status to Stored.
                currentState.copy(
                    containerId = intent.containerId,
                    containerName = parentName,
                    isDirty = true
                )
            }

        is AddEditItemIntent.StatusChanged ->
            if (currentState.containerId == null) {
                // Root container forces Taken Out; status toggle should be disabled in UI.
                currentState
            } else {
                val newStatus = intent.status

                if (newStatus == currentState.status) {
                    currentState
                } else {
                    // Toggle status, update checkout date, set form to dirty
                    when (newStatus) {
                        ItemStatus.STORED ->
                            currentState.copy(
                                status = ItemStatus.STORED,
                                checkoutDate = null,
                                isDirty = true
                            )

                        ItemStatus.TAKEN_OUT ->
                            currentState.copy(
                                status = ItemStatus.TAKEN_OUT,
                                checkoutDate = currentState.checkoutDate ?: Date(),
                                isDirty = true
                            )
                    }
                }
            }

        // Tag-related intents are delegated to a specialized reducer for clarity.
        AddEditItemIntent.AddQueryAsTagClicked,
        is AddEditItemIntent.ExistingTagSelected,
        is AddEditItemIntent.QueryChanged,
        is AddEditItemIntent.RecommendedTagSelected,
        AddEditItemIntent.RefreshRecommendations,
        is AddEditItemIntent.RemoveTagClicked -> reduceTagIntent(
            currentState = currentState,
            intent = intent,
            knownTags = knownTags
        )

        // These are handled as side effects by the controller/destination; reducer consumes them.
        AddEditItemIntent.DiscardChangesConfirmed,
        AddEditItemIntent.DismissDiscardDialog,
        AddEditItemIntent.PickImageClicked,
        AddEditItemIntent.BackClicked,
        AddEditItemIntent.SaveClicked -> currentState
    }

    return validate(updated)
}

/**
 * Handles tag-related intents by updating:
 * - [AddEditItemUiState.Ready.tagQuery]
 * - [AddEditItemUiState.Ready.tagSuggestions]
 * - [AddEditItemUiState.Ready.selectedTags]
 * - [AddEditItemUiState.Ready.inputError]
 *
 * This implementation uses [knownTags] as a demo data source. In the final implementation,
 * suggestions and existing tag resolution should come from the repository.
 *
 * :param currentState: Current form state.
 * :param intent: Tag-related user intent.
 * :param knownTags: Demo tag list.
 * :return: Updated form state (not validated here; caller runs [validate]).
 *
 * TODO(REMOVE): Replace knownTags usage with repo-backed tag search in VM.
 */
private fun reduceTagIntent(
    currentState: AddEditItemUiState.Ready,
    intent: AddEditItemIntent,
    knownTags: List<Tag>
): AddEditItemUiState.Ready {

    /**
     * Regex pattern for allowed tag characters.
     *
     * Allows letters, numbers, spaces, '&', and '-'.
     *
     * TODO: Expand allowed characters if product requirements change.
     */
    val allowedTagRegex = Regex("""^[A-Za-z0-9 &-]+$""")

    /**
     * Normalization helpers:
     * - Typing normalization avoids trimming so the TextField does not fight the cursor.
     * - Commit normalization trims and collapses whitespace for stable storage/comparison.
     */
    fun normalizeForTyping(value: String): String =
        value.replace(Regex("\\s{2,}"), " ")
    fun normalizeForCommit(value: String): String =
        value.trim().replace(Regex("\\s+"), " ")

    /**
     * Handles query updates by validating input and generating suggestion candidates.
     *
     * Suggestions are filtered case-insensitively and exclude tags already in selectedTags.
     */
    fun updateSuggestions(currentState: AddEditItemUiState.Ready): AddEditItemUiState.Ready {
        // Do not trim while typing, prevents spaces at end of String
        val query = normalizeForTyping(currentState.tagQuery)

        // If query is blank, clear suggestions and errors but keep the field value consistent.
        if (query.isBlank())
            return currentState.copy(
                tagSuggestions = emptyList(),
                inputError = null,
                tagQuery = query
            )

        val err =
            if (!allowedTagRegex.matches(query))
                "Use only letters, numbers, spaces, '-', and '&'." // TODO: update err text if additional allowed chars added
            else null

        val queryKey = query.lowercase()

        // Exclude already-selected tags from suggestions.
        val selectedIds: Set<TagId> =
            currentState.selectedTags.map { it.id }.toSet()

        // TODO(REMOVE): Replace with repo search (e.g., beginsWith/contains ranking).
        val newSuggestions =
            if (err == null) {
                knownTags
                    .filterNot { it.id in selectedIds }
                    .filter { it.name.lowercase().contains(queryKey) }
                    .sortedBy { it.name.lowercase() }
                    .take(currentState.suggestionsLimit)
            } else emptyList()

        // Return updated state with new suggestions and tag input error text
        return currentState.copy(
            inputError = err,
            tagSuggestions = newSuggestions
        )
    }

    /**
     * Adds [rawName] as a selected tag, reusing an existing tag when possible.
     *
     * If the tag does not exist in [knownTags], a demo "new tag" is created using a negative ID.
     * In the real implementation, tag creation should be persisted during save.
     *
     * :param state: Current form state.
     * :param rawName: Raw tag text (from query or recommendation).
     *
     * TODO: Consider emitting a snackbar msg when max tags is reached or duplicates are attempted
     */
    fun addTagNameToSelected(
        currentState: AddEditItemUiState.Ready,
        rawName: String
    ): AddEditItemUiState.Ready {
        // TODO: snackbar error message? "You have added the max number of tags to this item."
        if (!currentState.canAddMore) return currentState

        val tagName = normalizeForCommit(rawName)

        if (!allowedTagRegex.matches(tagName))
            return currentState.copy(
                inputError = "Use only letters, numbers, spaces, '-', and '&'."
            )

        val tagKey = tagName.lowercase()

        // Look through knownKeys list for existing keys - will be null if no match found
        // TODO: If using in ViewModel, knownTags list = repo of all tags in DB.
        val existing = knownTags.firstOrNull {
            it.name.trim().replace(Regex("\\s+"), " ").lowercase() == tagKey
        }

        // Case-insensitive duplicate check against currently selected tags.
        val currentSelectedKeys: Set<String> =
            currentState.selectedTags.map { normalizeForCommit(it.name).lowercase() }.toSet()

        // Check if the tag the user wants to add isn't already attached to this item.
        // TODO: snackbar error message? "This item already has that tag."
        if (tagKey in currentSelectedKeys)
            return currentState.copy(tagQuery = "", tagSuggestions = emptyList(), inputError = null)

        /**
         * Demo rule: negative IDs indicate "new tag to create on Save".
         *
         * TODO(REMOVE): Once repo exists, create tags through the repo (or stage them in VM)
         *  without relying on negative IDs.
         */
        val tagToAdd = existing ?: Tag(
            id = TagId(-tagKey.hashCode().toLong()),
            name = tagName
        )

        // Create a new selected tag list for the UiState, which includes the query's resulting
        // tag being applied to the item.
        // TODO: Decide whether selected tags should be sorted (alphabetical vs insertion order).
        val newSelectedTagsList =
            (currentState.selectedTags + tagToAdd).sortedBy { it.name.lowercase() }

        // Build new UiState based on results
        return currentState.copy(
            selectedTags = newSelectedTagsList,
            tagQuery = "",
            tagSuggestions = emptyList(),
            inputError = null
        )
    }

    // The actual reducer part
    return when (intent) {
        // When query is changed, must check input and update suggestions
        is AddEditItemIntent.QueryChanged -> {
            if (intent.value.isBlank())
                currentState.copy(tagQuery = "", tagSuggestions = emptyList(), inputError = null)
            else
                updateSuggestions(currentState.copy(tagQuery = intent.value))
        }

        // User wants to add the query as a tag
        AddEditItemIntent.AddQueryAsTagClicked ->
            addTagNameToSelected(currentState = currentState, rawName = currentState.tagQuery)

        // User wants to add one of the existing suggested tags to the item's selected tags list
        is AddEditItemIntent.ExistingTagSelected -> {
            /**
             * Uses knownTags to resolve the selected tag.
             *
             * TODO(REMOVE): In ViewModel, resolve selected tag via repository
             *  (or carry name in intent).
             */
            val userChosenTag =
                knownTags.firstOrNull { it.id == intent.tagId } ?: return currentState
            addTagNameToSelected(currentState = currentState, rawName = userChosenTag.name)
        }

        // User wants to remove tag from item's selected tags list.
        is AddEditItemIntent.RemoveTagClicked -> {
            val newSelectedTagsList = currentState.selectedTags.filterNot { it.id == intent.tagId }
            currentState.copy(selectedTags = newSelectedTagsList, inputError = null)
        }

        // User wants to add one of the ML Kit recommended tags to the item's selected tag list.
        // TODO: right now recommendations are returned as list of strings - check if confidence
        //  score is possible?
        is AddEditItemIntent.RecommendedTagSelected ->
            addTagNameToSelected(currentState = currentState, rawName = intent.name)

        // User wants to refresh the recommendations provided
        // TODO: Necessary or possible? Does it just give the same results every time?
        //  if we provide recommendations based on item name or description, this may be useful,
        //  otherwise not really?
        AddEditItemIntent.RefreshRecommendations ->
            currentState.copy(isRecommending = true)

        // Non-tag intents should be filtered before calling this reducer.
        else -> currentState
    }
}