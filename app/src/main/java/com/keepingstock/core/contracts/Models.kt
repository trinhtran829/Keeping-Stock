package com.keepingstock.core.contracts

import com.keepingstock.data.entities.ItemStatus

// TODO migrate to @JvmInline value class for stronger type-safety
typealias ItemId = Long
typealias ContainerId = Long

data class Tag(val name: String)

data class Item(
    val id: ItemId,
    val name: String,
    val description: String? = null,
    val containerId: ContainerId? = null,
    val tags: List<Tag> = emptyList(),
    val imagePath: String? = null,
    val status: ItemStatus
)

data class Container(
    val id: ContainerId,
    val name: String,
    val parentContainerId: ContainerId? = null
)