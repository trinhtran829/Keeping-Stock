package com.keepingstock.data

/*
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
import kotlinx.coroutines.flow.Flow

@Dao
interface ContainerDao {
    @Insert
    suspend fun insert(container: ContainerEntity): Long

    @Update
    suspend fun update(container: ContainerEntity)

    @Delete
    suspend fun delete(container: ContainerEntity)

    @Query("DELETE FROM containers WHERE containerId = :containerId")
    suspend fun deleteById(containerId: Long)

    /* ---------- get root containers ---------- */
    @Query("""
        SELECT * FROM containers
        WHERE parentContainerId IS NULL
        ORDER BY createdDate DESC
    """ )
    suspend fun getRootContainers(): Flow<List<ContainerEntity>>

    /* ---------- get child containers ---------- */
    @Query("""
        SELECT * FROM containers
        WHERE parentContainerId = :containerId
        ORDER BY createdDate DESC
    """ )
    suspend fun getChildContainers(containerId: Long): Flow<List<ContainerEntity>>

    /* ---------- count child containers ---------- */
    @Query("""
        SELECT COUNT(*) FROM containers
        WHERE parentContainerId = :containerId
    """ )
    suspend fun countChildContainers(containerId: Long): Long

    /* ---------- search child containers ---------- */
    @Query("""
        SELECT * FROM containers
        WHERE parentContainerId = :parentId
            AND name LIKE '%' || :query || '%'
        ORDER BY createdDate DESC
    """ )
    suspend fun searchChildContainers(parentId: Long, query: String): Flow<List<ContainerEntity>>
}