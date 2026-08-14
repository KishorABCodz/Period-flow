package com.periodflow.feature.log

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.periodflow.core.domain.model.FlowIntensity
import com.periodflow.core.domain.model.Mood
import com.periodflow.core.domain.model.Symptom
import com.periodflow.core.domain.model.OvulationTestResult
import com.periodflow.core.ai.model.AiResult
import com.periodflow.core.ui.components.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.rounded.AutoAwesome

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    onNavigateBack: () -> Unit,
    viewModel: LogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Log — ${uiState.dateFormatted}", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::save) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = "Save",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Flow intensity section
            SectionHeader(title = "Flow Intensity", icon = null)
            FlowIntensitySelector(
                options = FlowIntensity.entries.map { flow ->
                    FlowOption(
                        name = flow.displayName,
                        icon = flow.icon,
                        color = when (flow) {
                            FlowIntensity.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
                            FlowIntensity.SPOTTING -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                            FlowIntensity.LIGHT -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                            FlowIntensity.MEDIUM -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                            FlowIntensity.HEAVY -> MaterialTheme.colorScheme.secondary
                        },
                        isSelected = uiState.selectedFlow == flow,
                    )
                },
                onSelect = { viewModel.onFlowSelected(FlowIntensity.entries[it]) },
            )

            // Mood section
            SectionHeader(title = "Mood", icon = null)
            MoodSelector(
                moods = Mood.entries.map { mood ->
                    MoodOption(
                        name = mood.displayName,
                        icon = mood.icon,
                        isSelected = uiState.selectedMood == mood,
                    )
                },
                onSelect = { viewModel.onMoodSelected(Mood.entries[it]) },
            )

            // Symptoms section
            SectionHeader(title = "Symptoms", icon = null)
            Text(
                text = "Tip: long-press any symptom for an AI explanation.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SymptomChipGrid(
                symptoms = Symptom.entries.map { symptom ->
                    SymptomOption(
                        name = symptom.displayName,
                        icon = symptom.icon,
                        isSelected = symptom in uiState.selectedSymptoms,
                    )
                },
                onToggle = { viewModel.onSymptomToggled(Symptom.entries[it]) },
                onExplain = { viewModel.openSymptomExplainer(Symptom.entries[it]) },
            )

            // Ovulation Test Result
            SectionHeader(title = "Ovulation Test", icon = null)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OvulationTestResult.entries.forEach { result ->
                    ClayChip(
                        selected = uiState.ovulationTestResult == result,
                        onClick = { viewModel.onOvulationTestResultSelected(result) },
                        activeColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                        inactiveColor = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = result.displayName, 
                            color = if (uiState.ovulationTestResult == result) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (uiState.ovulationTestResult == result) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Weight section
            SectionHeader(title = "Weight (kg)", icon = null)
            OutlinedTextField(
                value = uiState.weightKg,
                onValueChange = viewModel::onWeightChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. 65.5", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            // Notes section
            SectionHeader(title = "Notes", icon = null)
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("How are you feeling today?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                minLines = 3,
                maxLines = 5,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // AI Symptom Explainer bottom sheet
    val explainerSymptom = uiState.explainerOpenFor
    if (explainerSymptom != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeSymptomExplainer() },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant) },
        ) {
            SymptomExplainerContent(
                symptomName = explainerSymptom.displayName,
                state = uiState.explainerResult,
                onRetry = { viewModel.openSymptomExplainer(explainerSymptom) },
            )
        }
    }
}

@Composable
private fun SymptomExplainerContent(
    symptomName: String,
    state: AiResult<String>,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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
                text = symptomName,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        when (state) {
            AiResult.Idle,
            AiResult.Loading -> {
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
                        text = "Asking Gemini for a gentle explanation…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            is AiResult.Success -> {
                Text(
                    text = state.value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            is AiResult.Error -> {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                ClayButton(
                    onClick = onRetry,
                    backgroundColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.height(44.dp).widthIn(min = 140.dp),
                ) {
                    Text(
                        text = "Try again",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Icon(imageVector = it, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
