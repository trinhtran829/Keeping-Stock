package com.keepingstock.data.entities

/*
* This code was generated with the help of Android Basics with Compose course.
* This link
* https://developer.android.com/training/data-storage/room/relationships/many-to-many
* documents the sample code that led to my code.
*/

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ItemWithTags (
    @Embedded val item: ItemEntity,
    @Relation(
        parentColumn = "itemId",
        entityColumn = "tagId",
        associateBy = Junction(
            value = ItemTagEntity::class,
            parentColumn = "itemId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)