package com.keepingstock.core.contracts

import android.os.Bundle
import com.keepingstock.data.entities.ItemStatus
import java.util.Date

// Moved to using @JvmInLine for type safety and for overloading toNavString
@JvmInline value class ContainerId(val value: Long)
@JvmInline value class ItemId(val value: Long)
@JvmInline value class TagId(val value: Long)

data class Tag(
    val id: TagId,
    val name: String
)

data class Item(
    val id: ItemId,
    val name: String,
    val description: String? = null,
    val imagePath: String? = null,
    val containerId: ContainerId? = null,
    val status: ItemStatus,
    val createdDate: Date = Date(),
    val checkoutDate: Date? = null,
    val tags: List<Tag> = emptyList(),
)

data class Container(
    val id: ContainerId,
    val name: String,
    val description: String? = null,
    val imageUri: String? = null,
    val parentContainerId: ContainerId? = null,
    val createdDate: Date = Date()
)