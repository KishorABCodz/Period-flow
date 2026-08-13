package com.periodflow.feature.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periodflow.core.ui.components.ClayButton
import com.periodflow.core.ui.components.ClayCard
import com.periodflow.core.ui.components.ClayChip
import com.periodflow.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Cycle defaults section
        Text(
            text = "Cycle Defaults",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        ClayCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                // Average cycle length
                ClayStepper(
                    label = "Average Cycle Length",
                    value = uiState.defaultCycleLength,
                    unit = "days",
                    onIncrement = { if (uiState.defaultCycleLength < 45) viewModel.onCycleLengthChanged(uiState.defaultCycleLength + 1) },
                    onDecrement = { if (uiState.defaultCycleLength > 20) viewModel.onCycleLengthChanged(uiState.defaultCycleLength - 1) },
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                // Average period length
                ClayStepper(
                    label = "Average Period Length",
                    value = uiState.defaultPeriodLength,
                    unit = "days",
                    onIncrement = { if (uiState.defaultPeriodLength < 10) viewModel.onPeriodLengthChanged(uiState.defaultPeriodLength + 1) },
                    onDecrement = { if (uiState.defaultPeriodLength > 2) viewModel.onPeriodLengthChanged(uiState.defaultPeriodLength - 1) },
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Appearance section
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        ClayCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Theme",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "A beautiful, modern Light Pink Claymorphism theme.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Security section
        Text(
            text = "Security",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        ClayCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "App Lock",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Require fingerprint authentication to open the app.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = uiState.isBiometricEnabled,
                        onCheckedChange = { viewModel.onBiometricToggle(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.tertiary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // About section
        Text(
            text = "About",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        ClayCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "PeriodFlow v1.0.0",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "A private, offline-first period tracker with a tactile clay design.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your data never leaves your device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ═══════════════════════════════════════════
        Text(
            text = "🛠️ Developer Tools",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
        )

        ClayCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Test Data Management",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = "Seed realistic cycle data (6 completed cycles + 1 ongoing) " +
                            "with flow, mood, symptoms, temperature, and ovulation logs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Status indicator
                if (uiState.hasData) {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = "🟢", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "Database has data",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    }
                } else {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = "⚪", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "Database is empty",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Feedback message
                AnimatedVisibility(
                    visible = uiState.devToolsMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    uiState.devToolsMessage?.let { message ->
                        ClayCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(
                                    onClick = { viewModel.dismissDevToolsMessage() },
                                    modifier = Modifier.size(24.dp),
                                ) {
                                    Icon(
                                        Icons.Rounded.Close,
                                        contentDescription = "Dismiss",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Seed button
                    ClayButton(
                        onClick = { viewModel.seedSampleData() },
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                    ) {
                        if (uiState.isSeedingData) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Rounded.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = "Seed Data",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }
                    }

                    // Clear button
                    ClayButton(
                        onClick = { viewModel.clearAllData() },
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                    ) {
                        if (uiState.isClearingData) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = "Clear All",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(180.dp))
    }
}

@Composable
private fun ClayStepper(
    label: String,
    value: Int,
    unit: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "$value $unit", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClayButton(
                onClick = onDecrement,
                backgroundColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(48.dp)
            ) {
                Text("-", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            }
            
            ClayButton(
                onClick = onIncrement,
                backgroundColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(48.dp)
            ) {
                Text("+", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
