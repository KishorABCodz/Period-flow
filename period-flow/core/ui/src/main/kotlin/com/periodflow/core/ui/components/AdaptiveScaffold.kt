package com.periodflow.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A simplified supporting-pane scaffold that displays the main pane followed
 * by the supporting pane in a single scrollable column.
 *
 * This replaces the Material3 Adaptive [SupportingPaneScaffold] which has a
 * binary-incompatibility with compose-bom 2024.05.00. On phone form-factors
 * the visual result is identical — both panes are stacked vertically.
 */
@Composable
fun AdaptiveSupportingPaneScaffold(
    mainPane: @Composable () -> Unit,
    supportingPane: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        mainPane()
        supportingPane()
    }
}

/**
 * A simplified list-detail pane scaffold that displays the list pane
 * followed by the detail pane in a single scrollable column.
 */
@Composable
fun AdaptiveListDetailPaneScaffold(
    listPane: @Composable () -> Unit,
    detailPane: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        listPane()
        detailPane()
    }
}
