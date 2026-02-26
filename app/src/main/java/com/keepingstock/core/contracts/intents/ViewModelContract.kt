package com.keepingstock.core.contracts.intents

import kotlinx.coroutines.flow.StateFlow

/**
 * A basic ViewModelContract to be implemented by each ViewModel. It is used to specify UiState and
 * intent models.
 */
interface ViewModelContract<T, G> {
    val uiState: StateFlow<T>
    fun onIntent(intent: G)
}