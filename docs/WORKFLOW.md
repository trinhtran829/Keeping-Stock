# Keeping Stock – Team Workflow & Architecture Rules

This project uses a single, shared navigation graph and Compose screens that are wired together through callbacks. These rules exist to prevent merge conflicts, to keep the app structure consistent, and to prevent sources of build failures due to unexpected changes to the configurations, libraries, and build.

## Core app structure so far - what lives where

This is updated as the app is built!

- `ui.navigation/`
  - `AppNavGraph` (the only NavHost)
  - `NavRoute` (route patterns + createRoute helpers)
- `core.contracts/Routes`
  - Source of truth for route names + argument keys
- `ui.screens.<feature>/`
  - Feature screens (Container, Item, Media, QR, Debug)
- `core.<feature>/`
  - Non-UI helpers/services (ex: media helpers, parsing, etc.)

## Rules for feature branches

1) **MainActivity should stay thin!**
- MainActivity must only do app setup (theme, window/system UI) and call the root composable (ex: `KeepingStockApp()`).
- Don't put screens, logic, or navigation graphs in MainActivity, please!

2) **We want only one NavHost**
- The app should have only one `NavHost`, which is in `AppNavGraph`.
- Avoid creating additional `NavHost`s anywhere else (screens, activities, helpers, etc). 
- This is also because we don't want y'all having to mess around building your own navigation architecture - it's annoying and too much overhead to worry about in addition to the features you're working on.
- See the "How to test your work" below for the alternative way to test your feature.

3) **Navigation is centralized**
- Route names and argument keys come from `core.contracts.Routes` maintained by Rich.
- Route patterns and `createRoute()` helpers live in the UI-layer `NavRoute` sealed interface. This means that routes that need to be built with path params (e.g. item_detail/<itemId>) or Query params for optional/nullable arguments (e.g. container_browser?containerId=<containerId>) are built using the `createRoute()` function found in `NavRoute`.
- Destination registration happens only in `AppNavGraph`. 
- As an analogy, `core.contracts.Routes` set the rules for the phone numbers, `NavRoute` builds the phone numbers based on the rules and acts like the phone book, and `AppNavGraph` is the central routing authority based on phone numbers it gets from the UI's actions.

4) **Screens do not depend on NavController**
- Screens shouldn't accept a `NavController` parameter.
- Screens shouldn't call `navigate()` directly.
- Instead, screens expose intent callbacks in their parameters and via UI actions (examples):
  - `onBack()`
  - `onOpenItem(itemId: String)`
  - `onAddItem(containerId: String?)`
  - `onEditContainer(containerId: String)`

5) **PR scope and ownership**
- If your change requires new routes or destination wiring, coordinate with the UI and/or integration lead.
- Prefer small PRs: one screen / one component / one service wrapper per PR. 
- Don't do what I did and add like 7 screens AND the entire navigation architecture in one PR!

6) **Do not edit protected files**
- See the protected files section below.
- These files affect everyone and their build. We don't want competing environments or builds.
- If you do need to edit them (example: Richard needed to add Camera libraries), don't change what's already there, only add. 
- If you need help adding dependencies, let us know!
- If you need or think we need to update the versions of any existing dependencies, let us know and we'll make that it's own PR instead of bundling it with a feature PR.

7) **Additional rules to be added as proposed/needed**
- Doing my best here, haven't had to do something like this before!

## How to test your work (without changing navigation)

### Option A: Compose Preview (fastest)
Add a `@Preview` in your screen file using fake data and no-op callbacks.

### Option B: Debug Gallery (emulator testing)
In debug builds, the app starts at **Debug Gallery**. Use it to open your screen in the emulator.

A STEP-BY-STEP GUIDE FOR OPTION B IS PROVIDED BELOW!

- Add emulator testing paths via Debug Gallery instead of editing MainActivity or adding NavHosts.
- If you need a new route/destination, ask the UI and/or integration lead.

## Protected files (do not change!!)

The following files affect the entire project. Most PRs should NOT modify them, unless the PR is dedicated to that modification.

- `app/build.gradle*` and any `build.gradle*`
- `settings.gradle*`
- `gradle.properties`
- `gradle/` (including the wrapper)
- `libs.versions.toml`
- `AndroidManifest.xml` (except for clearly-scoped, required additions like a permission tied to your task)
- GitHub/CI files that will be under `.github/`
- More to be added as we come across them!

If your work requires changing any protected file:
1) Please call it out explicitly in the PR description ("Protected file changes: ...").
2) Explain why each change is required.
3) Keep changes minimal and avoid unrelated edits (do not remove or edit lines unrelated to your additions - other work probably relies on these!).
4) Expect the integration lead to review/merge these changes.
5) If you need to upgrade a library version, that should be it's own entire PR that gets pushed to the whole team - it shouldn't be wrapped together with a feature branch.


## Quick examples for new screens

### Good screen signature
```kotlin
@Composable
fun ItemDetailsScreen(
    itemId: String,
    onBack: () -> Unit,
    onEdit: (itemId: String) -> Unit,
    modifier: Modifier = Modifier
) {
  /* ... */ 
}

@Preview(showBackground = true)
@Composable
private fun ItemDetailsScreenPreview() {
  KeepingStockTheme {
    ItemDetailsScreen(
      itemId = "111",
      onBack = {},
      onEdit = {}
    )
  }
}
```

## To test a screen with Debug Gallery (THIS TUTORIAL SECTION IS IN PROGRESS)

