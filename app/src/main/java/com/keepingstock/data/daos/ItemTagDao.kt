package com.keepingstock.data.daos

/**
* This code was generated with the help of Android Basics with Compose course.
* The following links
* https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room?authuser=1&continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-compose-unit-6-pathway-2%3Fauthuser%3D1%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-compose-persisting-data-room#5
* https://www.w3schools.com/sql/default.asp
* document the sample code that led to my code.
*/

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.keepingstock.data.entities.ItemTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemTagDao {
    /* ---------- link an item to a tag ---------- */
    @Insert
    suspend fun insert(itemTag: ItemTagEntity)

    /* ---------- unlink a tag from an item ---------- */
    @Query("""
        DELETE FROM item_tag
        WHERE itemId = :itemId AND tagId = :tagId
    """)
    suspend fun delete(itemId: Long, tagId: Long)

    /* ---------- remove all tag association for a single item ---------- */
    @Query("""
        DELETE FROM item_tag
        WHERE itemId = :itemId
    """)
    suspend fun deleteAllTagsForItem(itemId: Long)

    /* ---------- count items with a tag ---------- */
    @Query("""
        SELECT COUNT(*) FROM item_tag
        WHERE tagId = :tagId
    """)
    suspend fun countItemsWithTag(tagId: Long): Long

    /* ---------- observe all tag ids associated with an item ---------- */
    @Query("""
        SELECT tagId FROM item_tag
        WHERE itemId = :itemId
    """)
    fun getTagIdsFromItem(itemId: Long): Flow<List<Long>>
}