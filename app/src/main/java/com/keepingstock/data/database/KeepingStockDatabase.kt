package com.keepingstock.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.keepingstock.data.daos.ContainerDao
import com.keepingstock.data.daos.ItemDao
import com.keepingstock.data.daos.ItemTagDao
import com.keepingstock.data.daos.ItemWithTagsDao
import com.keepingstock.data.daos.TagDao
import com.keepingstock.data.entities.ContainerEntity
import com.keepingstock.data.entities.ItemEntity
import com.keepingstock.data.entities.ItemTagEntity
import com.keepingstock.data.entities.TagEntity

/**
* This code was generated with the help of Android Basics with Compose course.
* These links
* https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room?authuser=1&continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-compose-unit-6-pathway-2%3Fauthuser%3D1%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-compose-persisting-data-room#6
* document the sample code that led to my code.
*/

/**
 * Database class with a singleton Instance object.
 */
@TypeConverters(Converter::class)
@Database(entities = [
    ItemEntity::class,
    ContainerEntity::class,
    TagEntity::class,
    ItemTagEntity::class
    ],
    version = 1,
    exportSchema = false)
abstract class KeepingStockDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao
    abstract fun containerDao(): ContainerDao
    abstract fun tagDao(): TagDao
    abstract fun itemTagDao(): ItemTagDao
    abstract fun itemWithTagsDao(): ItemWithTagsDao

    companion object {
        @Volatile
        private var Instance: KeepingStockDatabase? = null

        fun getDatabase(context: Context): KeepingStockDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    KeepingStockDatabase::class.java,
                    "keepingstock_database"
                )
                    .build()
                    .also { Instance = it }
            }
        }
    }
}