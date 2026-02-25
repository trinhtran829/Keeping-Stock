package com.keepingstock.data.mapper

import com.keepingstock.core.contracts.Tag
import com.keepingstock.core.contracts.TagId
import com.keepingstock.data.entities.TagEntity

/**
 * This code was generated with help the following links
 * https://chatgpt.com/share/699e7311-df34-800a-a660-f687c32187a8
 * These links document the sample code that led to my code.
 */

/**
 * Extension functions to convert Database Entities to Domain Models
 */

fun TagEntity.toDomain(): Tag {
    return Tag(
        id = TagId(tagId),
        name = name
    )
}

fun Tag.toEntity(): TagEntity {
    return TagEntity(
        tagId = id.value,
        name = name
    )
}

