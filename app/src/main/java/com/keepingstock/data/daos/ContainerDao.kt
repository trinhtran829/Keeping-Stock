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
import com.keepingstock.data.entities.ContainerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContainerDao {
    @Insert
    suspend fun insert(container: ContainerEntity): Long

    @Update
    suspend fun update(container: ContainerEntity)

    @Delete
    suspend fun delete(container: ContainerEntity)

    /* ---------- delete a container by its ID ---------- */
    @Query("DELETE FROM containers WHERE containerId = :containerId")
    suspend fun deleteById(containerId: Long)

    /* ---------- observe containers with no parent ---------- */
    @Query("""
        SELECT * FROM containers
        WHERE parentContainerId IS NULL
        ORDER BY createdDate DESC
    """ )
    fun getRootContainers(): Flow<List<ContainerEntity>>

    /* ---------- observe direct child containers of current container ---------- */
    @Query("""
        SELECT * FROM containers
        WHERE parentContainerId = :currentContainerId
        ORDER BY createdDate DESC
    """ )
    fun getChildContainers(currentContainerId: Long): Flow<List<ContainerEntity>>

    /* ---------- count direct child containers of current container ---------- */
    @Query("""
        SELECT COUNT(*) FROM containers
        WHERE parentContainerId = :currentContainerId
    """ )
    suspend fun countChildContainers(currentContainerId: Long): Long

    /* ---------- search direct child containers by name ---------- */
    @Query("""
        SELECT * FROM containers
        WHERE parentContainerId = :currentContainerId
            AND name LIKE '%' || :query || '%'
        ORDER BY createdDate DESC
    """ )
    fun searchChildContainers(
        currentContainerId: Long,
        query: String
    ): Flow<List<ContainerEntity>>

    /* ---------- get a container entity by container Id ---------- */
    @Query("""
        SELECT * FROM containers
        WHERE containerId = :containerId
    """ )
    suspend fun searchChildContainers(
        containerId: Long,
    ): ContainerEntity?
}