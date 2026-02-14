package com.keepingstock.data.daos

/**
* This code was generated with the help of Android Basics with Compose course.
* The following links
* https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room?authuser=1&continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-compose-unit-6-pathway-2%3Fauthuser%3D1%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-compose-persisting-data-room#5
* https://www.w3schools.com/sql/default.asp
* document the sample code that led to my code.
*/

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.keepingstock.data.entities.ItemStatus
import com.keepingstock.data.entities.ItemEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ItemDao {
    @Insert
    suspend fun insert(item: ItemEntity): Long

    @Update
    suspend fun update(item: ItemEntity)

    @Delete
    suspend fun delete(item: ItemEntity)

    /* ---- update item status and manage checkout date -logic comes from repository ---- */
    @Query("""
        UPDATE items
        SET
            status = :status,
            checkoutDate = :checkoutDate
        WHERE itemId = :itemId
    """ )
    suspend fun updateItemStatus(
        itemId: Long,
        status: ItemStatus,
        checkoutDate: Date?
    )

    /* ---------- delete item by ID ---------- */
    @Query("DELETE FROM items WHERE itemId = :itemId")
    suspend fun deleteById(itemId: Long)

    /* ---------- get item by ID ---------- */
    @Query("SELECT * FROM items WHERE itemId = :itemId")
    suspend fun getItemById(itemId: Long): ItemEntity?

    /* ---------- observe all items ---------- */
    @Query("""
        SELECT * FROM items
        ORDER BY createdDate DESC
    """ )
    fun getItems(): Flow<List<ItemEntity>>

    /* ---------- observe items inside a specific container ---------- */
    @Query("""
        SELECT * FROM items
        WHERE containerId = :containerId
        ORDER BY createdDate DESC
    """ )
    fun getItemsInContainer(containerId: Long): Flow<List<ItemEntity>>

    /* ---------- observe items where containers are null ---------- */
    @Query("""
        SELECT * FROM items
        WHERE containerId IS NULL
        ORDER BY createdDate DESC
    """ )
    fun getItemsUnsorted(): Flow<List<ItemEntity>>

    /* ---------- count items inside a container ---------- */
    @Query("""
        SELECT COUNT(*) FROM items
        WHERE containerId = :containerId
    """ )
    suspend fun countItemsInContainer(containerId: Long): Long
}