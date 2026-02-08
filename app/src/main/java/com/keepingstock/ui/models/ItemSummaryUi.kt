package com.keepingstock.ui.models

import com.keepingstock.data.ItemStatus

/**
 * UI-friendly summary of an item.
 *
 * This model is intentionally decoupled from Room entities.
 * ViewModels are responsible for mapping entities into this type.
 */
data class ItemSummaryUi(
    val id: Long,
    val name: String,
    val description: String? = null,
    val imageUri: String? = null,
    val status: ItemStatus? = null,
    val tagNames: List<String> = emptyList()
)