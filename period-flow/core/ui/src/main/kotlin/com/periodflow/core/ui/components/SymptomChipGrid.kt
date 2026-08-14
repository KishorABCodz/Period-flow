package com.periodflow.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.periodflow.core.ui.theme.*

data class SymptomOption(
    val name: String,
    val icon: ImageVector,
    val isSelected: Boolean,
)

@Composable
fun SymptomChipGrid(
    symptoms: List<SymptomOption>,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onExplain: ((Int) -> Unit)? = null,
) {
    // Use FlowRow for wrapping chips
    @OptIn(ExperimentalLayoutApi::class)
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        symptoms.forEachIndexed { index, symptom ->
            ClayChip(
                selected = symptom.isSelected,
                onClick = { onToggle(index) },
                onLongClick = onExplain?.let { { it(index) } },
                activeColor = MaterialTheme.colorScheme.secondary,
                inactiveColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(
                    imageVector = symptom.icon,
                    contentDescription = symptom.name,
                    modifier = Modifier.size(20.dp),
                    tint = if (symptom.isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = symptom.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (symptom.isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
