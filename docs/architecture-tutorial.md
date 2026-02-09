# Keeping Stock - Architecture Tutorial

This document is meant to detail the overall architecture of the app, and help the team to understand HOW the app works at a granular level - how each layer interacts and how to utilize and build upon the existing infrastructure.

If you need clarification or had a different idea of how things would work with the app, let's go over it with the team in Discord!

## Overall Architecture Structure

**IN PROGRESS - will fill this section out after finishing individual sections**

## Part 1 - App Entry

- `MainActivity`: Android entry point; minimal system UI setup.
- `KeepingStockApp`: App shell (Scaffold, global UI like top bar/snackbars); app-wide state holders.

### MainActivity

All Android apps start in the `MainActivity`. This is located in the root folder of `com.keepingstock`. It's not meant to do much, all it's really doing is setting the screen's content to the main `KeepingStockApp` composable, and wrapping that composable in the `KeepingStockTheme`. 

> **Extra Info:** 
> 
> The `MainActivity` is meant to do Sytem UI setup - things like set the theme and handle window size classes (tablet vs phone layout). These are outside the scope of this document for now.

### KeepingStockApp Composable

Right now, the `KeepingStockApp` doesn't do much either, it merely calls the `AppNavGraph`, which is the main navigation component of the app, like the switchboard of the entire app - more on that in the navigation section. 

This means that right now, the navigation component (`AppNavGraph`) takes up and owns the entire screen space. Every UI screen called by `AppNavGraph` will fill the entire window.

This will change when we add a Scaffold composable to the `KeepingStockApp` composable. A scaffold will become the layout container, and the `AppNavGraph` will become content inside the scaffold. The scaffold will also add a `TopAppBar`, a `BottomBar` (if we want), and `SnackbarHost` (also optional - these are little temporary notifications that appear at the bottom of the app). `AppNavGraph` will no longer be "the whole app UI," it'll be the thing that fills the Scaffold's content slot.

Baiscally, navigation determines which screen is displayed, while the Scaffold determines how the screen is framed.

This separation allows screens to remain simple and navigation-agnostic, while app-wide UI behavior is controlled in a single place.

> **Extra Info:** 
> 
> Scaffold should really be a one-per-app shell. We don't want to put a scaffold in each screen or have multiple scaffolds in each destination - that gets messy. The single scaffold can be adjusted for each screen, like changing the top bar title, showing and hiding buttons, some screens may want to hide the bottom bar, etc.
>
> Example: the Camera screen is full-screen, so the Scaffold can hide the top/bottom bars for that destination.

> **Extra Info:**
>
> `KeepingStockApp` might also be useful for other things, like debug overlays, fake repository mode for demos, and toggles for test data. Might add these kinds of things down the line, but right now outside the scope of this document


## Part 2 - Navigation

The biggest components of navigation for our app are `AppNavGraph` and `NavRoute`, both located in `ui.navigation`. 

`AppNavGraph` holds the `NavHost`, where you register routes, what screens those routes lead to, and how those screens are built (the arguments passed to your screen). Think of it like the central routing office, or the switchboard of the App. `AppNavGraph` is the one that gets loaded into content section of the Scaffold in the `KeepingStockApp` composable, and it's also where the starting route/screen of the app gets defined.

`NavRoute`s are the routes themselves, which get registered by `AppNavGraph`. Think of `NavRoute`s like the phone numbers of your screen. Sometimes they're just simple strings, like `item_browser`, and sometimes they can have arguments in the route, like `item_detail/111`

`NavRoute` and `AppNavGraph` work together, but they do two different jobs. `NavRoute` defines route strings and argument structure of those routes, and provides helper functions (i.e. `createRoute`) to build valid routes. The `AppNavGraph` registers those routes with the `NavHost`, extracts arguments from routes, decides which screen composables to show, and wires screen callbacks to navigation actions. `NavRoute` defines addresses, `AppNavGraph` decides what happens when you go to that address.

