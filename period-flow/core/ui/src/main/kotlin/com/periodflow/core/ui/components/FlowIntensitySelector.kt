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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.periodflow.core.ui.theme.*

data class FlowOption(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val isSelected: Boolean,
)

@Composable
fun FlowIntensitySelector(
    options: List<FlowOption>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        options.forEachIndexed { index, option ->
            FlowIntensityItem(
                option = option,
                onClick = { onSelect(index) },
            )
        }
    }
}

@Composable
private fun FlowIntensityItem(
    option: FlowOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else if (option.isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "flow_scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else if (option.isSelected) 10.dp else 4.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "flow_elevation"
    )

        val bgColor by animateColorAsState(
        targetValue = if (option.isSelected) option.color else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "flow_bg",
    )

    Column(
        modifier = modifier
            .width(64.dp)
            .scale(scale)
            .claymorphism(
                backgroundColor = bgColor,
                shape = RoundedCornerShape(50),
                elevation = elevation
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = option.icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = if (option.isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = option.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (option.isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
