package com.keepingstock.data.repositories

import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.Tag
import com.keepingstock.core.contracts.TagId
import kotlinx.coroutines.flow.Flow

/**
 * This code was generated with the help of the following links
 * https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room?authuser=1&continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-compose-unit-6-pathway-2%3Fauthuser%3D1%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-compose-persisting-data-room#7
 * These links document the sample code that led to my code.
 */

interface TagRepository {
    /**
     * Create tag
     * Rule:
     * Trim leading and trailing white spaces
     * Collapse multiple white spaces into one
     * Convert to lower case
     * case insensitive prevent duplication - reuse existing tag if found
     * Throws [IllegalStateException] if tag name is invalid
     */
    suspend fun createTag(name: String): Tag

    /**
     * Update tag
     * Rule:
     * Cannot rename to an existing name
     * Throws [IllegalStateException] if target name is invalid
     * Throws [IllegalStateException] if target name is taken
     */
    suspend fun updateTag(tag: Tag)

    /**
     * Delete tag
     * Rule:
     * Cannot delete tag still associate with an item
     * Throws [IllegalStateException] if tag still associate with an item
     */
    suspend fun deleteTag(tag: Tag)

    /**
     * Observe all tags, ordered alphabetically
     */
    fun observeAllTags(): Flow<List<Tag>>

    /**
     * Search tags by name
     */
    fun searchTags(query: String): Flow<List<Tag>>

    /**
     * Get tag by name
     * Normalize name before calling Dao
     */
    suspend fun getTagByName(name: String): Tag?

    /**
     * Get tag by Id
     */
    suspend fun getTagById(tagId: TagId): Tag?

    //------------ Item-Tag Association ------------

    /**
     * Link tag to an item
     */
    suspend fun linkTagToItem(itemId: ItemId, tagId: TagId)

    /**
     * Unlink/remove tag from an item
     */
    suspend fun unlinkTagFromItem(itemId: ItemId, tagId: TagId)

    /**
     * Unlink/remove all tags from item
     */
    suspend fun unlinkAllTagsFromItem(itemId: ItemId)

    /**
     * Observe items by a tag
     */
    fun observeItemsByTag(tagId: TagId): Flow<List<Item>>

    /**
     * Observe items by a tag in a container
     */
    fun observeItemsByTagInContainer(
        tagId: TagId,
        containerId: ContainerId
    ): Flow<List<Item>>
}