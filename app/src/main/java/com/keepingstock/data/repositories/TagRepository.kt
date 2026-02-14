package com.keepingstock.data.repositories

import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.Tag
import com.keepingstock.core.contracts.TagId
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
    suspend fun createTag(name: String): Tag

    /**
     * Update tag
     */
    suspend fun updateTag(tag: Tag)

    /**
     * Delete tag
     */
    suspend fun deleteTag(tag: Tag)

    /**
     * Observe all tags
     */
    fun observeAllTags(): Flow<List<Tag>>

    /**
     * Search tags
     */
    fun searchTags(query: String): Flow<List<Tag>>

    /**
     * Get tag by name
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