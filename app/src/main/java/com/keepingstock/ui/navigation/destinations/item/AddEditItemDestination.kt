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
import com.keepingstock.core.contracts.uistates.item.AddEditItemIntent
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
 * Registers the Add/Edit Item destination and wires demo state + navigation callbacks.
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
 * :param deps: Navigation and UI dependencies (NavController, top bar updater, snackbar helper).
 */
internal fun NavGraphBuilder.addAddEditItemDestination(
    deps: NavDeps
) {
    composable(
        route = NavRoute.AddEditItem.route,
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

        // TODO(REMOVE): Demo-only mode selector
        var demoMode by rememberSaveable(itemId?.value) {
            mutableStateOf(DemoMode.READY)
        }

        // TODO(REMOVE): Demo tags (replace with repo/VM)
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

        // TODO(REMOVE): Demo containers list (replace with repo/VM)
        val parentOptions = remember {
            listOf(
                AddEditItemUiState.Ready.ParentOption(null, "Root"),
                AddEditItemUiState.Ready.ParentOption(ContainerId(1L), "Garage"),
                AddEditItemUiState.Ready.ParentOption(ContainerId(2L), "Kitchen"),
                AddEditItemUiState.Ready.ParentOption(ContainerId(3L), "Shed")
            )
        }

        // TODO(REMOVE): Demo ready state (uiState covered by VM)
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

        // TODO: Demo mode only, to be covered by VM
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

        LaunchedEffect(itemId, containerId) {
            deps.onTopBarChange(addEditItemTopBarConfig(uiState))
        }

        // TODO: Demo only: Controller will be replaced by ViewModel
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
            // TODO(REMOVE): demo-only UI controls.
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
                    // If user is in Loading/Error demo, ignore edit intents except back.
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
 * Builds top bar title/back behavior from AddEditItemUiState.
 *
 * :param uiState: The current UI state for the Add/Edit Item screen.
 * :return: A [TopBarConfig] describing the top app bar title and back button visibility.
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
 * A simple controller object for user intent + side effects
 *
 * This exists to keep the destination composable thinner while the real ViewModel is pending.
 *
 * :param deps: Navigation and snackbar helpers.
 * :param parentOptions: Demo list of available parent containers for selection.
 * :param knownTags: Set of tags suitable for demo purposes
 * :param getUiState: Getter for the current [AddEditItemUiState.Ready] form state.
 * :param setUiState: Setter for the updated [AddEditItemUiState.Ready] form state.
 *
 * TODO: For demo purposes only; replace with ViewModel functions. Can be potentially moved
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
     * Handles user intents emitted by the Add/Edit Item screen.
     *
     * Routing rules:
     * - [AddEditItemIntent.SaveClicked] triggers [onSave].
     * - Back/discard intents pop the back stack.
     * - Image picker launch intent is ignored here (the screen launches the picker); this
     *   controller only consumes [AddEditItemIntent.ImagePicked] results.
     * - All other intents are reduced into a new form state via [reduceIntent].
     *
     * :param intent: The user intent to process.
     *
     * TODO: onIntent functions for demo purposes - handled by ViewModel
     */
    fun onIntent(intent: AddEditItemIntent) {
        val currentState = getUiState()

        when (intent) {
            // Navigation related
            AddEditItemIntent.SaveClicked -> onSave(currentState)
            AddEditItemIntent.BackClicked,
            AddEditItemIntent.DiscardChangesConfirmed -> deps.navController.popBackStack()

            // Screen handles, destination currently consumes only
            AddEditItemIntent.PickImageClicked,
            AddEditItemIntent.DismissDiscardDialog -> Unit

            // Set UI State based on intent - call reducer function (keeps controller thin)
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
     * TODO: onSave function for demo purposes - handled by ViewModel
     */
    fun onSave(currentState: AddEditItemUiState) {
        // don't save if state isn't ready (redundant?)
        if (currentState !is AddEditItemUiState.Ready) return

        // Validate the UiState
        val validated = validate(currentState)
        setUiState(validated)

        if ((validated.validation.nameError == null) && (validated.validation.containerError == null))
            // TODO: SHOULD NOT BE IN VM - FIGURE OUT PATTERN TO PREVENT PASSING
            //  navController OR snackbar TO VM!
            // Show success message
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

/**
 * Validates the Add/Edit Item form state and returns a copy containing validation errors.
 *
 * Current validation rules:
 * - Name is required (non-blank after trimming).
 *
 * :param currentState The current form state.
 *
 * :return A copy of [currentState] with updated validation fields.
 *
 * TODO: This might be able to be moved directly into the ViewModel later.
 */
private fun validate(
    currentState: AddEditItemUiState.Ready
): AddEditItemUiState.Ready {
    // TODO: Name error validation here is just "is it blank". ViewModel might include if it's
    //  unique in the container? Update UiState model if additional validation is needed.
    val nameError = if(currentState.name.trim().isBlank()) "Name is required" else null

    // TODO: containerError is currently only just "if it's null (root) it HAS to be
    //  [ItemStatus.TAKEN_OUT]
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
 * State transition: applies an intent to the current uistate and returns the next uistate.
 *
 * :param currentState: Current form state.
 * :param intent: User intent to apply.
 * :param parentOptions: Available parent container options used to resolve parent display name.
 *
 * :return: The next form state after applying [intent] and validation.
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
                // if container is null, set checkout date and status as taken out
                currentState.copy(
                    containerId = null,
                    status = ItemStatus.TAKEN_OUT,
                    checkoutDate = currentState.checkoutDate ?: Date(),
                    isDirty = true
                )
            } else {
                val parentName = parentOptions.firstOrNull { it.id == intent.containerId }?.name
                // TODO: behavior check: change status to stored if currently taken out?
                currentState.copy(
                    containerId = intent.containerId,
                    containerName = parentName,
                    isDirty = true
                )
            }

        is AddEditItemIntent.StatusChanged ->
            if (currentState.containerId == null) {
                // Shouldn't happen, since no container = status toggle disabled
                currentState.copy(
                    status = ItemStatus.TAKEN_OUT,
                    checkoutDate = currentState.checkoutDate ?: Date(),
                )
            } else {
                // Toggle status, update checkout date, set form to dirty
                when (intent.status) {
                    ItemStatus.STORED ->
                        currentState.copy(
                            status = ItemStatus.TAKEN_OUT,
                            checkoutDate = Date(),
                            isDirty = true
                        )
                    ItemStatus.TAKEN_OUT ->
                        currentState.copy(
                            status = ItemStatus.STORED,
                            checkoutDate = null,
                            isDirty = true
                        )
                }
            }

        // Tag Related
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

        // Code shouldn't run; handled by UI/Controller; just consumes
        AddEditItemIntent.DiscardChangesConfirmed,
        AddEditItemIntent.DismissDiscardDialog,
        AddEditItemIntent.PickImageClicked,
        AddEditItemIntent.BackClicked,
        AddEditItemIntent.SaveClicked -> currentState
    }

    return validate(updated)
}

/**
 * When moving to ViewModel, knownTags would probably not be used. Uses of it in intent reducer
 * should probably be replaced by a repo call of tags that match the query (either begins with,
 * contains, or whatever other logic is preferred)
 */
private fun reduceTagIntent(
    currentState: AddEditItemUiState.Ready,
    intent: AddEditItemIntent,
    knownTags: List<Tag>
): AddEditItemUiState.Ready {

    /**
     * Regex pattern of allowed characters
     *
     * logic: ^ means starts with, $ means ends with, character between brackets are characters
     * that you want to match with (allowed characters), while + means match whatever is in the
     * brackets an unlimited number of times. All together, if matches function is run then it
     * should return true/false based on "does this string, from start to finish, contain only
     * these characters between one and an unlimited number of times?"
     *
     * TODO: add additional characters as group decides
     */
    val allowedTagRegex = Regex("^[A-Za-z0-9 \\-&]+$")

    // Only single space
    fun normalize(value: String): String =
        value.trim().replace(Regex("\\s+"), " ")

    /**
     * Used for query change intent. Check query for errors and updates list of suggested tags
     * based on user's current query.
     */
    fun updateSuggestions(currentState: AddEditItemUiState.Ready): AddEditItemUiState.Ready {
        // Remove any extra whitespace
        val query = normalize(currentState.tagQuery)

        // If query is blank, no suggestions and no error
        if (query.isBlank())
            return currentState.copy(tagSuggestions = emptyList(), inputError = null)

        // Set err text if query does not match regex. See regex for logic
        val err =
            if (!allowedTagRegex.matches(query))
                "Use only letters, numbers, spaces, '-', and '&'." // TODO: update err text if additional allowed chars added
            else null

        // For case-insensitive matching while preserving query input
        val queryKey = query.lowercase()

        // Filtering demo set of "known tags"
        // TODO: replace with repo call
        val newSuggestions =
            if (err == null) {
                knownTags
                    .filter { it.name.lowercase().contains(queryKey) }
                    .sortedBy { it.name.lowercase() }
                    .take(currentState.suggestionsLimit)
            } else emptyList()

        // Return updated state with new suggestions and tag input error text
        return currentState.copy(
            tagQuery = query,
            inputError = err,
            tagSuggestions = newSuggestions
        )
    }

    /**
     * Add the current query to the list of known tags, if no errors are present. Should reuse
     * existing tags. Also adds to the list of currently selected tags for the item.
     *
     * Currently just uses the demo list of known tags, replace with repo calls.
     */
    fun addTagNameToSelected(
        currentState: AddEditItemUiState.Ready,
        rawName: String
    ): AddEditItemUiState.Ready {
        // First check if the user can add any more tags based on maxTags variable in UiState
        // TODO: snackbar error message? "You have added the max number of tags to this item."
        if (!currentState.canAddMore) return currentState

        // Remove extra whitespace
        val tagName = normalize(rawName)

        // If regex match fails, return current state with inputError updated
        if (!allowedTagRegex.matches(tagName))
            return currentState.copy(
                inputError = "Use only letters, numbers, spaces, '-', and '&'."
            )

        // Used for case-insensitive matching.
        // TODO: if rules state that tags are stored as lowercase, then this can be moved to
        //  tagName definition as part of normalization
        val tagKey = tagName.lowercase()

        // Look through knownKeys list for existing keys - will be null if no match found
        // TODO: If using in ViewModel, knownTags list = repo of all tags in DB.
        val existing = knownTags.firstOrNull {
            it.name.trim().replace(Regex("\\s+"), " ").lowercase() == tagKey
        }

        // Get a set of currently selected keys for the item that have been converted to lowercase
        // for case-insensitive matching.
        // TODO: if tags are stored normalized and lowercase, then map function isn't needed
        val currentSelectedKeys: Set<String> =
            currentState.selectedTags.map { normalize(it.name).lowercase() }.toSet()

        // Check if the tag the user wants to add isn't already attached to this item.
        // TODO: snackbar error message? "This item already has that tag."
        if (tagKey in currentSelectedKeys)
            return currentState.copy(tagQuery = "", tagSuggestions = emptyList(), inputError = null)

        // Will use found existing tag or create a new one to be saved on onSave intent (we
        // probably don't want to save a new tag if the add/edit is completely discarded, so wait
        // until onSave intent)
        // For the purposes of this demo, negative IDs represent "new tag to create on Save".
        // TODO: alternatively instead of using Tag model we wrap it in another object, something
        //  like TagUi(Tag, isNew) so we don't rely on negative IDs as a rule? Maybe easier for
        //  onSave processing logic.
        val tagToAdd = existing ?: Tag(
            id = TagId(-tagKey.hashCode().toLong()),
            name = tagName
        )

        // Create a new selected tag list for the UiState, which includes the query's resulting
        // tag being applied to the item.
        // TODO: sort alphabetically?
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
            // retrieve the tag name text selected by the user so that addTagNameToSelected
            // can be reused.
            // TODO: Can also work with intent.name so it can be bundled with
            //  RecommmendedTagSelected intent, just need to update Intent contract
            //
            // TODO: replace knownTags with repo lookup to get tag name, if necessary
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

        // Shouldn't be encountered; Handled by state-reducer/UI/Controller; just consumes
        else -> currentState
    }
}

/**
 * Builds a demo [AddEditItemUiState.Ready] for previews/manual testing.
 *
 * CREATE mode:
 * - Empty name/description.
 * - Optional [containerId] preselected when provided.
 *
 * EDIT mode:
 * - Uses placeholder values for name/description (replace with repository-loaded values later).
 *
 * :param itemId: Item being edited (null for CREATE).
 * :param containerId: Optional initial parent selection.
 * :param parentOptions: Demo parent options list, used to resolve parent display name.
 * :param knownTags: Set of tags suitable for demo purposes
 *
 * :return A [AddEditItemUiState.Ready] suitable for demo rendering.
 *
 * TODO: For demo purposes only
 */
private fun demoInitialUiState(
    itemId: ItemId?,
    containerId: ContainerId?,
    parentOptions: List<AddEditItemUiState.Ready.ParentOption>,
    knownTags: List<Tag>
): AddEditItemUiState.Ready {
    // Set parent name
    val parentName = parentOptions.firstOrNull { it.id == containerId }?.name

    // Minor convenience
    val now = Date()

    // Auto-mode based on itemId == null
    val mode =
        if (itemId == null) AddEditItemUiState.Ready.Mode.CREATE
        else AddEditItemUiState.Ready.Mode.EDIT

    // Auto-taken out if in root container
    val initialStatus =
        if (containerId == null) ItemStatus.TAKEN_OUT
        else ItemStatus.STORED

    // Set initial checkout only if in root (auto-taken out)
    val initialCheckout = if (containerId == null) now else null

    // Build and pre-validate the demo ready state
    return validate(
        AddEditItemUiState.Ready(
            mode = mode,
            itemId = itemId,
            containerId = containerId,
            containerName = parentName,
            availableParents = parentOptions,

            name =
                if (mode == AddEditItemUiState.Ready.Mode.EDIT)
                    "Impact Driver"
                else "",
            description =
                if (mode == AddEditItemUiState.Ready.Mode.EDIT)
                    "18V brushless"
                else "",
            imageUri = null,
            status = initialStatus,
            createdDate = now,
            checkoutDate = initialCheckout,

            selectedTags =
                if (mode == AddEditItemUiState.Ready.Mode.EDIT)
                    listOf(knownTags[0], knownTags[1])
                else emptyList(),
            tagQuery = "",
            tagSuggestions = emptyList(),
            tagRecommendations = emptyList(),
            isRecommending = false,
            inputError = null,
            maxTags = 20,
            suggestionsLimit = 8,

            isSaving = false,
            isDirty = false,
            validation = AddEditItemUiState.Ready.Validation(),
            canChangeParent = true
        )
    )
}