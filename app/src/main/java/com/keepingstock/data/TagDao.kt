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
interface TagDao {
    @Insert
    suspend fun insert(tag: TagEntity): Long

    @Update
    suspend fun update(tag: TagEntity)

    @Query("""
        UPDATE tags
        SET name = :newName
        WHERE tagId = :tagId
    """)
    suspend fun updateName(tagId: Long, newName: String)

    @Delete
    suspend fun delete(container: ContainerEntity)

    @Query("DELETE FROM tags WHERE tagId = :tagId")
    suspend fun deleteById(tagId: Long)

    @Query("DELETE FROM tags WHERE name = :name")
    suspend fun deleteByName(name: String)

    /* ---------- get all tags ---------- */
    @Query("""
        SELECT * FROM tags
        ORDER BY name ASC
    """ )
    suspend fun getTags(): Flow<List<TagEntity>>

    /* ---------- get tag by name ---------- */
    @Query("""
        SELECT * FROM tags
        WHERE name = :name
        LIMIT 1
    """ )
    suspend fun getTagByName(name: String): TagEntity?

    /* ---------- get tag by ID ---------- */
    @Query("""
        SELECT * FROM tags
        WHERE tagId = :tagId
    """ )
    suspend fun getTagById(tagId: Long): TagEntity?

}