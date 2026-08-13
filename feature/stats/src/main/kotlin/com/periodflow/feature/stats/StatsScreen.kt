package com.periodflow.feature.stats

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periodflow.core.ui.components.ClayButton
import com.periodflow.core.ui.components.ClayCard
import com.periodflow.core.ui.theme.*

import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Share
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateToHealthInsights: () -> Unit = {},
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.reportUri) {
        if (uiState.reportUri != null) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uiState.reportUri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback or handle error
            }
            viewModel.dismissReportMessage()
        }
    }

    Scaffold(
        floatingActionButton = {
            ClayButton(
                onClick = { viewModel.generateReport() },
                backgroundColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(16.dp).height(56.dp).width(200.dp)
            ) {
                if (uiState.isGeneratingReport) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.PictureAsPdf, contentDescription = "Download Report", tint = MaterialTheme.colorScheme.onPrimary)
                        Text("Download Report", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 20.dp,
                bottom = paddingValues.calculateBottomPadding() + 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Header
            item {
                Text(
                    text = "Statistics",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

        // Summary cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SummaryCard(
                    label = "Avg. Cycle",
                    value = "${uiState.averageCycleLength} days",
                    modifier = Modifier.weight(1f),
                )
                SummaryCard(
                    label = "Avg. Period",
                    value = "${uiState.averagePeriodLength} days",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Cycle length trend Canvas Chart
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth().height(260.dp),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = "Cycle Length Trend",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (uiState.cycleLengths.size >= 2) {
                        CustomBarChart(
                            data = uiState.cycleLengths,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Log at least 2 complete cycles to see trends",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        // Health Insights Button
        item {
            ClayButton(
                onClick = onNavigateToHealthInsights,
                backgroundColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.fillMaxWidth().height(64.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Insights, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(end = 8.dp))
                    Text(
                        text = "Health Pattern Insights",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        
        // Cycle history header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Rounded.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Cycle History",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        if (uiState.cycles.isEmpty()) {
            item {
                Text(
                    text = "No cycles logged yet. Start tracking to see your history.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(uiState.cycles, key = { it.id }) { cycle ->
                CycleHistoryItem(cycle = cycle)
            }
        }

        // Bottom spacer for nav bar
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    ClayCard(
        modifier = modifier.height(120.dp),
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CustomBarChart(
    data: List<Int>,
    modifier: Modifier = Modifier
) {
    val maxData = (data.maxOrNull() ?: 35).coerceAtLeast(1)
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animatedProgress.animateTo(1f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
    }

    val colorOnSurface = MaterialTheme.colorScheme.onSurface
    val colorPrimary = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val barWidth = 40f
        val spacing = (size.width - (data.size * barWidth)) / (data.size + 1)
        
        data.forEachIndexed { index, value ->
            val x = spacing + (index * (barWidth + spacing))
            val targetHeight = (value.toFloat() / maxData.toFloat()) * size.height
            val height = targetHeight * animatedProgress.value
            val y = size.height - height

            // Draw track (background)
            drawRoundRect(
                color = colorOnSurface.copy(alpha = 0.1f),
                topLeft = Offset(x, 0f),
                size = Size(barWidth, size.height),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )

            // Draw active bar
            drawRoundRect(
                color = colorPrimary,
                topLeft = Offset(x, y),
                size = Size(barWidth, height),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}

@Composable
private fun CycleHistoryItem(
    cycle: CycleUiModel,
    modifier: Modifier = Modifier,
) {
    ClayCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = cycle.dateRange,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (cycle.isOngoing) "Ongoing" else "Cycle length: ${cycle.cycleLength} days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (cycle.periodLength != null) {
                Text(
                    text = "${cycle.periodLength}d period",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}
