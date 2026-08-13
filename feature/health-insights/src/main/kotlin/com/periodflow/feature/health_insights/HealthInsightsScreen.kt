package com.periodflow.feature.health_insights

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periodflow.core.domain.model.HealthAnalysisReport
import com.periodflow.core.ui.components.claymorphism

@Composable
fun HealthInsightsRoute(
    viewModel: HealthInsightsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val exportState by viewModel.exportState.collectAsStateWithLifecycle()

    HealthInsightsScreen(
        uiState = uiState,
        exportState = exportState,
        onExportClick = viewModel::exportPdf,
        onResetExportState = viewModel::resetExportState
    )
}

@Composable
fun HealthInsightsScreen(
    uiState: HealthInsightsState,
    exportState: ExportState,
    onExportClick: () -> Unit,
    onResetExportState: () -> Unit
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

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when (uiState) {
            is HealthInsightsState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is HealthInsightsState.Error -> {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is HealthInsightsState.Success -> {
                HealthInsightsContent(
                    report = uiState.report,
                    exportState = exportState,
                    onExportClick = onExportClick
                )
            }
        }
    }
}

@Composable
private fun HealthInsightsContent(
    report: HealthAnalysisReport,
    exportState: ExportState,
    onExportClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .claymorphism(
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(28.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Risk Score",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${report.riskScore}/100",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${report.riskLevel.emoji} ${report.riskLevel.displayName}",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }

        item {
            Text(
                text = "Recommendations",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(report.recommendations) { recommendation ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "• $recommendation",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
            val btnColor = if (exportState !is ExportState.Exporting) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
            
            Button(
                onClick = onExportClick,
                enabled = exportState !is ExportState.Exporting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .claymorphism(
                        backgroundColor = btnColor,
                        shape = RoundedCornerShape(28.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = PaddingValues()
            ) {
                if (exportState is ExportState.Exporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Export Report as PDF",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
