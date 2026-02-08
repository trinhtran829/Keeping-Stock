package com.keepingstock.ui.models

import com.keepingstock.data.ItemStatus

data class ContainerSummaryUi(
    val id: Long,
    val name: String,
    val description: String? = null,
    val imageUri: String? = null
)

data class ItemSummaryUi(
    val id: Long,
    val name: String,
    val description: String? = null,
    val imageUri: String? = null,
    val status: ItemStatus? = null,
    val tagNames: List<String> = emptyList()
)