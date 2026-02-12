package com.keepingstock.data.daos

/*
* This code was generated with the help of Android Basics with Compose course.
* The following links
* https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room?authuser=1&continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-compose-unit-6-pathway-2%3Fauthuser%3D1%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-compose-persisting-data-room#5
* https://www.w3schools.com/sql/default.asp
* https://developer.android.com/training/data-storage/room/relationships/many-to-many
* document the sample code that led to my code.
*/

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.keepingstock.data.entities.ItemWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemWithTagsDao {
    /* ---------- observe all items with their associated tags ---------- */
    @Transaction
    @Query("""
        SELECT * FROM items
    """)
    fun getAllItemsWithTags(): Flow<List<ItemWithTags>>

    /* ---------- observe items in a specific container with associated tags ---------- */
    @Transaction
    @Query("""
        SELECT * FROM items
        WHERE containerId = :containerId
    """)
    fun getItemsInContainerWithTags(containerId: Long): Flow<List<ItemWithTags>>

    /* ---------- observe items with name or tag matches the query ---------- */
    @Transaction
    @Query("""
        SELECT DISTINCT items.* FROM items
        LEFT JOIN item_tag ON items.itemId = item_tag.itemId
        LEFT JOIN tags ON item_tag.tagId = tags.tagId
        WHERE items.name LIKE '%' || :query || '%'
            OR tags.name LIKE '%' || :query || '%'
    """)
    fun searchByItemOrTagName(query: String): Flow<List<ItemWithTags>>

    /* ---------- observe items in a container with name or tag matches the query ---------- */
    @Transaction
    @Query("""
        SELECT DISTINCT items.* FROM items
        LEFT JOIN item_tag ON items.itemId = item_tag.itemId
        LEFT JOIN tags ON item_tag.tagId = tags.tagId
        WHERE items.containerId = :containerId
            AND (items.name LIKE '%' || :query || '%'
            OR tags.name LIKE '%' || :query || '%')
    """)
    fun searchByItemOrTagNameInContainer(
        containerId: Long,
        query: String
    ): Flow<List<ItemWithTags>>

    /* ---------- observe items associated with a specific tag ---------- */
    @Transaction
    @Query("""
        SELECT DISTINCT items.* FROM items
        INNER JOIN item_tag ON items.itemId = item_tag.itemId
        LEFT JOIN tags ON item_tag.tagId = tags.tagId
        WHERE item_tag.tagId = :tagId
        ORDER BY items.createdDate DESC
    """)
    fun searchItemsByTag(
        tagId: Long,
    ): Flow<List<ItemWithTags>>

    /* ---------- observe items in a container associated with a specific tag ---------- */
    @Transaction
    @Query("""
        SELECT DISTINCT items.* FROM items
        LEFT JOIN item_tag ON items.itemId = item_tag.itemId
        LEFT JOIN tags ON item_tag.tagId = tags.tagId
        WHERE items.containerId = :containerId
            AND item_tag.tagId = :tagId
        ORDER BY items.createdDate DESC
    """)
    fun getItemsByTagInContainer(
        containerId: Long,
        tagId: Long
    ): Flow<List<ItemWithTags>>
}