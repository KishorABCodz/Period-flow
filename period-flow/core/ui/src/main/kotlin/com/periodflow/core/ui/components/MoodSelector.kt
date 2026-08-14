package com.periodflow.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.periodflow.core.ui.theme.*

data class MoodOption(
    val name: String,
    val icon: ImageVector,
    val isSelected: Boolean,
)

@Composable
fun MoodSelector(
    moods: List<MoodOption>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Use a 4-column grid layout for moods
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        moods.chunked(4).forEach { rowMoods ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                rowMoods.forEach { mood ->
                    val index = moods.indexOf(mood)
                    MoodItem(
                        mood = mood,
                        onClick = { onSelect(index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MoodItem(
    mood: MoodOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else if (mood.isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "mood_scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else if (mood.isSelected) 8.dp else 4.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "mood_elevation"
    )

    val bgColor by animateColorAsState(
        targetValue = if (mood.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "mood_bg",
    )

    Column(
        modifier = modifier
            .width(64.dp)
            .scale(scale)
            .claymorphism(
                backgroundColor = bgColor,
                shape = RoundedCornerShape(20.dp),
                elevation = elevation
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 12.dp)
            .semantics { contentDescription = "Mood: ${mood.name}" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = mood.icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = if (mood.isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = mood.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (mood.isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
