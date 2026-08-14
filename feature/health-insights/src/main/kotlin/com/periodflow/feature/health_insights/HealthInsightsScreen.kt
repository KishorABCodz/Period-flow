package com.periodflow.feature.health_insights

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periodflow.core.ai.model.AiResult
import com.periodflow.core.domain.model.HealthAnalysisReport
import com.periodflow.core.domain.model.RiskLevel
import com.periodflow.core.ui.components.ClayButton
import com.periodflow.core.ui.components.ClayCard
import com.periodflow.core.ui.components.claymorphism

@Composable
fun HealthInsightsRoute(
    viewModel: HealthInsightsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val exportState by viewModel.exportState.collectAsStateWithLifecycle()
    val aiNarrative by viewModel.aiNarrative.collectAsStateWithLifecycle()
    val streamedText by viewModel.streamedText.collectAsStateWithLifecycle()
    val isFromCache by viewModel.isFromCache.collectAsStateWithLifecycle()

    HealthInsightsScreen(
        uiState = uiState,
        exportState = exportState,
        aiNarrative = aiNarrative,
        streamedText = streamedText,
        isFromCache = isFromCache,
        onExportClick = viewModel::exportPdf,
        onResetExportState = viewModel::resetExportState,
        onRetryAi = viewModel::retryAiNarrative,
    )
}

@Composable
fun HealthInsightsScreen(
    uiState: HealthInsightsState,
    exportState: ExportState,
    aiNarrative: AiResult<String>,
    streamedText: String,
    isFromCache: Boolean,
    onExportClick: () -> Unit,
    onResetExportState: () -> Unit,
    onRetryAi: () -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(exportState) {
        when (exportState) {
            is ExportState.Success -> {
                Toast.makeText(context, "Exported successfully!", Toast.LENGTH_SHORT).show()
                onResetExportState()
            }
            is ExportState.Error -> {
                Toast.makeText(context, "Export failed: ${exportState.message}", Toast.LENGTH_SHORT).show()
                onResetExportState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        when (uiState) {
            is HealthInsightsState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            is HealthInsightsState.Error -> {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is HealthInsightsState.Success -> {
                HealthInsightsContent(
                    report = uiState.report,
                    exportState = exportState,
                    aiNarrative = aiNarrative,
                    streamedText = streamedText,
                    isFromCache = isFromCache,
                    onExportClick = onExportClick,
                    onRetryAi = onRetryAi,
                )
            }
        }
    }
}

@Composable
private fun HealthInsightsContent(
    report: HealthAnalysisReport,
    exportState: ExportState,
    aiNarrative: AiResult<String>,
    streamedText: String,
    isFromCache: Boolean,
    onExportClick: () -> Unit,
    onRetryAi: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Screen title
        item {
            Text(
                text = "Health Insights",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // Risk Score hero card
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                backgroundColor = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "RISK SCORE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${report.riskScore}",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "out of 100",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RiskBadge(level = report.riskLevel)
                }
            }
        }

        // AI-generated personalised insight
        item {
            AiInsightCard(
                state = aiNarrative,
                streamedText = streamedText,
                isFromCache = isFromCache,
                onRetry = onRetryAi,
            )
        }

        item {
            Text(
                text = "Recommendations",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        items(report.recommendations, key = { it.hashCode() }) { recommendation ->
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "•",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = recommendation,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            ClayButton(
                onClick = onExportClick,
                backgroundColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                if (exportState is ExportState.Exporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PictureAsPdf,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                        Text(
                            text = "Export Report as PDF",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = report.disclaimer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AiInsightCard(
    state: AiResult<String>,
    streamedText: String,
    isFromCache: Boolean,
    onRetry: () -> Unit,
) {
    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        backgroundColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                )
                Text(
                    text = "AI Personal Insight",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                if (isFromCache) {
                    Text(
                        text = "cached · refreshing",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // If we have any streamed text, show it (with a blinking caret while loading).
            when {
                streamedText.isNotBlank() -> {
                    val caret = if (state is AiResult.Loading) " ▍" else ""
                    Text(
                        text = streamedText + caret,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                state is AiResult.Loading || state is AiResult.Idle -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.tertiary,
                            strokeWidth = 2.dp,
                        )
                        Text(
                            text = "Streaming your personalised insight…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                state is AiResult.Error -> {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ClayButton(
                        onClick = onRetry,
                        backgroundColor = MaterialTheme.colorScheme.tertiary,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.height(44.dp).widthIn(min = 140.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "Try again",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }
                else -> Unit
            }
        }
    }
}

/**
 * Emoji-free, accessible risk badge with a colored dot + label.
 */
@Composable
private fun RiskBadge(level: RiskLevel) {
    val dotColor: Color = when (level) {
        RiskLevel.LOW -> MaterialTheme.colorScheme.tertiary
        RiskLevel.MODERATE -> MaterialTheme.colorScheme.secondary
        RiskLevel.ELEVATED -> MaterialTheme.colorScheme.secondary
        RiskLevel.HIGH -> MaterialTheme.colorScheme.error
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .claymorphism(
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(50),
                elevation = 4.dp,
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color = dotColor, radius = size.minDimension / 2f, center = Offset(size.width / 2f, size.height / 2f))
            // subtle inner highlight
            drawCircle(color = Color.White.copy(alpha = 0.35f), radius = size.minDimension / 4f, center = Offset(size.width / 3f, size.height / 3f))
        }
        Text(
            text = level.displayName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
