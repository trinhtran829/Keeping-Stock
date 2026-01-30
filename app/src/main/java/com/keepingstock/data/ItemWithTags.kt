package com.keepingstock.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ItemWithTags (
    @Embedded val item: ItemEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ItemTagEntity::class,
            parentColumn = "itemId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)