# Keeping Stock – Architecture Tutorial

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

So far, this document has dealth with how screens are reached (navigation), this part is how screens get the data to display. It's the middle layer between ui and data layers, the ViewModel in Model-View-ViewModel (MVVM) architecture. It acts as a converter, handling data formatting, validation, and command logic.

So the responsibilities are as follows:

#### UI Screens:
- Draw UI only
- Emit user intent using callbacks
- Receive state as input
- Do not fetch data or mutate state directly

#### ViewModels:
- Own the current state for a screen
- Decide how that state changes over time
- Handle user actions
- Survive recomposition and configuration changes

#### UiState:
- Simply put, _what the screen should look like at any given time_
- Includes loading, empty, error, and ready states (as in, indicates what the screen should display for each state)
- Is all the UI needs to render the screen

#### Repositories/Database (Under Construction):
- Provide data (items, containers)
- Only accessed by ViewModels
- Not touched by UI/navigation

### Unidirectional Data Flow

One of the core ideas we have to remember for our app is that data is meant to flow only one direction. Data flows down, from ViewModel -> UiState -> Screen, and events are what flow up, from Screen -> callbacks -> ViewModel. So remember the ViewModel owns the UiState, and the UI _observes_ that state. When the state changes, Jetpack Compose automatically recomposes the screen for us, we don't have to trigger anything, and the screen will automatically redraw itself based on the new state.

Screen doesn't ask why or make decisions about the state, it just renders what it's given. State and ViewModels decide what screens show and how they behave.

As for events flowing up, this follows from the callbacks provided to the UI. When the user interacts with the UI (e.g. taps a button, selects an item, edits a field, refreshes, etc), the screen just calls a callback and hands control back to the ViewModel, it doesn't change state, load data, or navigate on its own.

> **Extra Info:**
>
> Early on, our screens may still receive raw parameters, call navigation callbacks directly, and have temporary logic in the screen. This is transitory and temporary, and it's fine, but remember that our target model architecture will eventually eliminate these aspects of the UI

### What exactly is 'State'

This is one of those terms that keeps popping up as though we should know what it means. 

When we say that a screen has "state", we're not just talking about the data it shows. State is the bigger idea of *"If this screen were drawn right now, what should it look like?"* This means more than just data, but also what the current situation is with that data (e.g. is it loading? is it missing? is there an error?).

It helps to separate the types of information that often get mixed together. We have the domain data, which is the actual inventory information, like containers, items, tags, photos, and the relationship between them all. This data, including the reletionship between each type, lives in the database. Domain data = what exists in the database.

The other type of data is UI state, which represents what a specific screen needs in order to render itself. It IS screen specific, so each screen gets it's own UiState. It includes loading, empty, and error cases. It might reshape or simplify domain data and it exists only to support the UI itself. So while UI observes the state, state is telling the UI things like:

- "Yo, we're still loading items for this container"
- "This container is empty"
- "The item wasn't found"
- "Show this list of items"
- "Disable the save button while saving so we don't get a bunch of save callbacks over and over again."

So why state and not just the raw data? Why can't we just send raw lists or objects directly into a screen? Well, it's not that we can't, the approach is just very messy and breaks down (especially in the face of how Android does recomposition).

Passing state instead of raw data helps to centralize loading and error handling instead of having each screen inventing their own rules - we want screens following the same patterns and we want loading, error, empty, and ready all to be explicit definitions. We don't want the screens checking for nulls, deciding whether the data it sees is 'valid', or enabling or disabling UI based on all sorts of opaque rules unique to each screen. UiState moves these decisions away from the screen.

UiState is not a sequence of steps, it's a snapshot of the screen at any moment of time. A container browser screen might move through states of "Loading -> Empty",  "Loading -> Ready", and "Loading -> Error", but we don't care about how that happens, only what the current state is. So the UI logic becomes simple as a result: 

- If state is loading, show a spinner.
- If state is empty, show an empty message
- If state is ready, show list of sub-containers/items
- If state is error, show the error.

So ultimately, every screen will have it's own UiState type, even if two screens show similar data. This will probably live in a package like `ui.screens.<feature>.<ScreenName>UiState.kt`

> **TODO:**
>
> Add a UiState type for every screen type

### 


### **THIS SECTION IS CURRENTLY IN PROGRESS**

Here is an example of how the `ItemDetails` composable in the `AppNavGraph` will probably end up changing when we add ViewModels and UiState, best I can figure (we'll need to review these concepts more to get them right, they've been one of my biggest sources of frustration).

Current code for `ItemDetails` composable in `AppNavGraph`:
```kotlin
// Register the ItemDetails destination: when route == "item_details" with itemId,
// show ItemDetailsScreen
composable(
    route = NavRoute.ItemDetails.route,
    arguments = listOf(
        navArgument(Routes.Args.ITEM_ID) { type = NavType.StringType }
    )
) { backStackEntry ->
    val itemId = backStackEntry.arguments?.getString(Routes.Args.ITEM_ID)
        ?: error("Missing itemId")

    ItemDetailsScreen(
        itemId = itemId,
        onBack = { navController.popBackStack()},
        onEdit = { id ->
            navController.navigate(NavRoute.AddEditItem.createRoute(itemId = id))
        }
    )
}
```

How it will end up changing:
```kotlin
composable(
    route = NavRoute.ItemDetails.route,
    arguments = listOf(
        navArgument(Routes.Args.ITEM_ID) { type = NavType.StringType }
    )
) { backStackEntry ->
    val itemId = backStackEntry.arguments?.getString(Routes.Args.ITEM_ID)
        ?: error("Missing itemId")

    // Obtain the ViewModel
    val vm: ItemDetailsViewModel = viewModel(
        factory = ItemDetailsViewModelFactory(itemId = itemId)
    )

    // Observe UiState from the ViewModel
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    ItemDetailsScreen(
        uiState = uiState,
        onBack = { navController.popBackStack() },
        onEdit = {
            navController.navigate(NavRoute.AddEditItem.createRoute(itemId = itemId))
        }
    )
}
```

## Part 4 - Data Layer (Repositories and the Database)

## Part 5 - The Media Platform Interfaces

## Part 6 - Final Overview of the App's Flow