> **Note:**
>
> This section is longer than the others since it's gotten the most attention during this intial week of coding

### NavRoute Interface 

`NavRoute` isn't anything official, it's just a sealed interface that I wrote, which promises to hold a string variable named route. That's all, pretty simple.

A sealed interface isn't much different from a normal interface, which is just a contract of how an object should look. The sealed part means that only objects defined in the same file can implement the interface. It's basically saying "`NavRoute` can only be one of these listed objects/types".

To that end, you'll notice that all the objects in the interace specifically implement the `NavRoute` interface, and they all include an `override val route: String = `. EVERY object that implements the `NavRoute` interface must have this override, and that is the only requirement.

**Example:**

```kotlin
object ItemBrowser : NavRoute {
    override val route: String = Routes.ITEM_BROWSER
}
```

In this example, `Routes.ITEM_BROWSER` just resolves into the string "item_browser" - you'll find this string constant if you open `core.contracts.Routes`, it's there as `const val ITEM_BROWSER = "item_browser"`. This is the route, or address, or "phone number" that can be referenced using `NavRoute.ItemBrowser.route` outside the `NavRoute` interface. So when you see `NavRoute.ItemBrowser.route`, you know that it will literally just resolve into "item_browser".

That's a simple example. However, some routes require arguments that can’t be known ahead of time. For example, `ItemDetails` requires an itemId in the route, like `item_details/111`, where the `itemId` is 111. This is needed so that we know what item gets displayed on the `ItemDetailsScreen` when we want to navigate to that screen. Since this route has to be built with the `itemId`, we add a `createRoute` function in any object that will need additional work to build a route beyond a simple static string.

There are two types of paths that can be built with arguments: 
 - Path parameters for paths with one argument(e.g. `item_details/111`, where 111 is the `itemId`) 
 - Query parameters for paths with multiple arguments, which can be made optional (e.g. `add_edit_item?itemId=111&containerId=999`, where 111 is the `itemId` and 999 is the `containerId` - think website URLs)

> **Extra Info:**
>
> How exactly these are built are out of scope for this doc, but for examples:
> - `ItemDetails`, `ContainerDetail`, and `Photo` use path parameters.
> - `ContainerBrowser`, `AddEditContainer`, and `AddEditItem` use query parameters.

So quick summary, a `NavRoute` defines the shape of a route, but it does not register the route with the app. Registration happens later in `AppNavGraph`.

### AppNavGraph Composable

This the big navigation file. It holds the `NavHost` and the `navController`, which are important components for navigation in Android. There should only be one `NavHost`, and by extension one `navController` for the entire app. Since they're so important, we try not to mess with them too much.

What they do is really in the name itself. `NavHost` pretty much... well... *hosts* the navigation of the app. As I indicated, it's like the switchboard of the app, all routes lead through here. 

The `navController` is a lot like a control board for the `NavHost`. It's the manager for the `NavHost`, and it tracks the current screen and the back stack (when you press the back button on your phone, it's the thing that remembers the order of previous screens to go back to). 

The main functions called using the `navController` that we use are: 
- `.navigate(NavRoute.SomeNavRoute.route)` which moves forward (adds to the back stack) 
- `.navController.popBackStack()` which goes back (remoes from the back stack)

> **Extra Info:**
>
> There are more advanced options (`popUpTo`, `launchSingleTop`, `restoreState`), which are used in a few places, but those are beyond the scope of this doc for now.

When `NavHost` is declared, the main arguments you pass to it are the `navController` and the `startDestination` of the app itself (in the form of a route string).

> **Extra Info:**
>
> The `NavHost`'s arguments also appear to be where enter and exit transistions can be set! Something to consider for later.

In the `NavHost` block, `NavRoute` routes are registered so that the app knows what screen to go to and how to build the arguments for the screen. Registering the `NavRoute` in the `NavHost` is like telling the switchboard "when someone dials this number, connect them to this person and build the call like this."

