package com.keepingstock.ui.models

/**
 * UI-friendly summary of a container.
 *
 * This model contains only the information required to render a container
 * in list, grid, or card layouts.
 *
 * It is intentionally decoupled from Room entities.
 * ViewModels are responsible for mapping ContainerEntity objects into this type.
 */
data class ContainerSummaryUi(
    val id: Long,
    val name: String,
    val description: String? = null,
    val imageUri: String? = null
)