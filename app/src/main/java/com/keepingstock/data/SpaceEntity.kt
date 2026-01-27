package com.keepingstock.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spaces")
data class SpaceEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val imageUri: String,
    val parentSpaceId: Long
)