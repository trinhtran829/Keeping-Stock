package com.keepingstock.ui.screens.utility

import android.R.attr.name
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Routes
import com.keepingstock.core.contracts.intents.utility.SelectContainerIntent
import com.keepingstock.core.contracts.uistates.utility.SelectContainerUiState
import com.keepingstock.ui.components.screen.DetailRow
import com.keepingstock.ui.components.screen.ErrorContent
import com.keepingstock.ui.components.screen.LoadingContent
import com.keepingstock.ui.components.thumbnail.ContainerThumbnail
import java.util.Date

/**
 * Add/Edit Container screen that renders based on uiState.
 *
 * State handling:
 * - [SelectContainerUiState.Loading] shows a loading indicator.
 * - [SelectContainerUiState.Error] shows an error message.
 * - [SelectContainerUiState.Ready] shows the editable form and emits [SelectContainerIntent]
 *   events via [onIntent].
 *
 * Navigation:
 * - [onNavigateBack] is called when the user confirms leaving (e.g. discard changes) or taps
 *   Cancel when the form is not dirty.
 *
 * TODO: Extract back handling to VM/effects
 *
 * @param modifier: Modifier applied to the screen container.
 * @param uiState: Current UI state for the Select Container flow.
 * @param onIntent: Callback for user intents (field edits, save, image changes, etc.).
 * @param onNavigateBack: Callback to navigate up/back out of this screen.
 */
@Composable
fun SelectContainerScreen(
    modifier: Modifier = Modifier,
    uiState: SelectContainerUiState,
    onIntent: (SelectContainerIntent) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    when (uiState) {
        SelectContainerUiState.Loading ->
            LoadingContent(modifier = modifier.fillMaxSize())

        is SelectContainerUiState.Error ->
            ErrorContent(
                modifier = modifier.fillMaxSize(),
                message = "Unable to load select new container screen."
            )

        is SelectContainerUiState.Ready ->
            ReadyContents(
                modifier = modifier.fillMaxSize(),
                uiState = uiState,
                onIntent = onIntent,
                onNavigateBack = onNavigateBack
            )
    }
}

/**
 * Renders the editable Add/Edit Container form for the [SelectContainerUiState.Ready] state.
 *
 * @param modifier: Modifier applied to the scrolling content container.
 * @param uiState: Ready state containing current field values, validation, and flags.
 * @param onIntent: Callback for emitting user intents to the state owner
 * @param onNavigateBack: Callback invoked when navigation away from the screen is confirmed.
 */
@Composable
fun ReadyContents(
    modifier: Modifier,
    uiState: SelectContainerUiState.Ready,
    onIntent: (SelectContainerIntent) -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        // TODO: Add Header? Is Title Bar sufficient?

        // Context Card
        ContextCard(uiState, onIntent)

        // Controls Card
        // TODO: Add search feature?

        // Breadcrumb
        BreadcrumbRow(uiState, onIntent)

        // Current subcontainers
        CurrentSubcontainers(
            modifier = Modifier.weight(1f),
            uiState = uiState,
            onIntent = onIntent
        )

        // Action Buttons
        ActionSection(uiState, onIntent)
    }
}

/**
 * Renders the current meta information/context of the current move.
 *
 * @param uiState: Ready state containing current field values, validation, and flags.
 * @param onIntent: Callback for emitting user intents to the state owner
 */
@Composable
fun ContextCard(
    uiState: SelectContainerUiState.Ready,
    onIntent: (SelectContainerIntent) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailRow(
                label = if (uiState.subjectType == Routes.SubjectType.Container)
                    "Moving Container:"
                else
                    "Moving Item"
                ,
                value = uiState.subjectName
            )

            DetailRow(
                label = "Selected Container to Move to:",
                value = uiState.selectedContainer?.name ?: "Root"
            )
        }
    }
}

/**
 * Renders the breadcrumb path row(s)
 *
 * TODO: AssistChip vs Text component?
 *
 * @param uiState: Ready state containing current field values, validation, and flags.
 * @param onIntent: Callback for emitting user intents to the state owner
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BreadcrumbRow(
    uiState: SelectContainerUiState.Ready,
    onIntent: (SelectContainerIntent) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Browsing container: ${uiState.browsingParentContainer?.name ?: "Root"}")

            HorizontalDivider()

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                uiState.breadcrumbs.forEachIndexed { index, crumb ->
                    /*
                    AssistChip(
                        onClick = { onIntent(SelectContainerIntent.ClickBreadcrumb(crumb.id)) },
                        label = { Text(crumb.label) }
                    )


                    if (index != uiState.breadcrumbs.lastIndex) {
                        Text(">", modifier = Modifier.padding(top = 16.dp))
                    }
                    */

                    Text(
                        text = crumb.label,
                        modifier = Modifier.clickable(
                            onClick = { onIntent(SelectContainerIntent.ClickBreadcrumb(crumb.id)) }
                        )
                    )

                    if (index != uiState.breadcrumbs.lastIndex) {
                        Text(">")
                    }
                }
            }
        }
    }
}

/**
 * Renders the current containers list of subcontainers
 *
 * @param uiState: Ready state containing current field values, validation, and flags.
 * @param onIntent: Callback for emitting user intents to the state owner (demo controller / ViewModel).
 */
