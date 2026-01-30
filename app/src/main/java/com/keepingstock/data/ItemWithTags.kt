package com.keepingstock.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/*
* This code was generated with the help of Android Basics with Compose course.
* This link
* https://developer.android.com/training/data-storage/room/relationships/many-to-many
* documents the sample code that led to my code.
*/

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