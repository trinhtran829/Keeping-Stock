package com.keepingstock.data.repositories

import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.Tag
import com.keepingstock.core.contracts.TagId
import com.keepingstock.data.daos.ItemTagDao
import com.keepingstock.data.daos.ItemWithTagsDao
import com.keepingstock.data.daos.TagDao
import com.keepingstock.data.entities.ItemTagEntity
import com.keepingstock.data.entities.TagEntity
import com.keepingstock.data.mapper.toDomain
import com.keepingstock.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * This code was generated with the help of the following links
 * https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room?authuser=1&continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-compose-unit-6-pathway-2%3Fauthuser%3D1%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-compose-persisting-data-room#7
 * These links document the sample code that led to my code.
 */

class TagRepositoryImpl(
    private val tagDao: TagDao,
    private val itemTagDao: ItemTagDao,
    private val itemWithTagsDao: ItemWithTagsDao
) : TagRepository  {

    /**
     * Validate tag name
     * Rule: only contains A - Z, a - z, 0 - 9, -, &, and space
     */
    override fun validateTagName(name: String): Boolean {
        for (char in name) {
            val isLetter = char in 'A'..'Z' || char in 'a'..'z'
            val isDigit = char in '0'..'9'
            val isHyphen = char == '-'
            val isAmpersand = char == '&'
            val isSpace = char == ' '

            val isValid = isLetter || isDigit || isHyphen || isAmpersand || isSpace

            if (!isValid) {
                return false
            }
        }
        return true
    }

    /**
     * Normalize tag name
     * Rule:
     *Trim leading and trailing white spaces
     *Collapse multiple white spaces into one
     *Convert to lower case
     *case insensitive is handled by Room unique index
     */
    override fun normalizeTagName(name: String): String {
        val trimmed = name.trim().lowercase()
        val result = StringBuilder()
        var lastCharWasSpace = false

        for (char in trimmed) {
            if (char == ' ') {
                if (!lastCharWasSpace) {
                    result.append(char)
                }
                lastCharWasSpace = true
            } else {
                result.append(char)
                lastCharWasSpace = false
            }
        }
        return result.toString()
    }

    /**
     * Create tag
     * Rule:
     * Trim leading and trailing white spaces
     * Collapse multiple white spaces into one
     * Convert to lower case
     * case insensitive prevent duplication - reuse existing tag if found
     * Throws [IllegalStateException] if tag name is invalid
     */
    override suspend fun createTag(name: String): Tag {
        if (!validateTagName(name)) {
            throw IllegalStateException("Invalid tag name")
        }
        val normalizedName = normalizeTagName(name)
        val existingName = tagDao.getTagByName(normalizedName)
        if (existingName != null) {
            return existingName.toDomain()
        }
        val tagEntity = TagEntity(name = normalizedName)
        val generatedId = tagDao.insert(tagEntity)
        return tagEntity.copy(tagId = generatedId).toDomain()
    }

    /**
     * Update tag
     * Rule:
     * Cannot rename to an existing name
     * Throws [IllegalStateException] if target name is invalid
     * Throws [IllegalStateException] if target name is taken
     */
    override suspend fun updateTag(tag: Tag) {
        if(!validateTagName(tag.name)) {
            throw IllegalStateException("Invalid tag name")
        }

        val normalizedName = normalizeTagName(tag.name)
        val existingName = tagDao.getTagByName(normalizedName)
        // cannot rename to an existing name owned by a different tagId
        if (existingName != null && existingName.tagId != tag.id.value) {
            throw IllegalStateException(
                "A tag with the name \"$normalizedName\" already exists.")
        }
        val tagToUpdate = TagEntity(
            tagId = tag.id.value,
            name = normalizedName
        )
        tagDao.update(tagToUpdate)
    }

    /**
     * Delete tag
     * Rule:
     * Cannot delete tag still associate with an item
     * Throws [IllegalStateException] if tag still associate with an item
     */
    override suspend fun deleteTag(tag: Tag) {
        val itemCount = itemTagDao.countItemsWithTag(tag.id.value)
        if(itemCount > 0) {
            throw IllegalStateException("unable to delete a tag still associated with an item.")
        }
        tagDao.delete(tag.toEntity())
    }

    /**
     * Observe all tags, ordered alphabetically
     */
    override fun observeAllTags(): Flow<List<Tag>> {
        return tagDao.getTags()
            .map { tagList -> tagList.map { it.toDomain() } }
    }

    /**
     * Search tags by name
     */
    override fun searchTags(query: String): Flow<List<Tag>> {
        return tagDao.searchTags(query)
            .map { tagList -> tagList.map { it.toDomain() } }
    }

    /**
     * Get tag by name
     * Normalize name before calling Dao
     */
    override suspend fun getTagByName(name: String): Tag? {
        return tagDao.getTagByName(normalizeTagName(name))?.toDomain()
    }

    /**
     * Get tag by Id
     */
    override suspend fun getTagById(tagId: TagId): Tag? {
        return tagDao.getTagById(tagId.value)?.toDomain()
    }

    /**
     * Observe tag by Id
     */
    override fun observeTagById(tagId: TagId): Flow<Tag?> {
        return tagDao.observeTagById(tagId.value)
            .map { it?.toDomain() }
    }

    //------------ Item-Tag Association ------------

    /**
     * Link tag to an item
     */
    override suspend fun linkTagToItem(itemId: ItemId, tagId: TagId) {
        itemTagDao.insert(ItemTagEntity(itemId = itemId.value, tagId = tagId.value))
    }

    /**
     * Unlink/remove tag from an item
     */
    override suspend fun unlinkTagFromItem(itemId: ItemId, tagId: TagId) {
        itemTagDao.delete(itemId = itemId.value, tagId = tagId.value)
    }

    /**
     * Unlink/remove all tags from item
     */
    override suspend fun unlinkAllTagsFromItem(itemId: ItemId) {
        itemTagDao.deleteAllTagsForItem(itemId = itemId.value)
    }

    /**
     * Observe items by a tag
     */
    override fun observeItemsByTag(tagId: TagId): Flow<List<Item>> {
        return itemWithTagsDao.searchItemsByTag(tagId.value)
            .map { itemList -> itemList.map { it.toDomain() } }
    }

    /**
     * Observe items by a tag in a container
     */
    override fun observeItemsByTagInContainer(
        tagId: TagId,
        containerId: ContainerId
    ): Flow<List<Item>> {
        return itemWithTagsDao.getItemsByTagInContainer(
            containerId = containerId.value,
            tagId = tagId.value
        ).map { itemList -> itemList.map { it.toDomain() } }
    }
}