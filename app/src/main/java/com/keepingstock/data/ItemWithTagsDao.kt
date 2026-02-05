package com.keepingstock.data

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
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemWithTagsDao {
    @Transaction
    @Query("""
        SELECT * FROM items
    """)
    fun getAllItemsWithTags(): Flow<List<ItemWithTags>>

    /* ---------- get items in a specific container with tags ---------- */
    @Transaction
    @Query("""
        SELECT * FROM items
        WHERE containerId = :containerId
    """)
    fun getItemsInContainerWithTags(containerId: Long): Flow<List<ItemWithTags>>

    /* ---------- search by item or tag name ---------- */
    @Transaction
    @Query("""
        SELECT DISTINCT items.* FROM items
        LEFT JOIN item_tag ON items.itemId = item_tag.itemId
        LEFT JOIN tags ON item_tag.tagId = tags.tagId
        WHERE items.name LIKE '%' || :query || '%'
            OR tags.name LIKE '%' || :query || '%'
    """)
    fun searchByItemOrTagName(query: String): Flow<List<ItemWithTags>>

    /* ---------- search by item or tag name in a container ---------- */
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

    /* ---------- search items by a tag ---------- */
    @Transaction
    @Query("""
        SELECT DISTINCT items.* FROM items
        INNER JOIN item_tag ON items.itemId = item_tag.itemId
        LEFT JOIN tags ON item_tag.tagId = tags.tagId
        WHERE item_tag.tagId = :tagId
    """)
    fun SearchItemsByTag(
        tagId: Long,
    ): Flow<List<ItemWithTags>>

    /* ---------- get items by a tag in a container ---------- */
    @Transaction
    @Query("""
        SELECT DISTINCT items.* FROM items
        LEFT JOIN item_tag ON items.itemId = item_tag.itemId
        LEFT JOIN tags ON item_tag.tagId = tags.tagId
        WHERE items.containerId = :containerId
            AND item_tag.tagId = :tagId
    """)
    fun getItemsByTagInContainer(
        containerId: Long,
        tagId: Long
    ): Flow<List<ItemWithTags>>
}