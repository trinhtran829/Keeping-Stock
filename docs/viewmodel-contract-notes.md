# Final week - ViewModel Contract Notes

I'll go through screen by screen of what remains for integration.

Primarily, the ViewModels all will need to implement the existing UiStates built out for each screen - you can probably refactor each one to instead use the generic UiState, but I feel like that's extra churn that isn't really necessary. Your call

## Each screen's contracts

Each screen will have the following contracts (so far - I don't think any more need to be added?)

- ViewModelContract
- UiState Contract
- Intent Contract
- Repository Contracts

### The General ViewModelContract

This should be implemented by each ViewModel.

```kotlin
interface ViewModelContract<T, G> {
    val uiState: StateFlow<T>
    fun onIntent(intent: G)
}
```

Pretty simple contract, not meant to be complicated, it just really formalizes the unidirectional data flow structure of the app. ViewModel exposes uiState for observation and provides handling of Intent from the UI.

### UI State contracts

These are the states and the associated variables for each state. I've made a few updates to the screens, but hopefully I've covered everything we'll need for each screen's MVP.

**If you want to see expected purpose of each variable in the Ready, I have put them as KDocs over each intended user-action in the specific Intent file.**

**_Exception: ItemBrowserUiData is in the ItemBrowserViewModel and I avoided touching that. The details should be very similar to the KDoc for the ContainerBrowserUiState.Ready, so reference that or ask me for clarification if needed_**

### Intent contracts - what they are

Intent is just an interface that specifies what action the UI is emitting from the user - the user's intent at the moment. 

I thought it kept the code a bit neater to wrap up the functions in their own class, but it wouldn't be much different if we put the functions themselves as callback parameters. I just figured it lets you use when to specify which function to run while giving you lots of leeway on how to do that.

**If you want to see expected behavior of each intent, I have put them as KDocs over each intended user-action in the specific Intent file.**

## Container Browser Screen:

The container browser screen is currently missing search, filter, and possibly sort functionality. This will add a few Intents emitted from the UI, but I'm aiming for MVP. I will add a ContainerBrowserIntent contract. I will also update the ContainerBrowserUiState.Ready class for these functions.

We can also maybe consider adding a layout change to the intent (grid vs list), I don't think it would add much work on y'all's end (it's mostly a UI thing), but right now that's a stretch goal.

### Using ViewModelContract

Your ViewModel class will probably have the signature as something like this:

```kotlin
class ContainerBrowserViewModel(
    private val containerId: ContainerId?,
    private val containerRepository: ContainerRepository,
    private val itemRepository: ItemRepository
) : ViewModel(), 
    ViewModelContract<ContainerBrowserUiState, ContainerBrowserIntent> {

    override val uiState: StateFlow<ContainerBrowserUiState> = _uiState.asStateFlow()

    override fun onIntent(intent: ContainerBrowserIntent) { ... }
}
```

### ContainerBrowserUiState Contract

This has been updated with new values to reflect searching/filtering/sorting. 

I've now included new variables query, filter, sort, and layout, for obvious reasons. I've also included two new lists, visibleSubcontainers and visibleItems. The 'visible' variants would be the subcontainers/items that are supposed to be showing due to seach/filters. 

Finally, there's the variable "emptyState", which is more like a sub-state of the Ready state. Essentially the UI right now is just showing "There's nothing here yet" when there are no items/subcontainers in their respective lists, but the message won't make sense for when it's actually "No results for that search/filter" or something. This is just a simple enum so that the ViewModel can state what kind of empty state it is for the UI to show the right empty state message.