The Debug Gallery is a shared testing tool.

Small additions are okay, but if you’re unsure, ask before editing it extensively to avoid merge conflicts.

This process looks long, but most of it is wiring that already exists, or has a lot of examples you can look at and imitate! You're not expected to memorize it!

If you’re new to Android or Compose, it’s completely fine to:
 - build the screen UI first
 - add a Preview
 - then ask for help with NavRoute/AppNavGraph wiring

There are several steps that must be done - ideally you should ping UI/integration lead (though Rich is not familiar with the process quite yet) to build you a new route, but I understand that's not feasible when you want to test something quickly.

## Step-by-step guide

### Step 1: Design your screen API (UI only)

Your screen composable should expose navigation actions as callback parameters (e.g. `onBack`, `onEdit`, `onOpenOtherScreen`).

Don't use `NavController` inside screens!

See the above example for a good screen signature. Use these callbacks for your UI's actions!

These should usually just be empty functions, that way you can define what these callback actions actually do in AppNavGraph in Step 3.

### Step 2: Create a NavRoute

Each screen needs a `NavRoute`. Think of the NavRoute as the phone number for your screen. Creating a NavRoute is like assigning a phone number and putting it in the phone book.

Routes use the string values defined in core.contracts.Routes for their addresses. To continue the analogy, this defines known and existing area codes (kinda - stretching the analogy a little there. It just holds string constants so we don't have raw strings literals in the NavRoute file).

  - Open the ui.navigation.NavRoute file
  - Add your new route near the bottom under **CUSTOM SCREENS - TO BE FORMALIZED**
  - The NavRoute you make needs to override the route string variable. This is the destination/address of your screen, and can be technically be any string.

Example:

```kotlin
object ItemBrowser : NavRoute {
    override val route: String = Routes.ITEM_BROWSER
}
```
  - Routes.ITEM_BROWSER is simply a constant String defined in the core.contracts.Routes contract that resolves to "item_browser".
    - Before adding a new route constant to Routes, check with the integration/UI lead.
    - For temporary/debug screens, a raw string route is acceptable, but production routes should go through Routes!
  
  **Advanced step:**  
  - If your screen's route/address needs arguments, add a createRoute function
  - See existing examples for: 
    - creating a route with path parameters (e.g. item_details/111, where 111 is the itemId) 
    - creating a route with query parameters (e.g. add_edit_iem?itemId=111&containerId=999)

### Step 3: Register the route in AppNavGraph

This is where your screen is wired into the app.

Think of the AppNavGraph as the central routing office, the switchboard. Registering the NavRoute in the AppNavGraph is like telling the switchboard "when someone dials this number, connect them to this person and build the call like this."

Except it's actually "when this route is encountered, show this screen and build the arguments for the screen like this."

  - Open the ui.navigation.AppNavGraph file
  - Register your new route near the bottom under **CUSTOM SCREEN - TO BE FORMALIZED**

#### **NEW** Step 3.5: Create your destination function

This step has been added because AppNavGraph was becoming monolithic and unwieldy, so all the `NavGraphBuilder` functions within the NavHost block were simply refactored into their own function files. Ultimately, this just means you need to create a `<ScreenName>Destination` file inside of `ui.navigation.destinations`. See the other files for the basic set-up of a desitination composable function. Remember, imitating these functions means you're creating a function for `NavGraphBuilder`!

The route registration takes the form of `composable(route = NavRoute.YourNavRouteName.route)`, where YourNavRouteName is the name of the route you wrote in the NavRoute section (for example, `object ItemBrowser` would have ItemBrowser as the NavRoute name).

  **Advanced step:**  
  - Extract arguments from your NavRoute using backStackEntry if needed
  - See existing examples in AppNavGraph for guidance, or ping the UI/integration lead on Discord for help
  
Add the call to your screen here. Any callback function arguments you included in your screen's parameters in Step 1 will also need to be resolved here!

If your screen opens any additional screens, this is where you can define the behavior of your callbacks to navigate to those screen's registered routes

### Step 4: Add an entry to the Debug Gallery

This lets you test your screen in the emulator by adding a button to the debug gallery

Since I've already extended the analogy this far, adding the button is like installing a new speed dial button on your phone. It's not actually programmed to call that number yet, but now we have a button that we'll be able to say "when pressed, dial a number I'll define in AppNavGraph."

  - Open ui.screens.debug.DebugGalleryScreen
  - Add a new callback parameter in the DebugGalleryScreen signature
  - Add a DebugButton below the others that uses this new callback function
  - See existing callback functions and DebugButtons for examples

### Step 5: Wire Debug Gallery navigation
 The DebugGalleryScreen's registered route needs to be updated in the AppNavGraph to accomodate the new callback function added in Step 4

 Last part of the analogy - need to actually update the registered behavior of debug gallery screen because of the new speed dial button (tell the switchboard, "when the speed dial button is pressed, this is the number that should be dialed")

  - Open the ui.navigation.AppNavGraph again
  - Scroll to the very bottom, under the "Register Debug Screens" section
  - Add your `onOpen<your-screen-name>` callback function to the arguments passed to the debug gallery screen.
  - Use `navController.navigate(NavRoute.YourNavRouteName.route)`, like the other onOpen callback examples

You should now be able to run the app and see your screen's entry/preview button in the Debug Gallery!

Again, this can be overwhelming at first, and if you need help just ping us and we'll all help best we can!