package com.keepingstock.data.entities

/*
* This code was generated with the help of Android Basics with Compose course.
* These links
* https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room?continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-compose-unit-6-pathway-2%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-compose-persisting-data-room#4
* https://developer.android.com/training/data-storage/room/defining-data#:~:text=are%20case%2Dinsensitive.-,Define%20a%20primary%20key,Ignore%20val%20picture:%20Bitmap?%20
* document the sample code that led to my code.
*/

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tags",
    indices = [Index(value = ["name"], unique = true)]
)
data class TagEntity (
    @PrimaryKey(autoGenerate = true)
    val tagId: Long = 0,
    val name: String
)