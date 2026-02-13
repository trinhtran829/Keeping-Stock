package com.keepingstock.data.repositories

import com.keepingstock.data.entities.ItemWithTags
import com.keepingstock.data.entities.TagEntity
import kotlinx.coroutines.flow.Flow

/**
 * Placeholder repository
 *
 * NOTE TO TEAM:
 * - This is a temporary implementation
 * - These function names are final and stable
 * - UI and Integration lead can start using them
 * - The real business logic will be implemented later
 *
 * This is to ensures the UI/ViewModels code can be develop while I continue to
 * work on these logic.
 */

interface TagRepository {
    /**
     * Create tag
     */
    suspend fun createTag(name: String): Long

    /**
     * Update tag
     */
    suspend fun updateTag(tag: TagEntity)

    /**
     * Delete tag
     */
    suspend fun deleteTag(tag: TagEntity)

    /**
     * Observe all tags
     */
    fun observeAllTags(): Flow<List<TagEntity>>

    /**
     * Search tags
     */
    fun searchTags(query: String): Flow<List<TagEntity>>

    /**
     * Get tag by name
     */
    suspend fun getTagByName(name: String): TagEntity?

    /**
     * Get tag by Id
     */
    suspend fun getTagById(tagId: Long): TagEntity?

    //------------ Item-Tag Association ------------

    /**
     * Link tag to an item
     */
    suspend fun linkTagToItem(itemId: Long, tagId: Long)

    /**
     * Unlink/remove tag from an item
     */
    suspend fun unlinkTagFromItem(itemId: Long, tagId: Long)

    /**
     * Unlink/remove all tags from item
     */
    suspend fun unlinkAllTagsFromItem(itemId: Long)

    /**
     * Observe items by a tag
     */
    suspend fun observeItemsByTag(tagId: Long): Flow<List<ItemWithTags>>

    /**
     * Observe items by a tag in a container
     */
    suspend fun observeItemsByTagInContainer(
        tagId: Long,
        containerId: Long
    ): Flow<List<ItemWithTags>>
}