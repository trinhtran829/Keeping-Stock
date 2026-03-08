package com.keepingstock.ui.navigation.destinations.utility

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.keepingstock.core.contracts.Routes
import com.keepingstock.core.contracts.Routes.SubjectType
import com.keepingstock.core.contracts.uistates.utility.SelectContainerUiState
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.navigation.containerIdOrNull
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.utility.SelectContainerScreen
import com.keepingstock.ui.viewmodel.utility.SelectContainerViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Registers the Select Container destination and wires navigation arguments, state collection,
 * and result-return behavior.
 *
 * This destination displays the Select Container flow for either a container or an item, collects
 * [SelectContainerViewModel.UiEffect] emissions, and returns the confirmed destination selection
 * to the previous back stack entry through its saved state handle.
 *
 * @param deps: Navigation and UI dependencies used to build the destination and handle side effects.
 */
internal fun NavGraphBuilder.addSelectContainerDestination(
    deps: NavDeps
) {
    composable(
        route = NavRoute.SelectContainer.route,
        arguments = listOf(
            navArgument(Routes.Args.SUBJECT_TYPE) { type = NavType.StringType },
            navArgument(Routes.Args.SUBJECT_ID) { type = NavType.LongType },
            navArgument(Routes.Args.CONTAINER_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val args = backStackEntry.arguments ?: error("Missing args")
        val subjectType = SubjectType.from(args.getString(Routes.Args.SUBJECT_TYPE)!!)
        val subjectId = args.getLong(Routes.Args.SUBJECT_ID)
        val currentContainerId = args.containerIdOrNull(Routes.Args.CONTAINER_ID)

        val vm: SelectContainerViewModel = viewModel(
            viewModelStoreOwner = backStackEntry,
            factory = viewModelFactory {
                initializer {
                    SelectContainerViewModel(
                        subjectType = subjectType,
                        subjectId = subjectId,
                        currentContainerId = currentContainerId,
                        containerRepository = deps.containerRepo,
                        itemRepository = deps.itemRepo
                    )
                }
            }
        )

        val uiState by vm.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(vm) {
            vm.effects.collectLatest { effect ->
                when (effect) {
                    is SelectContainerViewModel.UiEffect.ReturnSelection -> {
                        // Write result to previous entry and pop
                        deps.navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(effect.resultKey, effect.selectedContainerId?.value)

                        deps.navController.popBackStack()
                    }

                    is SelectContainerViewModel.UiEffect.ShowSnackbar ->
                        deps.showSnackbar(effect.message)

                    SelectContainerViewModel.UiEffect.NavigateBack ->
                        deps.navController.popBackStack()
                }
            }
        }

        val topBarConfig = buildTopBarConfig(uiState)

        LaunchedEffect(topBarConfig.title, topBarConfig.showBack) {
            deps.onTopBarChange(topBarConfig)
        }

        SelectContainerScreen(
            uiState = uiState,
            onIntent = vm::onIntent,
            onNavigateBack = { deps.navController.popBackStack() }
        )
    }
}

/**
 * Builds the TopBarConfig for the destination from UiState.
 *
 * TODO: refine titles
 *
 * Back button:
 * - Shown always // TODO: correct behavior?
 *
 * @param uiState: The current UI state for the screen.
 * @return: TopBarConfig used by the app scaffold's top bar.
 */
private fun buildTopBarConfig(
    uiState: SelectContainerUiState,
): TopBarConfig {
    val title = when (uiState) {
        is SelectContainerUiState.Ready ->
            "Moving ${if (uiState.subjectType == SubjectType.Container) "Container" else "Item"}: " +
                    uiState.subjectName
        is SelectContainerUiState.Loading -> "Loading…"
        is SelectContainerUiState.Error -> "Moving Err"
    }

    return TopBarConfig(title = title, showBack = true)
}