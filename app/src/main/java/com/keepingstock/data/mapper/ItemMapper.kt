package com.keepingstock.data.mapper

import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.Tag
import com.keepingstock.data.entities.ItemEntity
import com.keepingstock.data.entities.ItemWithTags

/**
 * This code was generated with help the following links
 * https://chatgpt.com/share/699e7311-df34-800a-a660-f687c32187a8
 * These links document the sample code that led to my code.
 */

/**
 * Extension functions to convert Database Entities to Domain Models
 */

fun ItemEntity.toDomain(tags: List<Tag> = emptyList()): Item {
    return Item(
        id = ItemId(itemId),
        name = name,
        description = description,
        imageUri = imageUri,
        containerId = containerId?.let { ContainerId(it) },
        status = status,
        createdDate = createdDate,
        checkoutDate = checkoutDate,
        tags = tags
    )
}

fun Item.toEntity(): ItemEntity {
    return ItemEntity(
        itemId = id.value,
        name = name,
        description = description,
        imageUri = imageUri,
        containerId = containerId?.value,
        status = status,
        createdDate = createdDate,
        checkoutDate = checkoutDate
    )
}

fun ItemWithTags.toDomain(): Item {
    return item.toDomain(
        tags = tags.map { it.toDomain() }
    )
}



