package com.keepingstock.data

import androidx.room.TypeConverter
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