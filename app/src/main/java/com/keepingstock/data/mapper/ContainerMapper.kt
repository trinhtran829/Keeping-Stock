package com.keepingstock.data.mapper

import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.data.entities.ContainerEntity

/**
 * This code was generated with help the following links
 * https://chatgpt.com/share/699e7311-df34-800a-a660-f687c32187a8
 * These links document the sample code that led to my code.
 */

/**
 * Extension functions to convert Database Entities to Domain Models
 */

fun ContainerEntity.toDomain(): Container {
    return Container(
        id = ContainerId(containerId),
        name = name,
        description = description,
        imageUri = imageUri,
        parentContainerId = parentContainerId?.let {ContainerId(it)},
        createdDate = createdDate
    )
}

fun Container.toEntity(): ContainerEntity {
    return ContainerEntity(
        containerId = id.value,
        name = name,
        description = description,
        imageUri = imageUri,
        parentContainerId = parentContainerId?.value,
        createdDate = createdDate
    )
}