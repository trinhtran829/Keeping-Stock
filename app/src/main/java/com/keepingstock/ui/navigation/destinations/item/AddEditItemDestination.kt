package com.keepingstock.ui.navigation.destinations.item

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Routes
import com.keepingstock.core.contracts.Tag
import com.keepingstock.core.contracts.TagId
import com.keepingstock.core.contracts.uistates.container.AddEditContainerUiState
import com.keepingstock.core.contracts.uistates.item.AddEditItemIntent
import com.keepingstock.core.contracts.uistates.item.AddEditItemUiState
import com.keepingstock.ui.components.navigation.DemoMode
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.navigation.containerIdOrNull
import com.keepingstock.ui.navigation.itemIdOrNull
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.item.AddEditItemScreen

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

        var readyState by remember(itemId, containerId) {
            mutableStateOf(

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

        AddEditItemScreen(
            itemId = itemId,
            containerId = containerId,
            onSave = { deps.navController.popBackStack() },
            onCancel = { deps.navController.popBackStack() }
        )
    }
}

private fun addEditItemTopBarConfig(uiState: AddEditItemUiState): TopBarConfig {
    val title = when (uiState) {
        AddEditItemUiState.Loading -> "Loading"
        is AddEditItemUiState.Error -> "Item"
        is AddEditItemUiState.Ready ->
            if (uiState.mode == AddEditItemUiState.Ready.Mode.CREATE) "Add Item" else "Edit Item"
    }

    return TopBarConfig(title = title, showBack = true)
}

private class AddEditItemDemoController(
    private val deps: NavDeps,
    private val knownTags: List<Tag>,
    private val getUiState: () -> AddEditItemUiState,
    private val setUiState: (AddEditItemUiState) -> Unit
) {
    fun onIntent(intent: AddEditItemIntent) {
        val current = getUiState()

        when (intent) {
            // Navigation related
            AddEditItemIntent.SaveClicked -> onSave(current)
            AddEditItemIntent.BackClicked,
            AddEditItemIntent.DiscardChangesConfirmed -> deps.navController.popBackStack()

            // Screen handles, destination currently consumes only
            AddEditItemIntent.PickImageClicked,
            AddEditItemIntent.DismissDiscardDialog -> Unit

            // Set UI State based on intent - call reducer function
            AddEditItemIntent.AddQueryAsTagClicked -> TODO()
            AddEditItemIntent.ClearQuery -> TODO()
            is AddEditItemIntent.ContainerChanged -> TODO()
            is AddEditItemIntent.DescriptionChanges -> TODO()
            is AddEditItemIntent.ExistingTagSelected -> TODO()
            is AddEditItemIntent.ImagePicked -> TODO()
            is AddEditItemIntent.NameChanged -> TODO()
            is AddEditItemIntent.QueryChanged -> TODO()
            is AddEditItemIntent.RecommendedTagSelected -> TODO()
            AddEditItemIntent.RefreshRecommendations -> TODO()
            AddEditItemIntent.RemoveImageClicked -> TODO()
            is AddEditItemIntent.RemoveTagClicked -> TODO()
            is AddEditItemIntent.StatusChanged -> TODO()
        }
    }

    fun onSave(current: AddEditItemUiState) {

    }
}

private fun reduceAddEditItemIntent(
    current: AddEditItemUiState.Ready,
    intent: AddEditItemIntent,
    knownTags: List<Tag>
) {
    
}