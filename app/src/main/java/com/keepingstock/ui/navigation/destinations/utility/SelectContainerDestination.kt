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
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.navigation.containerIdOrNull
import com.keepingstock.ui.viewmodel.utility.SelectContainerViewModel
import kotlinx.coroutines.flow.collectLatest

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
                        containerRepository = deps.containerRepo
                    )
                }
            }
        )

        val uiState by vm.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(vm) {
            vm.effects.collectLatest { effect ->
                when (effect) {
                    is SelectContainerViewModel.UiEffect.ReturnSelection -> TODO()

                    is SelectContainerViewModel.UiEffect.ShowSnackbar -> TODO()

                    SelectContainerViewModel.UiEffect.NavigateBack ->
                        deps.navController.popBackStack()
                }
            }
        }
    }
}