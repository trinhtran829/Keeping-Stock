package com.keepingstock.data.repositories

import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.data.daos.ItemDao
import com.keepingstock.data.daos.ItemTagDao
import com.keepingstock.data.daos.ItemWithTagsDao
import com.keepingstock.data.entities.ItemStatus
import com.keepingstock.data.mapper.toDomain
import com.keepingstock.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import kotlin.collections.emptyList

/**
 * This code was generated with the help of the following links
 * https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room?authuser=1&continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-compose-unit-6-pathway-2%3Fauthuser%3D1%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-compose-persisting-data-room#7
 * These links document the sample code that led to my code.
 */

class ItemRepositoryImpl(
    private val itemDao: ItemDao,
    private val itemWithTagsDao: ItemWithTagsDao,
    private val itemTagDao: ItemTagDao
    ) : ItemRepository {

    /**
     * Create an item
     * Rule:
     * containerId == null -> status = TAKEN_OUT, checkoutDate = now
     * containerId != null -> status = STORED, checkoutDate = null
     */
    override suspend fun createItem(
        name: String,
        description: String?,
        imageUri: String?,
        containerId: ContainerId?
    ): Item {
        var status: ItemStatus = ItemStatus.TAKEN_OUT
        var checkoutDate: Date? = Date()

        if (containerId != null) {
            status = ItemStatus.STORED
            checkoutDate = null
        }

        val item = Item(
            id = ItemId(0L),
            name = name,
            description = description,
            imageUri = imageUri,
            containerId = containerId,
            status = status,
            createdDate = Date(),
            checkoutDate = checkoutDate,
            tags = emptyList()
        )
        val generatedId = itemDao.insert(item.toEntity())
        return item.copy(id = ItemId(generatedId))
    }

    /**
     * Full item update
     * Rule:
     * containerId change to null -> status = TAKEN_OUT, checkoutDate unchanged
     * containerId change to not-null -> status = unchanged, checkoutDate = unchanged
     */
    override suspend fun updateItem(item: Item){
        val updatedItem: Item
        if (item.containerId == null) {
            updatedItem = item.copy(
                status = ItemStatus.TAKEN_OUT
            )
        } else {
            updatedItem = item
        }
        itemDao.update(updatedItem.toEntity())
    }

    /**
     * Update only item's status
     * Rule:
     * STORED -> TAKEN_OUT, checkoutDate = now
     * TAKEN_OUT -> STORED, checkoutDate = null
     */
    override suspend fun updateItemStatus(itemId: ItemId, status: ItemStatus) {
        var checkoutDate: Date? = Date()
        if(status == ItemStatus.STORED) {
            checkoutDate = null
        }
        itemDao.updateItemStatus(
            itemId = itemId.value,
            status = status,
            checkoutDate = checkoutDate
        )
    }

    /**
     * Delete an item
     * Also removes all item-tag rows associated with the item
     */
    override suspend fun deleteItem(item: Item) {
        itemTagDao.deleteAllTagsForItem(item.id.value)
        itemDao.delete(item.toEntity())
    }

    /**
     * Get item by Id
     */
    override suspend fun getItemById(itemId: ItemId): Item? {
        return itemWithTagsDao.getItemWithTagsById(itemId.value)?.toDomain()
    }

    /**
     * Observe all items
     */
    override fun observeItem(): Flow<List<Item>> {
        return itemWithTagsDao.getAllItemsWithTags()
            .map { itemList -> itemList.map { it.toDomain() } }

    }

    /**
     * Observe items in a container
     */
    override fun observeItemInContainer(containerId: ContainerId): Flow<List<Item>> {
        return itemWithTagsDao.getItemsInContainerWithTags(containerId.value)
            .map { itemList -> itemList.map { it.toDomain() } }
    }

    /**
     * Observe items NOT in a container (unsorted)
     */
    override fun observeItemUnsorted(): Flow<List<Item>> {
        return itemWithTagsDao.getUnsortedItemsWithTags()
            .map { itemList -> itemList.map { it.toDomain() } }
    }

    /**
     * Search items where query match item's or tag's name
     */
    override fun searchItemsByNameOrTag(query: String): Flow<List<Item>> {
        return itemWithTagsDao.searchByItemOrTagName(query)
            .map { itemList -> itemList.map { it.toDomain() } }
    }

    /**
     * Search items where query match item's name
     */
    override fun searchItemsByName(query: String): Flow<List<Item>> {
        return itemWithTagsDao.searchItemsByName(query)
            .map { itemList -> itemList.map { it.toDomain() } }
    }

    /**
     * Search items where query match tag's name
     */
    override fun searchItemsByTagName(query: String): Flow<List<Item>> {
        return itemWithTagsDao.searchItemsByTagName(query)
            .map { itemList -> itemList.map { it.toDomain() }
        }
    }
}