package com.periodflow.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.periodflow.core.ui.theme.*

@Composable
fun OnboardingRoute(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is OnboardingEvent.OnboardingFinished -> onComplete()
            }
        }
    }

    OnboardingScreen(
        uiState = uiState,
        viewModel = viewModel,
        modifier = modifier
    )
}

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.currentStep > 1) {
                    TextButton(onClick = { viewModel.prevStep() }) {
                        Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Spacer(modifier = Modifier.width(64.dp))
                }

                Text(
                    text = "Step ${uiState.currentStep} of 3",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ClayButton(
                    onClick = { viewModel.nextStep() },
                    backgroundColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.width(100.dp)
                ) {
                    Text(
                        text = if (uiState.currentStep == 3) "Finish" else "Next",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith 
                    slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                },
                label = "onboarding_step_transition"
            ) { step ->
                when (step) {
                    1 -> StepOne(uiState, viewModel)
                    2 -> StepTwo(uiState, viewModel)
                    3 -> StepThree(uiState, viewModel)
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun StepOne(uiState: OnboardingUiState, viewModel: OnboardingViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Welcome to PeriodFlow",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Let's start with your cycle basics.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(48.dp))

        ClayCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Average Cycle Length", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                NumberSelector(
                    value = uiState.cycleLength,
                    onValueChange = viewModel::updateCycleLength,
                    range = 20..40
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ClayCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Average Period Length", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                NumberSelector(
                    value = uiState.periodLength,
                    onValueChange = viewModel::updatePeriodLength,
                    range = 2..10
                )
            }
        }
    }
}

@Composable
private fun StepTwo(uiState: OnboardingUiState, viewModel: OnboardingViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Your Bio Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "This helps us tailor insights to you.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(48.dp))

        ClayCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Height (cm)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                NumberSelector(
                    value = (uiState.heightCm ?: 165f).toInt(),
                    onValueChange = { viewModel.updateHeight(it.toFloat()) },
                    range = 100..250
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ClayCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Weight (kg)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                NumberSelector(
                    value = (uiState.weightKg ?: 65f).toInt(),
                    onValueChange = { viewModel.updateWeight(it.toFloat()) },
                    range = 30..200
                )
            }
        }
    }
}

@Composable
private fun StepThree(uiState: OnboardingUiState, viewModel: OnboardingViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Health Insights",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Tell us about any specific conditions.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(48.dp))

        ClayCard(modifier = Modifier.fillMaxWidth(), backgroundColor = MaterialTheme.colorScheme.surfaceVariant) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Have you been diagnosed with PCOS/PCOD?", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ChoiceChip(
                        label = "Yes",
                        selected = uiState.hasPolycysticOvaries == true,
                        onClick = { viewModel.updatePcosDiagnosis(true) },
                        modifier = Modifier.weight(1f)
                    )
                    ChoiceChip(
                        label = "No",
                        selected = uiState.hasPolycysticOvaries == false,
                        onClick = { viewModel.updatePcosDiagnosis(false) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ClayCard(modifier = Modifier.fillMaxWidth(), backgroundColor = MaterialTheme.colorScheme.surfaceVariant) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Do you experience hormonal acne?", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("None", "Mild", "Severe").forEach { severity ->
                        ChoiceChip(
                            label = severity,
                            selected = uiState.acneSeverity == severity,
                            onClick = { viewModel.updateAcneSeverity(severity) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        ClayCard(modifier = Modifier.fillMaxWidth(), backgroundColor = MaterialTheme.colorScheme.surfaceVariant) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Do you experience excess facial/body hair (Hirsutism)?", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("None", "Mild", "Severe").forEach { severity ->
                        ChoiceChip(
                            label = severity,
                            selected = uiState.hirsutismSeverity == severity,
                            onClick = { viewModel.updateHirsutismSeverity(severity) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ClayButton(
        onClick = onClick,
        backgroundColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        modifier = modifier.height(40.dp)
    ) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun NumberSelector(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ClayButton(
            onClick = { if (value > range.first) onValueChange(value - 1) },
            modifier = Modifier.size(48.dp)
        ) {
            Text(text = "-", style = MaterialTheme.typography.titleLarge)
        }

        Text(
            text = "$value",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        ClayButton(
            onClick = { if (value < range.last) onValueChange(value + 1) },
            modifier = Modifier.size(48.dp)
        ) {
            Text(text = "+", style = MaterialTheme.typography.titleLarge)
        }
    }
}
