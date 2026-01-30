package com.keepingstock.data

import androidx.room.Entity

@Entity(
    tableName = "item_tag",
    primaryKeys = ["itemId", "tagId"]
)
data class ItemTagEntity (
    val itemId: Long,
    val tagId: Long
)