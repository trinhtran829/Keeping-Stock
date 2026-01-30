package com.keepingstock.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "items",
    indices = [Index("containerId")]
)
data class ItemEntity (
    @PrimaryKey(autoGenerate = true)
    val itemId: Long = 0,
    val name: String,
    val description: String? = null,
    val imageUri: String? = null,
    val containerId: Long? = null,
    val status: ItemStatus,
    val createdDate: Date = Date(),
    val checkoutDate: Date? = null
)