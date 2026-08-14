package com.periodflow.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.periodflow.core.ui.theme.*

@Composable
fun PhaseChip(
    phaseName: String,
    emoji: String,
    phaseColor: Color,
    modifier: Modifier = Modifier,
) {
    val animatedColor by animateColorAsState(
        targetValue = phaseColor,
        animationSpec = tween(500),
        label = "phase_color",
    )

    Row(
        modifier = modifier
            .claymorphism(
                backgroundColor = animatedColor.copy(alpha = 0.15f),
                shape = MaterialTheme.shapes.large,
                elevation = 6.dp
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = emoji, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = phaseName,
            style = MaterialTheme.typography.labelLarge,
            color = animatedColor,
        )
    }
}
