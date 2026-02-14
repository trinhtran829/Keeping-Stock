package com.keepingstock.data.repositories

import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.Tag
import com.keepingstock.core.contracts.TagId
import com.keepingstock.data.daos.ItemTagDao
import com.keepingstock.data.daos.ItemWithTagsDao
import com.keepingstock.data.daos.TagDao
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
    override suspend fun createTag(name: String): Tag {
        // Placeholder: return dummy Tag
        return Tag(
            id = TagId(1L),
            name = name
        )
    }

    /**
     * Update tag
     */
    override suspend fun updateTag(tag: Tag) {
        // Placeholder: do nothing for now
    }

    /**
     * Delete tag
     */
    override suspend fun deleteTag(tag: Tag) {
        // Placeholder: do nothing for now
    }

    /**
     * Observe all tags
     */
    override fun observeAllTags(): Flow<List<Tag>> {
        // Placeholder: return empty list
        return flowOf(emptyList())
    }

    /**
     * Search tags
     */
    override fun searchTags(query: String): Flow<List<Tag>> {
        // Placeholder: return empty list
        return flowOf(emptyList())
    }

    /**
     * Get tag by name
     */
    override suspend fun getTagByName(name: String): Tag? {
        // Placeholder: return dummy TagEntity
        return Tag(
            id = TagId(2L),
            name = name
        )
    }

    /**
     * Get tag by Id
     */
    override suspend fun getTagById(tagId: TagId): Tag? {
        // Placeholder: return dummy TagEntity
        return Tag(
            id = tagId,
            name = "Placeholder Tag Name"
        )
    }

    //------------ Item-Tag Association ------------

    /**
     * Link tag to an item
     */
    override suspend fun linkTagToItem(itemId: ItemId, tagId: TagId) {
        // Placeholder: do nothing for now
    }

    /**
     * Unlink/remove tag from an item
     */
    override suspend fun unlinkTagFromItem(itemId: ItemId, tagId: TagId) {
        // Placeholder: do nothing for now
    }

    /**
     * Unlink/remove all tags from item
     */
    override suspend fun unlinkAllTagsFromItem(itemId: ItemId) {
        // Placeholder: do nothing for now
    }

    /**
     * Observe items by a tag
     */
    override fun observeItemsByTag(tagId: TagId): Flow<List<Item>> {
        // Placeholder: return empty list
        return flowOf(emptyList())
    }

    /**
     * Observe items by a tag in a container
     */
    override fun observeItemsByTagInContainer(
        tagId: TagId,
        containerId: ContainerId
    ): Flow<List<Item>> {
        // Placeholder: return empty list
        return flowOf(emptyList())
    }
}