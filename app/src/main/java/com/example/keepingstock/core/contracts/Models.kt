package com.example.keepingstock.core.contracts

// TODO migrate to @JvmInline value class for stronger type-safety
typealias ItemId = String
typealias ContainerId = String

data class Tag(val name: String)

data class Item(
    val id: ItemId,
    val name: String,
    val description: String? = null,
    val containerId: ContainerId? = null,
    val tags: List<Tag> = emptyList(),
    val imagePath: String? = null
)

data class Container(
    val id: ContainerId,
    val name: String,
    val parentContainerId: ContainerId? = null
)