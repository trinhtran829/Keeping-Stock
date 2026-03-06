package com.keepingstock.core.contracts.intents.utility

import com.keepingstock.core.contracts.Container

sealed interface SelectContainerIntent {
    data class EnterContainer(val container: Container?) : SelectContainerIntent

    data object Cancel: SelectContainerIntent
    data object Confirm: SelectContainerIntent
}