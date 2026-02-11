package com.keepingstock.data.database

/*
* This code was generated with the help of Android Basics with Compose course.
* This link
* https://developer.android.com/training/data-storage/room/referencing-data
* documents the sample code that led to my code.
*/

import androidx.room.TypeConverter
import com.keepingstock.data.entities.ItemStatus
import java.util.Date

class Converter {
    @TypeConverter
    fun fromItemStatus(status: ItemStatus): String {
        return status.name
    }
    @TypeConverter
    fun toItemStatus(status: String): ItemStatus {
        return ItemStatus.valueOf(status)
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time?.toLong()
    }
    @TypeConverter
    fun toDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }
}