@Composable
fun CurrentSubcontainers(
    modifier: Modifier,
    uiState: SelectContainerUiState.Ready,
    onIntent: (SelectContainerIntent) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Text(
            style = MaterialTheme.typography.bodyLarge,
            text = "Current Container Contents:"
        )

        HorizontalDivider()

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.rows) { row ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ContainerThumbnail(
                            imagePath = row.container.imageUri,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            text = row.container.name,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = ">",
                            Modifier.padding(end = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * The collection of buttons for the user to signal intent.
 *
 * @param uiState: Ready state containing current field values, validation, and flags.
 * @param onIntent: Callback for emitting user intents to the state owner (demo controller / ViewModel).
 */
@Composable
fun ActionSection(
    uiState: SelectContainerUiState.Ready,
    onIntent: (SelectContainerIntent) -> Unit
) {
    Button(
        onClick = {
            onIntent(SelectContainerIntent.ChangeSelection(uiState.currentContainer?.id))
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Select Current Container")
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = {},
            modifier = Modifier.weight(1f)
        ) {
            Text("Cancel")
        }
        Button(
            onClick = {},
            modifier = Modifier.weight(1f)
        ) {
            Text("Confirm")
        }
    }
}

/**
 * Previews the current overall select container screen format
 */
@Preview(showBackground = true)
@Composable
fun Preview_SelectContainerScreen_Ready() {
    val currentContainer = Container(
        id = ContainerId(1L),
        name = "Garage",
        imageUri = "demo",
        createdDate = Date()
    )

    val nullContainer = null

    val rows = listOf(
        SelectContainerUiState.Ready.ContainerSelectRow(
            currentContainer,
            isSelected = false,
            isCurrent = false,
            isDisabled = false
        ),
        SelectContainerUiState.Ready.ContainerSelectRow(
            Container(
                id = ContainerId(2L),
                name = "Example",
                parentContainerId = ContainerId(1L),
                createdDate = Date()
            ),
            isSelected = false,
            isCurrent = false,
            isDisabled = false
        ),
        SelectContainerUiState.Ready.ContainerSelectRow(
            Container(
                id = ContainerId(3L),
                name = "Example2",
                parentContainerId = ContainerId(2L),
                createdDate = Date()
            ),
            isSelected = false,
            isCurrent = false,
            isDisabled = false
        ),
        SelectContainerUiState.Ready.ContainerSelectRow(
            Container(
                id = ContainerId(4L),
                name = "Example3",
                parentContainerId = ContainerId(3L),
                createdDate = Date()
            ),
            isSelected = false,
            isCurrent = false,
            isDisabled = false
        ),
        SelectContainerUiState.Ready.ContainerSelectRow(
            Container(
                id = ContainerId(5L),
                name = "Example4",
                parentContainerId = ContainerId(4L),
                createdDate = Date()
            ),
        isSelected = false,
        isCurrent = false,
            isDisabled = false
        ),
        SelectContainerUiState.Ready.ContainerSelectRow(Container(
                id = ContainerId(6L),
                name = "Example5",
                parentContainerId = ContainerId(5L),
                createdDate = Date()
            ),
        isSelected = false,
        isCurrent = false,
            isDisabled = false
        ),
        SelectContainerUiState.Ready.ContainerSelectRow(
            Container(
                id = ContainerId(7L),
                name = "Example6",
                parentContainerId = ContainerId(6L),
                createdDate = Date()
            ),
        isSelected = false,
        isCurrent = false,
            isDisabled = false
        ),
        SelectContainerUiState.Ready.ContainerSelectRow(
            Container(
                id = ContainerId(8L),
                name = "Example7",
                parentContainerId = ContainerId(7L),
                createdDate = Date()
            ),
        isSelected = false,
        isCurrent = false,
            isDisabled = false
        ),
        SelectContainerUiState.Ready.ContainerSelectRow(Container(
                id = ContainerId(9L),
                name = "Example8",
                parentContainerId = ContainerId(8L),
                createdDate = Date()
            ),
        isSelected = false,
        isCurrent = false,
            isDisabled = false
        ),
    )

    SelectContainerScreen(
        uiState = SelectContainerUiState.Ready(
            currentContainer = null,
            selectedContainer = currentContainer,
            breadcrumbs = listOf(
                SelectContainerUiState.Ready.Breadcrumb(null, "Root"),
                SelectContainerUiState.Ready.Breadcrumb(ContainerId(2L), "Example"),
                SelectContainerUiState.Ready.Breadcrumb(ContainerId(3L), "Example2"),
                SelectContainerUiState.Ready.Breadcrumb(ContainerId(4L), "Example3"),
                SelectContainerUiState.Ready.Breadcrumb(ContainerId(5L), "Example4"),
                SelectContainerUiState.Ready.Breadcrumb(ContainerId(6L), "Example5"),
                SelectContainerUiState.Ready.Breadcrumb(ContainerId(7L), "Example6"),
                SelectContainerUiState.Ready.Breadcrumb(ContainerId(8L), "Example7"),
                SelectContainerUiState.Ready.Breadcrumb(ContainerId(9L), "Example8"),
            ),
            rows = rows,
            subjectType = Routes.SubjectType.Container,
            subjectId = 1L,
            subjectName = "Garage",
            browsingParentContainer = Container(
                id = ContainerId(1L),
                name = "Root",
                createdDate = Date()
            )
        )
    )
}