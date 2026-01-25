package com.example.keepingstock.core.contracts

interface ItemRepository {
    @Throws(Exception::class)
    suspend fun getItem(id: ItemId): Item?

    suspend fun searchItems(query: String, tags: List<Tag> = emptyList()): List<Item>

    suspend fun getItemsInContainer(containerId: ContainerId): List<Item>

    @Throws(Exception::class)
    suspend fun upsertItem(item: Item): Item

    @Throws(Exception::class)
    suspend fun deleteItem(id: ItemId)
}

interface ContainerRepository {
    @Throws(Exception::class)
    suspend fun getContainer(id: ContainerId): Container?

    suspend fun getRootContainers(): List<Container>

    suspend fun getChildContainers(parentId: ContainerId): List<Container>

    @Throws(Exception::class)
    suspend fun upsertContainer(container: Container): Container

    @Throws(Exception::class)
    suspend fun deleteContainer(id: ContainerId)
}