> **Extra Info (Advanced)**
>
> Inside the `NavHost {...}` block, routes are registered using a builder lamda (`NavGraphBuilder.() -> Unit`). I'm still wrapping my head around the *exact* mechanism by which this works (I've mostly been working off of existing examples I've found), but you are registering composables using this builder lambda, which is using the `NavGraphBuilder` function to build `NavGraph`s for you. Technically, you are not registering routes themselves to the `NavHost`, you're registering `NavGraph`s. 
>
> It gets more complicated than that, but that's again outside of the scope of this doc! You definitely don't need to understand this to work with navigation.

In each composable registered in the `NavHost` there are arguments for the route, and if the route itself has navigation arguments that need to be extracted, that's done by listing those arguments as navArguments, which are then extracted in the composable using the backStackEntry lambda (where the key to the argument is the same as the name given to the `navArgument`).

From there, right now all that we do is build the UI screen that the route indicates we need to create, building the callback functions using their intended navigation calls with navController and any other arguments required by the Screen!

**Jumping Ahead Slightly:**

The `AppNavGraph` will be where we end up wiring in the ViewModels and how we expose UI state to the screens. They're not there yet, but the way they work is integrated with navigation.

When we add ViewModels and UiState, most of the composables will include a line to obtain the ViewModel scoped to that destination (via a factory if they have a constructor), and another line to observe the UiState from the ViewModel (*observe* and not *build*. This distinction keeps getting hammered to me as important, because it needs to survive recomposition of the composable, which happens when things like "user rotates his phone" occur), and finally pass the UiState alonside the callbacks into the Screen.

See *Part 3 - State & ViewModels* for more details on how ViewModel and UiState gets wired into everything.

### UI Screens - Their Role in Navigation

UI Screens (sometimes just called screens) are the composable functions that actually draw what the user sees on the screen. We've got several example placeholders so far in our `ui.screens` package, any of those are good examples.

They're the most visible, but that's pretty much all they're supposed to really do - look good. 

OK, that's not all they do, but ideally UI is presentation-only. It's supposed to just render UI based on the state (like loading, empty, error, data received), respond to user actions (like the user mashing his finger on a button), and "emit intent" via callback functions that are registered to it (e.g. a screen doesn’t say "navigate to AddEditItemScreen," it says "the user wants to edit this item").

