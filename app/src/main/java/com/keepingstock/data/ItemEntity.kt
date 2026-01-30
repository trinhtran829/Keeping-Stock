package com.keepingstock.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/*
* This code was generated with the help of Android Basics with Compose course.
* These links
* https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room?continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-compose-unit-6-pathway-2%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-compose-persisting-data-room#4
* https://developer.android.com/training/data-storage/room/defining-data#:~:text=are%20case%2Dinsensitive.-,Define%20a%20primary%20key,Ignore%20val%20picture:%20Bitmap?%20
* document the sample code that led to my code.
*/

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