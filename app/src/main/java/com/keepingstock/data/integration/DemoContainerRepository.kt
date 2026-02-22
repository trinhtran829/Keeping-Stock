package com.keepingstock.data.integration

import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.ContainerRepository

/**
 * Minimal in-memory implementation used for integration wiring and early UI flows.
 */
class DemoContainerRepository(
    seedContainers: List<Container> = DEFAULT_CONTAINERS
) : ContainerRepository {

    private val containers = seedContainers.associateBy { it.id.value }.toMutableMap()

    override suspend fun getContainer(id: ContainerId): Container? = containers[id.value]

    override suspend fun getRootContainers(): List<Container> =
        containers.values.filter { it.parentContainerId == null }.sortedBy { it.name }

    override suspend fun getChildContainers(parentId: ContainerId): List<Container> =
        containers.values.filter { it.parentContainerId == parentId }.sortedBy { it.name }

    override suspend fun upsertContainer(container: Container): Container {
        val id = if (container.id.value <= 0L) nextId() else container.id.value
        val saved = container.copy(id = ContainerId(id))
        containers[id] = saved
        return saved
    }

    override suspend fun deleteContainer(id: ContainerId) {
        containers.remove(id.value)
    }

    private fun nextId(): Long = (containers.keys.maxOrNull() ?: 0L) + 1L

    private companion object {
        val DEFAULT_CONTAINERS = listOf(
            Container(
                id = ContainerId(1L),
                name = "Garage",
                description = "Tools and storage"
            ),
            Container(
                id = ContainerId(2L),
                name = "Kitchen",
                description = "Appliances and pantry overflow"
            ),
            Container(
                id = ContainerId(3L),
                name = "Tool Chest",
                description = "Hand tools",
                parentContainerId = ContainerId(1L)
            )
        )
    }
}