Here are the new Ready state variables (Error and Loading haven't changed, yet):

```kotlin
val containerId: ContainerId?
val containerName: String
val subcontainers: List<Container>
val items: List<Item>
val visibleSubcontainers: List<Container>
val visibleItems: List<Item>
val query: String
val filter: ContainerBrowserFilter
val sort: ContainerBrowserSort
val layout: ContainerBrowserLayout
val emptyState: ContainerBrowserEmptyState
```

I've only specified three filters so far: 
- Flag for showing items only
- Flag for showing containers only
- A filter for showing items only with a specific ItemStatus.

Sort is just name ascending, name descending, created newest, and created oldest. I think you might be able to just sort the list directly in the ViewModel into the visible variant lists.

Layout change doesn't require really anything other than just recording the enum selection, UI will do heavy lifting if we have the time.

Please make sure you take a look at the UiState, it's got comments and TODOs.

### ContainerBrowserIntent Contract

The ViewModel will need to handle the following emitted Intents from the UI. There is a lot of commentary with TODOs to go over in the actual file:

```kotlin
data class QueryChange(val query: String) : ContainerBrowserIntent
data class QuerySubmit(val query: String) : ContainerBrowserIntent
data object ClearQuery : ContainerBrowserIntent
data class FilterChange(val filter: ContainerBrowserFilter) : ContainerBrowserIntent
data class SortChange(val sort: ContainerBrowserSort) : ContainerBrowserIntent
data class LayoutChange(val layout: ContainerBrowserLayout) : ContainerBrowserIntent
data object Retry : ContainerBrowserIntent
```

### Any additional Repository functions needed:

Right now, for MVP, we may want the VM to do the search/filter/sorting based on the subcontainers/items lists. This doesn't scale well with larger datasets, but it may be faster to implement, and it's good enough for our purposes.

If we have the VM do the search/sort/filtering of container contents, then no new DAO queries are required.

If we want to have a DB-backed search/filter, we will need:
- Container Repo
    - `searchChildContainers(parentContainerId, query)` (already have this)
    - `observeChildContainersSorted(parentId, sort)`

- Item Repo
    - `searchItemsInContainerByNameOrTag(containerId, query): Flow<List<Item>>` (currently we only have `searchItemsByNameOrTag(query)` which is a global item search, good for the ItemBrowser)

## Item Browser Screen:

I think there might be some misalignment with the ItemBrowser vs ContainerBrowser.

I've been setting up the app so far so that the Container Browser is like a standard file explorer - it shows all containers and all items found in the container, and opening any subcontainer opens up the container browser for that particular container.

To contrast this, the Item Browser is a global item viewer - it just shows all the items in all containers, so that you can see what you have and search that way, rather than hunting down the item by going through the containers one at a time.

I realize that the ItemBrowser ViewModel has been set up with the idea that it only shows the items found in a single container, while presumably the container browser only shows the containers in a container. I think that would be very complicated to set up, and would require another screen to make the UX work.

Sorry it took so long for me to realize this - I only figured it out while trying to integrate the ItemBrowser ViewModel in its current state with the ItemBrowser destination/screen.

So with that in mind, I have some updated contracts:

### Using the ViewModelContract

The contract is much the same, and probably will the pretty much the same for all screens.

```kotlin
class ItemBrowserViewModel(
    private val repository: ItemRepository
) : ViewModel(),
    ViewModelContract<UiState<ItemBrowserUiData>, ItemBrowserIntent> {

    override val uiState: StateFlow<UiState<ItemBrowserUiData>> = _uiState

    override fun onIntent(intent: ItemBrowserIntent) { ... }
}
```

### ItemBrowserUiData Contract

Your using the UiState generic class as your UiState model, with ItemBrowserUiData as the supporting data class. I'll continue to use this framework. 

The UiData data class will need to be updated with the following variables to get the Item browser working with search/filter/sort:

```kotlin
data class ItemBrowserUiData(
    // Raw results from repo for the chosen scope (ALL / IN_CONTAINER / UNSORTED)
    val items: List<Item> = emptyList(),

    // Derived list after query/filter/sort
    val visibleItems: List<Item> = emptyList(),

    // Control state
    val query: String = "",
    val filter: ItemBrowserFilter = ItemBrowserFilter(),
    val sort: ItemBrowserSort = ItemBrowserSort.NAME_ASC,
    val layout: ItemBrowserLayout = ItemBrowserLayout.LIST,

    // Empty state options
    val emptyState: ItemBrowserEmptyState = ItemBrowserEmptyState.NONE
)
```

**I did not update the ItemBrowserUiData, as I didn't want to touch the ViewModel class without your say-so.**

I put the ItemBrowserSort, ItemBrowserFilter, ItemBrowserEmptyState enums as well as the ItemBrowserFilter data class into my ItemBrowserUiState file - you can move these to your ItemBrowserViewModel file instead if you prefer to keep them together with the ItemBrowserUiData.

Item Browser filter only has two options:
- Show items that have no container (stored in root)
- Show items with a specific status (STORED / TAKEN_OUT)
- A potential new option, which may be difficult to implement in the repo, but have it so that it shows all items in a specific container, included items stored in its subcontainers.

Layout, sort, and empty state all have the same options as ContainerBrowser sort (could be generalized?)

### ItemBrowserIntent Contract

Here are the functions needed to be implemented by the ViewModel:

```kotlin
data class QueryChange(val query: String) : ItemBrowserIntent
data object ClearQuery : ItemBrowserIntent
data class FilterChange(val filter: ItemBrowserFilter) : ItemBrowserIntent
data class SortChange(val sort: ItemBrowserSort) : ItemBrowserIntent
data class LayoutChange(val layout: ItemBrowserLayout) : ItemBrowserIntent
data object Retry : ItemBrowserIntent
```

### Any additional Repository functions needed:

Same options as ContainerBrowser screen - nothing new needed, depending on the option we go with.

## Container Details Screen:

Right now we don't have any way to delete items/containers. This screen will probably be where I implement this function, so I'll need to add new intents to be emitted from the screen (stretch goal could be to add this to the container/item browser screens too, to be able to do a bulk delete, but going for MVP here).

Otherwise this screen is a lot simpler than the browser screens.

### Using the ViewModelContract

```kotlin
class ContainerDetailViewModel(
    private val containerId: ContainerId?,
    private val containerRepository: ContainerRepository,
    private val itemRepository: ItemRepository
) : ViewModel(),
    ViewModelContract<ContainerDetailUiData, ContainerDetailIntent> {

    override val uiState: StateFlow<ContainerDetailUiData> = _uiState

    override fun onIntent(intent: ContainerDetailIntent) { ... }
}
```

### ContainerDetailUiState Contract

Here are the new Ready state variables (Error and Loading haven't changed, yet):

```kotlin
val container: Container,
val parentContainerName: String?,
val subcontainerCount: Int,
val itemCount: Int,
val canDelete: Boolean,
val deleteBlockedReason: String?, // null when canDelete
```

### ContainerDetailIntent Contract

Pretty simple in comparison to the others.

```kotlin
data object Retry : ContainerDetailIntent
data object DeleteConfirmed : ContainerDetailIntent
```

### New Repo Functions needed:

This is mostly a ready-only screen, so no additional repo functions needed.

## Item Details Screen:

Same as ContainerDetailScreen, but even more simple, thankfully.

### Using the ViewModelContract

```kotlin
class ContainerDetailViewModel(
    private val containerId: ContainerId?,
    private val containerRepository: ContainerRepository,
    private val itemRepository: ItemRepository
) : ViewModel(),
    ViewModelContract<ItemDetailUiData, ContainerDetailIntent> {

    override val uiState: StateFlow<ContainerDetailUiData> = _uiState

    override fun onIntent(intent: ContainerDetailIntent) { ... }
}
```

### ItemDetailUiState Contract

Unlike ContainerDetails, we don't need subcontainer or item counts, and there are no inherent blockers preventing the user from deletion (right now, container deletion is blocked when the container contains anything). 

Ready state variables are:

```kotlin
val item: Item,
val parentContainerName: String? = null
```

### ItemDetailIntent Contract

Same as ContainerDetailIntent:

```kotlin
data object Retry : ItemDetailIntent
data object DeleteConfirmed : ItemDetailIntent
```

### New Repo Functions needed:

None

## Add/Edit Container Screen:

This and Add/Edit Item are very complicated screens, a lot is being done in each one.

**The logic I used for the demo in the destination can be re-used in large parts for the ViewModel, because simulating the demo required writing some logic for several of the Intents.**

Keep in mind, in the destination you'll find I use a controller and reducer functions. These are literally just taking the intent and outputting a new UiState (reduces multiple input to one output). I used them because I was getting massive functions and it was getting hard to read.

### Using the ViewModelContract

```kotlin
class AddEditContainerViewModel(
    private val containerId: ContainerId?,                 // null = CREATE
    private val initialParentContainerId: ContainerId?,    // optional default for CREATE
    private val containerRepository: ContainerRepository
) : ViewModel(), ViewModelContract<AddEditContainerUiState, AddEditContainerIntent> {

    override val uiState: StateFlow<AddEditContainerUiState> = _uiState.asStateFlow()

    override fun onIntent(intent: AddEditContainerIntent) { ... }
}
```

### AddEditContainerUiState Contract

Ready state variables are:

```kotlin
val mode: Mode,
val containerId: ContainerId?,
val parentContainerId: ContainerId?,
val parentContainerName: String?,
val availableParents: List<ParentOption>,
val name: String,
val description: String?,
val imageUri: String?,
val isSaving: Boolean = false,
val isDirty: Boolean = false,
val validation: Validation = Validation(),
val canChangeParent: Boolean = true
```

(ParentOption and Validation data classes are implemented in the AddEditContainerUiState file)

### AddEditContainerIntent Contract

```kotlin
data class NameChanged(val value: String) : AddEditContainerIntent
data class DescriptionChanged(val value: String) : AddEditContainerIntent
data class ParentChanged(val parentId: ContainerId?) : AddEditContainerIntent
data object PickImageClicked : AddEditContainerIntent
data class ImagePicked(val uriString: String) : AddEditContainerIntent
data object RemoveImageClicked : AddEditContainerIntent
data object SaveClicked : AddEditContainerIntent
data object BackClicked : AddEditContainerIntent
data object DiscardChangesConfirmed : AddEditContainerIntent
data object DismissDiscardDialog : AddEditContainerIntent
```

Keep in mind that right now for MVP, PickImageClicked, BackClicked, and DismissDiscardChanges are all being handled in the UI, so VM will only need to consume these events as they're emitted, not actually do anything (VM should handle these ultimately, but MVP and all)

### New Repo Functions needed:

This one may have more repo functions needed, specifically some way to retrieve all available containers that this container can be moved to. This would be the most complicated DB query so far.

MVP has a flat list of containers, but stretch goal would be to do a hierarchical viewing/tree view of the containers so that the user can more easily select the desired container to move to. If you are able to implement this, Perhaps as some kind of linked list or something, I can see about updating the UI to display this.

## Add/Edit Item Screen:

Again, very complicated screens, a lot is being done, especially because of tagging feature.

**The logic I used for the demo in the destination can be re-used in large parts for the ViewModel, because simulating the demo required writing some logic for several of the Intents.**

Keep in mind, in the destination you'll find I use a controller and reducer functions. These are literally just taking the intent and outputting a new UiState (reduces multiple input to one output). I used them because I was getting massive functions and it was getting hard to read.

### Using the ViewModelContract

```kotlin
class AddEditItemViewModel(
    private val itemId: ItemId?,                      // null = CREATE
    private val initialContainerId: ContainerId?,     // optional default for CREATE
    private val itemRepository: ItemRepository,
    private val containerRepository: ContainerRepository,
    private val tagRepository: TagRepository
) : ViewModel(), ViewModelContract<AddEditItemUiState, AddEditItemIntent> {

    override val uiState: StateFlow<AddEditItemUiState> = _uiState.asStateFlow()

    override fun onIntent(intent: AddEditItemIntent) { ... }
}
```

### AddEditItemUiState Contract

Ready-state variables:

```kotlin
val mode: Mode,
val itemId: ItemId?,
val containerId: ContainerId?,
val containerName: String?,
val availableParents: List<ParentOption>,

val name: String,
val description: String,
val imageUri: String?,
val status: ItemStatus,
val createdDate: Date,
val checkoutDate: Date?,

// Tagging
val selectedTags: List<Tag> = emptyList(),
val tagQuery: String = "",
val tagSuggestions: List<Tag> = emptyList(),
val tagRecommendations: List<String> = emptyList(),
val isRecommending: Boolean = false,
val inputError: String? = null,
val maxTags: Int = 20,
val suggestionsLimit: Int = 8,

// Form Lifecycle
val isSaving: Boolean = false,
val isDirty: Boolean = false,
val validation: Validation = Validation(),
val canChangeParent: Boolean = true
```

Same as AddEditContainer, ParentOption and Validation data classes are implemented in the AddEditItemUiState file

### AddEditItemIntent Contract

```kotlin
data class NameChanged(val value: String) : AddEditItemIntent
data class DescriptionChanged(val value: String) : AddEditItemIntent
data class ContainerChanged(val containerId: ContainerId?) : AddEditItemIntent
data class StatusChanged(val status: ItemStatus) : AddEditItemIntent
data object PickImageClicked : AddEditItemIntent
data class ImagePicked(val uriString: String) : AddEditItemIntent
data object RemoveImageClicked : AddEditItemIntent
data object SaveClicked : AddEditItemIntent
data object BackClicked : AddEditItemIntent
data object DiscardChangesConfirmed : AddEditItemIntent
data object DismissDiscardDialog : AddEditItemIntent
data class QueryChanged(val value: String) : AddEditItemIntent
data object AddQueryAsTagClicked : AddEditItemIntent
data class ExistingTagSelected(val tagId: TagId) : AddEditItemIntent
data class RemoveTagClicked(val tagId: TagId) : AddEditItemIntent
data class RecommendedTagSelected(val name: String) : AddEditItemIntent
data object RefreshRecommendations : AddEditItemIntent
```

Same note from AddEditItemIntent: Keep in mind that right now for MVP, PickImageClicked, BackClicked, and DismissDiscardChanges are all being handled in the UI, so VM will only need to consume these events as they're emitted, not actually do anything (VM should handle these ultimately, but MVP and all)

### New Repo Functions needed:

Same note as from AddEditContainer section