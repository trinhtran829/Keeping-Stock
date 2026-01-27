package com.keepingstock.data

import androidx.room.TypeConverter

class Converter {
    @TypeConverter
    fun fromItemStatus(status: ItemStatus): String {
        return status.name
    }
    @TypeConverter
    fun toItemStatus(status: String): ItemStatus {
        return ItemStatus.valueOf(status)
    }
}