package com.keepingstock.core.contracts.intents.utility

import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId

sealed interface SelectContainerIntent {
    data class EnterContainer(val containerId: ContainerId?) : SelectContainerIntent
    data class ClickBreadcrumb(val containerId: ContainerId?) : SelectContainerIntent
    data class ChangeSelection(val containerId: ContainerId?) : SelectContainerIntent

    data object Cancel: SelectContainerIntent
    data object Confirm: SelectContainerIntent
}