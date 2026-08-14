package com.periodflow.feature.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.periodflow.core.ui.components.ClayButton
import com.periodflow.core.ui.components.ClayCard
import com.periodflow.core.ui.components.CycleRing
import com.periodflow.core.ui.components.PredictionCard
import com.periodflow.core.ui.components.AdaptiveSupportingPaneScaffold
import com.periodflow.core.ui.theme.*
import com.periodflow.feature.home.chat.CycleChatSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLog: (dateEpochDay: Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showChat by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(16.dp),
            ) {
                // Ask Bloom (Gemini) — small secondary FAB
                ClayButton(
                    onClick = { showChat = true },
                    backgroundColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(52.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = "Ask Bloom (Cycle Chat)",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }

                ClayButton(
                    onClick = { onNavigateToLog(uiState.todayEpochDay) },
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.height(56.dp).width(160.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Log today", tint = MaterialTheme.colorScheme.onPrimary)
                        Text("Log Today", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
    ) { padding ->
        AdaptiveSupportingPaneScaffold(
            modifier = Modifier.padding(padding),
            mainPane = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "PeriodFlow",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (uiState.companionMessage.isNotEmpty()) {
                        ClayCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FavoriteBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Your Companion",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                    Text(
                                        text = uiState.companionMessage,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Cycle ring with animated Day 12 / status pill built-in
                    CycleRing(
                        currentDay = uiState.currentCycleDay,
                        totalDays = uiState.averageCycleLength,
                        periodLength = uiState.averagePeriodLength,
                        phaseIcon = uiState.phaseIcon,
                        phaseName = uiState.phaseName,
                    )
                    
                    Spacer(modifier = Modifier.height(48.dp))
                }
            },
            supportingPane = {
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    visible = true
                }

                // Diet Tip floating animation
                val infiniteTransition = rememberInfiniteTransition(label = "diet_tip_float")
                val floatOffsetY by infiniteTransition.animateFloat(
                    initialValue = -4f,
                    targetValue = 4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "diet_tip_offset"
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    AnimatedVisibility(
                        visible = visible,
                        enter = slideInVertically(
                            initialOffsetY = { 50 },
                            animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f)
                        ) + fadeIn(animationSpec = tween(300))
                    ) {
                        if (uiState.hasEnoughData) {
                            PredictionCard(
                                nextPeriodDate = uiState.nextPeriodDateFormatted,
                                daysUntil = uiState.daysUntilNextPeriod,
                                fertileWindowDate = uiState.fertileWindowFormatted,
                                confidence = uiState.confidenceLevel,
                            )
                        } else {
                            ClayCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        text = "Track to Predict",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = "Log your period days to start getting cycle predictions. Tap \"Log Today\" to begin!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AnimatedVisibility(
                        visible = visible,
                        enter = slideInVertically(
                            initialOffsetY = { 50 },
                            animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f)
                        ) + fadeIn(animationSpec = tween(400))
                    ) {
                        if (uiState.dailyDietTip.isNotEmpty()) {
                            ClayCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Restaurant,
                                        contentDescription = null,
                                        modifier = Modifier.graphicsLayer { translationY = floatOffsetY }.size(32.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "Daily Tip",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = uiState.dailyDietTip,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    AnimatedVisibility(
                        visible = visible,
                        enter = slideInVertically(
                            initialOffsetY = { 50 },
                            animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f)
                        ) + fadeIn(animationSpec = tween(500))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            StatCard(
                                label = "Avg. Cycle",
                                value = "${uiState.averageCycleLength}d",
                                modifier = Modifier.weight(1f),
                            )
                            StatCard(
                                label = "Avg. Period",
                                value = "${uiState.averagePeriodLength}d",
                                modifier = Modifier.weight(1f),
                            )
                            StatCard(
                                label = "Cycles",
                                value = "${uiState.totalCyclesLogged}",
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        )
    }

    if (showChat) {
        CycleChatSheet(onDismiss = { showChat = false })
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    ClayCard(
        modifier = modifier.height(100.dp),
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