All the navigation stuff actually done by `AppNavGraph` and `NavRoute` doesn't need to be done in the UI Screens 
- No `NavHost`
- No `NavController` 
- Don't call `navController.navigate`
- UI doesn't need to know it's own phone number (i.e. it's `NavRoute`)
- Don't extract any route arguments (**big one**)
- Don't decide which screen to navigate to next (happens in `AppNavGraph`)

Screens mainly communicate using the callback registered in the `AppNavGraph` using the Screen's parameters - so callbacks like `onBack`, `onEdit`, `onOpenItem`, `onAddContainer`, etc. These callbacks are user intent, they're not navigation decisions made by the screen.

Here's an example of a screen signature:
```kotlin
@Composable
fun ItemDetailsScreen(
    itemId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit
)
```

The callbacks (`onBack` and `onEdit`) passed into the screen are wired up to navigation in `AppNavGraph` when the screen is called/built, like the following:
```kotlin
ItemDetailsScreen(
    itemId = itemId,
    onBack = { navController.popBackStack() },
    onEdit = { 
        navController.navigate(
            NavRoute.AddEditItem.createRoute(itemId = itemId)
        )
    }
)

```
> **Small Note**
>
> In the above example, I've changed the signature from the *actual* `ItemDetailsScreen` we have in our app right now. In the one in our app, right now it has `onEdit: (itemId: String) -> Unit`, and the `AppNavGraph` uses a lambda expression to pass the `itemId` to that callback's parameter. This kinda goes against the intended UI responsibility boundaries, since it means the screen is deciding which `itemId` gets edited and has to know the current `itemId`. 
>
> It's like this in the app right now, because no ViewModel or UiState is hooked up yet. Once UiState is introduced, the screen will already have the current item's ID as part of its state, so it no longer needs to pass it back explicitly.

This keeps navigation centralized and organized. Easier to keep track of and consistent. It also allows the screen to be reused, easily previewed, and tested *without* navigation, which is a good feature to have!

Essentially, if you see actual navigation stuff in your screen, it's a sign that the stuff should get refactored into `AppNavGraph`

> **Jumping Ahead**
>
> In the above example, `itemId` is going to be replaced by something like `uiState: ItemDetailsUiState`. This won't effect the `AppNavGraph` example too much aside from passing a different parameter, and a little tweaking of the `onEdit` callback.
> 
> It's worth noting here that screens do **not** create, store, or fetch data. They don't own state, they just render it.
> 
> More on all this in the *State & ViewModels* section.

## Part 3 - State & ViewModels

So far, this document has dealt with how screens are reached (navigation), this part is how screens get the data to display. 

State and ViewModel make up the middle layer between ui and data layers - they're the ViewModel in the name of our overall architectural structure: Model-View-ViewModel (MVVM). It acts as a converter, handling data formatting, validation, and command logic.

So the responsibilities are as follows _(note-to-self: maybe move this section to the start? Or is having a responsibility refresher here helpful)_:

##### UI Screens:
- Draw UI only
- Emit user intent using callbacks
- Receive state as input
- Do not fetch data or mutate state directly

##### ViewModels:
- **Own** the current state for a screen
- Decide how that state changes over time
- Handle user actions
- Survive recomposition and configuration changes

##### UiState:
- Simply put, _what the screen should look like at any given time_
- Includes states like loading, empty, error, and ready states (as in, indicates what the screen should display for each state)
- Is all the UI needs to render the screen

##### Repositories/Database (Under Construction):
- Provide data (items, containers)
- Only accessed by ViewModels
- Not touched by UI/navigation

### Unidirectional Data Flow

One of the core ideas we have to remember for our app is that data is meant to flow only one direction. Data flows down, from ViewModel -> UiState -> Screen, and events are what flow up, from Screen -> callbacks -> ViewModel. So remember the ViewModel owns the UiState, and the UI _observes_ that state. 

**When any part of the state changes, Jetpack Compose automatically recomposes the screen for us.** 

We don't have to trigger anything, and the screen will automatically redraw itself based on the new state. Screen doesn't ask why or make decisions about the state, it just renders what it's given. State and ViewModels decide what screens show and how they behave.

As for events flowing up, this follows from the callbacks provided to the UI. When the user interacts with the UI (e.g. taps a button, selects an item, edits a field, refreshes, etc), the screen just calls a callback and hands control back to the ViewModel, it doesn't change state, load data, or navigate on its own.

> **Extra Info:**
>
> Early on, our screens may still receive raw parameters, call navigation callbacks directly, and have temporary logic in the screen. This is transitory and temporary, and it's fine, but remember that our target model architecture will eventually eliminate these aspects of the UI

### UI State: What exactly is 'State'?

This is one of those terms that keeps popping up as though we should know what it means.

When we say that a screen has "state", we're not just talking about the data it shows. State is the bigger idea of *"If this screen were drawn right now, what should it look like?"* This means more than just data, but also what the current situation is with that data (e.g. is it loading? is it missing? is there an error?).

Since we're talking code, state is fundamentally just an object with variables that the UI will look at and use. For each screen, you can use an interface or a class, but all it is is a set of possible objects that represent how the screen can be at any given time. We assign a variable `_uiState` with `SpecificScreenUiState.Loading`, or `SpecificScreenUiState.Success`, or other such states (keep in mind `SpecificScreenUiState` is a class defined by us, not something already defined in Kotlin).

Ultimately, the ViewModel has a function inside it that decides which of these UI State objects is the current one to use (e.g. `_uiState.value = UiState.Loading`, `_uiState.value = UiState.Error`, `_uiState.value = ItemBrowserUiState.Loading`). When anything in the state changes, the function that assigns what the current state is gets re-run by Jetpack Compose (more on that in the ViewModel section).

#### UI State by example:

Here are two examples of UiState, one made by Rich and the other by David:

```kotlin
/**
 * Example provided by Rich:
 * Uses a sealed class to create a generic result wrapper
 */
sealed class UiState<out T> {

    data object Loading : UiState<Nothing>()

    data class Success<T>(val data: T) : UiState<T>()

    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : UiState<Nothing>()

}
data class ItemBrowserUiData(
    val items: List<Item> = emptyList(),
    val containerId: String? = null
)

/**
 * Example provided by David:
 * Uses a sealed interface for specific screen's UI contract
 */
sealed interface ItemBrowserUiState {

    data class Loading(
        val query: String = ""
    ) : ItemBrowserUiState

    data class Success(
        val query: String = "",
        val items: List<Item> = emptyList(),
        val containerId: String? = null
    ) : ItemBrowserUiState

    data class Error(
        val query: String = "",
        val message: String,
        val cause: Throwable? = null
    ) : ItemBrowserUiState

}
```

Let's break down both examples.

Rich uses a sealed class, which were the default pattern before sealed interfaces (which are apparently newer) came into play. He uses a generic UI state, which is a common pattern for result wrappers that have shared states, i.e. Loading, Success, and Error. He follows it up (later, in the ItemBrowser's ViewModel file) with the ItemBrowserUiData, which is used for the Success state's generic (the `<T>` part), so that he can provide the data that the ItemBrowser screen specifically needs. 

Functionally, this is just UiState saying "Hey, T will represent whatever extra data is needed (if any) by any specific screen to render the state", and then the ItemBrowserUiData is the object that contains the data that the ItemBrowser screen will need, bundled up, and we're saying "In this case, T = ItemBrowserUiData". This keeps the wrapper reusable while still allowing the success case to provide screen-specific data.

David uses a sealed interface, which is more like a tag or a contract. It doesn't use an ItemBrowserUiData, because this UiState isn't generic and uses variables specific to the ItemBrowser screen. It can really only be used by ItemBrowser screens. It avoids bundling fields into a separate `data` payload type variable.

Both of these convey almost exactly the same data, the only difference is that David's implementation added the `query` variable to each state (an extra variable the screen will later need, not important to how the UiState itself works). Remove that variable and suddenly they're conveying exactly the same data: `Loading` has no variables; Error has a `message` and a `cause` variable; `Success` has variables for a `containerId` and for a list of `Item`s, with David's version of those variables being directly in the interface, and Rich's being supplied to his `Success`'s `data` variable using the ItemBrowserUiData for the generic T.

Practically and for our use, they're pretty much the same.

Core distinction is how the screen-specific fields are handled. Rich's generic wrapper is good for standard async results and reuse, but tends to produce `_uiState = UiState.Success(ItemBrowserUiData(...))` which can feel like nested wrapping. David's is a per-screen contract which is for an explicit UI model and is good for when screens need extra context, but duplicates the Loading/Success/Error pattern for each screen. Rich's would require additional generic UI states if other screens end up using different states, like "Editing", "PermissionsRequired", or other states. David's requires a ui state for every screen regardless.

> **Extra Info:**
>
> Rich (correctly) uses data object for his Loading state, mainly because it has no variables, therefore every "loading" object would be identical. Making it a data object turns it into a singleton, which means that there can ever only exist one instance of this object. This avoids allocating multiple identical objects, and it's basically saying "there is only one meaningful Loading state". 
>
> David's Loading state has a variable `query`, which is there because we want the screen to always be able to render the current query even while loading or showing an error. The fact that this version of the Loading state requires a variable is why it's a data class, and if there was no variable then it *would* be a data object instead.
>
> You *could* use a data class instead of a data object for states with no variables, using a data object is mainly for better clarity (and a *small* efficiency boost).

#### More on State

It helps to separate the types of information that often get mixed together. We have the domain data, which is the actual inventory information, like containers, items, tags, photos, and the relationship between them all. This data, including the reletionship between each type, lives in the database. Domain data = what exists in the database.

The other type of data is UI state, which represents what a screen needs in order to render itself. It includes loading, empty, error, or whatever else cases a screen can be in. It might reshape or simplify domain data and it exists only to support the UI itself. So while UI observes the state, state is telling the UI things like:

- "Yo, we're still loading items for this container"
- "This container is empty"
- "The item wasn't found"
- "Show this list of items"
- "Disable the save button while saving so we don't get a bunch of save callbacks over and over again."

So why state and not just the raw data, like just a data class? Why can't we just send raw lists or objects directly into a screen? Well, it's not that we can't, the approach is just very messy and breaks down (especially in the face of how Android does recomposition). It has the potential for nonsense states or for error-prone logic. By having it a sealed class or a sealed interface, we're saying "this screen's state can only be one of these objects: Loading, Success, or Error" and then the UI screen can use a when statement to draw the screen in a specific way based on these known possible states.

Passing state instead of raw data helps to centralize loading and error handling instead of having each screen inventing their own rules - we want screens following the same patterns and we want loading, error, empty, and ready all to be explicit definitions. We don't want the screens checking for nulls, deciding whether the data it sees is 'valid', or enabling or disabling UI based on all sorts of opaque rules unique to each screen. UiState moves these decisions away from the screen.

A container browser screen might move through states of "Loading -> Empty",  "Loading -> Ready", and "Loading -> Error", but we don't care about how that happens, only what the current state is. So the UI logic becomes simple as a result: 

- If state is loading, show a spinner.
- If state is empty, show an empty message
- If state is ready, show list of sub-containers/items
- If state is error, show the error.

So ultimately, every screen will have it's own UiState type, even if two screens show similar data.

> **Add to TODO board:**
>
> Add a UiState type for every screen type, or create generic UiStates that cover each set of possible screen states we'll need. For example, form screens (add/edit) may have states like Editing, Saving, ValidationError, and Saved. Then for utility screens like for the camera, we can have PermissionRequired, Ready, and Processing states. 

### ViewModels - What they are and why we need them

A ViewModel is a screen's manager. It's also the owner of the UiState - it's important that the UI doesn't own it's own state. If a screen is "what it looks like", then ViewModel is "What's going on".

ViewModels are responsible for a lot of the business logic, and is the single source of truth for its screen's state. They are responsible for:
- Owning the current UiState for a screen
- Deciding how that state changes over time
- Handling user actions, like button clicks, edits, refreshes, etc
- Coordinating data loading and updates
- Surviving recomposition and configuration changes

A good general mental model for Screens, ViewModels, and UiStates: Screens **ask** and **present**, ViewModels **decide**, and UiState **describes**

#### Various concepts and terms related to ViewModel, and *why* they're there

At this point, it's helpful to talk about a bunch of terms related to the ViewModel, and most of them exist because of the concept of recomposition and configuration changes.

##### Recomposition:

Recomposition means Compose runs UI functions again when any data tracked by that composable function changes. Like, the whole UI screen function gets run again (e.g. the entire ItemBrowserScreen function). Sometimes this is the whole screen, but often it is only a small part of the UI tree. Compose tracks which state each composable reads and only reruns the affected ones.

It's like refreshing a webpage automatically whenever data changes.

Compose only tracks special observable data types which trigger recomposition, mainly:
- `State<T>` / `MutableState<T>` (from `mutableStateOf`, `collectAsState`, etc.)
- `StateFlow` (after being converted to `State` via `collectAsState`)
- `LiveData` (after being converted via `observeAsState`)

Regular variables, whether supplied as an argument or inside the composable function itself, do NOT count for whether a composable gets recomposed/rerun (unless the value is backed by Compose State trigger)

Recomposition can happen many times per second, for tiny reasons, and usually without the user knowing. It's cheap, so rebuilding composables happen frequently.

##### Configuration Changes:

Configuration is deeper than a recomposition. It's Android saying *"Something about the device changed. I'm rebuilding your screen."* It's caused by things like:

- Rotating the device
- Switching to dark/light mode
- Changing the language
- Split screens
- Font size changes

When configuration changes, the entire app Activity (i.e. the `MainActivity`) is destroyed along with the Composable, and new ones are recreated. _But the ViewModel **survives**_. 

It's like closing and reopening the current screen. Think of it like closing and reopening a document, and the ViewModel is the file.

##### Mutable, State, Flow, remember, Lifecycle: What do these mean?

These are all terms that get mixed together, so you get types and functions like `StateFlow`, `MutableStateFlow`, `collectAsState`, `collectAsStateWithLifecycle`, etc. These all get used in ViewModels (and the screen itself sometimes).

- `Flow` is pipe that sends values over time, a stream of updates.
- `StateFlow` is a special `Flow` that always has a current value, remembers the latest value, and immediately gives it to any listeners. It's "current state + stream of changes"
- `MutableStateFlow` exists because `StateFlow` is read only. `MutableStateFlow`, made in the ViewModel, exists so only the ViewModel can change the state. In the ViewModel you'll have:
    ```kotlin
    private val _state = MutableStateFlow(...)
    val state: StateFlow<UiState> = _state.asStateFlow()
    ```
this is for encapsulation purposes. `MutableStateFlow` is writable by the ViewModel; `StateFlow` is read-only for the UI.
- Lifecycle is Android telling you "Is this screen visible? paused? destroyed?"
- `collectAsState` vs collectAsStateWithLifecycle: `collectAsState` always collects even when the screen is hidden. `collectAsStateWithLifecycle` collects only when the screen is visible/active, and pauses when it's in the background. So `WithLifecycle` is better for the battery and is safer.
- `remember` is what makes local state persist. It's basically "Keep this between reruns". Without remember, the MutableState object itself would be recreated on every recomposition, resetting its value. So `var x = mutableStateOf(0)` would have the 0 forgotten between recompositions, so instead we use `var x by remember { mutableStateOf(0) }`
- `by` is syntactical sugar. It unrwraps the `.value` property automtically and wires reads/writes to the underlying `State` object. `val x by remember { mutableStateOf(0) }` is shorthand for 
    ```kotlin
    val tmp = remember { mutableStateOf(0) }
    val x = tmp.value
    ```

#### Why ViewModel Owns UiState

With those terms out of the way, explaining why ViewModel is the one to own the UiState becomes more obvious. Because composables are frequently recreated and destroyed, they cannot reliably own long-lived state. ViewModels survive recomposition and configuration changes, so they're best suited for owning the UiState.

> **Add to TODO board:**
>
> Each screen should have its own ViewModel responsible for producing that screen's UiState. Screens should never share ViewModels unless they are intentionally sharing UI state.

#### The internals of the ViewModel - what goes on under the hood:

**THIS SECTION IS UNDER CONSTRUCTION**

#### How ViewModels expose UiState

Each screen's ViewModel exposes its UiState in a simple and consistent way: it keeps a mutable version of the state internally, and exposes a read-only StateFlow version to the UI. This way, the ViewModel can change the state, and the UI can only observe it. This is what we mean when we say ViewModel "exposes" UiState. If state changes unexpectedly, there's exactly one place it could have come from: the ViewModel.

Inside each screen's ViewModel, we'll have something like the following as class variables:

```kotlin
private val _uiState = MutableStateFlow<ItemBrowserUiState>(ItemBrowserUiState.Loading)
val uiState: StateFlow<ItemBrowserUiState> = _uiState.asStateFlow()
```

The `_uiState` variable is the mutable version of the State, the uiState variable is the read-only version that the UI screen observes. Making `_uiState` private prevents the UI from modifying state directly. If the ViewModel changes the _uiState value, the UI screen recomposes (redrawn) with the new values.

#### Tying the ViewModel into Navigation ("How are ViewModels even created??")

Up to now, we've gone over ViewModels as if they're simple objects created using constructors, like:

```kotlin
val itemDeetsViewModel = ItemDetailsViewModel(itemId)
```

In practice, ViewModels are never created manually. They are created and managed by the Android system using `ViewModelProvider`.

When a ViewModel has no constructor parameters, this process is hidden and appears automatic. When a ViewModel needs inputs (e.g. IDs, repositories, etc), Android needs instructions on *how* to create the ViewModel in question.

These instructions are provided using a ViewModel factory.

Factories exist primarily because Android must be able to recreate ViewModels later (for example, after rotation, process death, or navigation restoration). The system cannot call arbitrary constructors on its own.

##### ItemDetails example

Let's say we have the ItemDetailsViewModel:

```kotlin
class ItemDetailsViewModel(
    private val itemId: Long,
    private val repository: ItemRepository
) : ViewModel() {
    // ViewModel logic here
}
```

and our destination looks like this before we build the ViewModel:

```kotlin
composable(
    route = NavRoute.ItemDetails.route,
    arguments = listOf(
        navArgument(Routes.Args.ITEM_ID) { type = NavType.StringType }
    )
) { backStackEntry ->
    val itemId = backStackEntry.arguments?.getString(Routes.Args.ITEM_ID)
        ?: error("Missing itemId")

    val repository = ItemRepository.current // or however this will look

    // ... truncated

    // ViewModel creation goes here

    ItemDetailsScreen(
        uiState = viewModel.uiState.collectAsStateWithLifecycle().value,
        // ... truncated
    )
}
```

At this point, we have a few methods of telling Android how to create our `ItemDetailsViewModel`. All of these ways ultimately come back to using the `ViewModelProvider` and stored in the navigation back stack's `ViewModelStore`, so each method is a different way of doing the same thing. There is:

- Named factories, which are explicit and reusable, but a bit verbose
- Anonymous inline factories, which avoids creating a separate class when the factory is only used in one place
- Factory Lambda, which is a modern shortcut of the inline factory method - it avoids casting and boilerplate.


We can build it via a named factory, using an anonymous inline factory, or using a viewModel factory lambda shortcut (there may be other ways, but this isn't exhaustive, so outside the scope of this doc). It doesn't really matter how it's constructed for our purposes, only that it is created by the system's `ViewModelProvider`.

###### Named Factory:

Our first method is a named factory, which would look like this, either in the same file as the screen's destination function or in it's own file:

```kotlin
class ItemDetailsViewModelFactory(
    private val itemId: Long,
    private val repository: ItemRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ItemDetailsViewModel(itemId, repository) as T
    }
}
```

Which would be used in the destination composable like so:

```kotlin
val viewModel: ItemDetailsViewModel = viewModel(
    factory = ItemDetailsViewModelFactory(itemId, repository)
)
```

###### Anonymous Inline Factory:

We put this in the destination's composable function:

```kotlin
val viewModel: ItemDetailsViewModel = viewModel(
    factory = object : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            return ItemDetailsViewModel(
                itemId = itemId,
                repository = repository
            ) as T
        }
    }
)
```

###### Factory Lambda:

Shorter version of the anonymous inline. The `SavedStateHandle` allows the ViewModel to restore state after process death if needed

```kotlin
val viewModel: ItemDetailsViewModel = viewModel(
    factory = viewModelFactory {
        initializer {
            val savedStateHandle =
                createSavedStateHandle()

            ItemDetailsViewModel(
                itemId = itemId,
                repository = repository,
                savedStateHandle = savedStateHandle
            )
        }
    }
)
```

## Part 4 - Data Layer (Repositories and the Database)

## Part 5 - The Media Platform Interfaces

## Part 6 - Final Overview of the App's Flow