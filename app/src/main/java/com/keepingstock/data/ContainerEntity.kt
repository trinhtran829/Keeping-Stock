package com.keepingstock.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "containers",
    indices = [Index("parentContainerId")]
)
data class ContainerEntity (
    @PrimaryKey(autoGenerate = true)
    val containerId: Long = 0,
    val name: String,
    val description: String? = null,
    val imageUri: String? = null,
    val parentContainerId: Long? = null,
    val createdDate: Date = Date()
)