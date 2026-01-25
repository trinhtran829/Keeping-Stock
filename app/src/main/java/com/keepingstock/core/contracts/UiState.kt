package com.keepingstock.core.contracts

/**
 * Shared UI state.
 * Example:
 * val state: UiState<List<Item>> = UiState.Success(items)
 */
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : UiState<Nothing>()
}