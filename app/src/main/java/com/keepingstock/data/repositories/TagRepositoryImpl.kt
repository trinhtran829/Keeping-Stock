package com.keepingstock.data.repositories

import com.keepingstock.data.daos.ItemTagDao
import com.keepingstock.data.daos.ItemWithTagsDao
import com.keepingstock.data.daos.TagDao
import com.keepingstock.data.entities.ItemWithTags
import com.keepingstock.data.entities.TagEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

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

class TagRepositoryImpl(
    private val tagDao: TagDao,
    private val itemTagDao: ItemTagDao,
    private val itemWithTagsDao: ItemWithTagsDao
) : TagRepository  {
    /**
     * Create tag
     */
    override suspend fun createTag(name: String): Long {
        // Placeholder: return dummy id
        return 0L
    }

    /**
     * Update tag
     */
    override suspend fun updateTag(tag: TagEntity) {
        // Placeholder: do nothing for now
    }

    /**
     * Delete tag
     */
    override suspend fun deleteTag(tag: TagEntity) {
        // Placeholder: do nothing for now
    }

    /**
     * Observe all tags
     */
    override fun observeAllTags(): Flow<List<TagEntity>> {
        // Placeholder: return empty list
        return flowOf(emptyList())
    }

    /**
     * Search tags
     */
    override fun searchTags(query: String): Flow<List<TagEntity>> {
        // Placeholder: return empty list
        return flowOf(emptyList())
    }

    /**
     * Get tag by name
     */
    override suspend fun getTagByName(name: String): TagEntity? {
        // Placeholder: return dummy TagEntity
        return TagEntity(
            tagId = 1L,
            name = "Placeholder Tag"
        )
    }

    /**
     * Get tag by Id
     */
    override suspend fun getTagById(tagId: Long): TagEntity? {
        // Placeholder: return dummy TagEntity
        return TagEntity(
            tagId = tagId,
            name = "Placeholder Tag"
        )
    }

    //------------ Item-Tag Association ------------

    /**
     * Link tag to an item
     */
    override suspend fun linkTagToItem(itemId: Long, tagId: Long) {
        // Placeholder: do nothing for now
    }

    /**
     * Unlink/remove tag from an item
     */
    override suspend fun unlinkTagFromItem(itemId: Long, tagId: Long) {
        // Placeholder: do nothing for now
    }

    /**
     * Unlink/remove all tags from item
     */
    override suspend fun unlinkAllTagsFromItem(itemId: Long) {
        // Placeholder: do nothing for now
    }

    /**
     * Observe items by a tag
     */
    override suspend fun observeItemsByTag(tagId: Long): Flow<List<ItemWithTags>> {
        // Placeholder: return empty list
        return flowOf(emptyList())
    }

    /**
     * Observe items by a tag in a container
     */
    override suspend fun observeItemsByTagInContainer(
        tagId: Long,
        containerId: Long
    ): Flow<List<ItemWithTags>> {
        // Placeholder: return empty list
        return flowOf(emptyList())
    }
}