package com.periodflow.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.periodflow.core.ui.theme.*

@Composable
fun CycleRing(
    currentDay: Int,
    totalDays: Int,
    periodLength: Int,
    phaseIcon: androidx.compose.ui.graphics.vector.ImageVector?,
    phaseName: String,
    modifier: Modifier = Modifier,
    size: Dp = 240.dp,
    strokeWidth: Dp = 24.dp,
) {
    val progress = if (totalDays > 0) currentDay.toFloat() / totalDays.toFloat() else 0f
    
    // Smooth entry animation for the ring
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 100f
        ),
        label = "cycle_progress",
    )

    val periodProgress = if (totalDays > 0) periodLength.toFloat() / totalDays.toFloat() else 0f

    // Soft breathing effect for the center content
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe_scale"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        val colorSurfaceVariant = MaterialTheme.colorScheme.surfaceVariant
        val colorSecondary = MaterialTheme.colorScheme.secondary
        val colorPrimary = MaterialTheme.colorScheme.primary

        Canvas(
            modifier = Modifier.fillMaxSize().padding(strokeWidth / 2),
        ) {
            val canvasSize = this.size
            val arcSize = Size(canvasSize.width, canvasSize.height)
            val topLeft = Offset.Zero
            val width = strokeWidth.toPx()

            // 1. Shadow for the clay ring
            drawArc(
                color = Color.Black.copy(alpha = 0.3f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(topLeft.x, topLeft.y + 12f),
                size = arcSize,
                style = Stroke(width = width, cap = StrokeCap.Round),
            )
            
            // 2. Base Clay Ring
            drawArc(
                color = colorSurfaceVariant,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = width, cap = StrokeCap.Round),
            )

            // 3. Period segment (Coral)
            drawArc(
                color = colorSecondary,
                startAngle = -90f,
                sweepAngle = 360f * periodProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = width, cap = StrokeCap.Round),
            )

            // 4. Current progress arc (Periwinkle)
            drawArc(
                color = colorPrimary,
                startAngle = -90f + (360f * periodProgress),
                sweepAngle = 360f * (animatedProgress - periodProgress).coerceAtLeast(0f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = width, cap = StrokeCap.Round),
            )

            // 5. Inner highlight to give it that 3D rounded clay feel
            drawArc(
                color = Color.White.copy(alpha = 0.05f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(topLeft.x + 2f, topLeft.y + 2f),
                size = Size(arcSize.width - 4f, arcSize.height - 4f),
                style = Stroke(width = width * 0.2f, cap = StrokeCap.Round),
            )
        }

        // Center content with breathing and AnimatedContent
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.graphicsLayer(
                scaleX = breatheScale,
                scaleY = breatheScale
            )
        ) {
            phaseIcon?.let {
                androidx.compose.material3.Icon(
                    imageVector = it,
                    contentDescription = phaseName,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Day",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            AnimatedContent(
                targetState = currentDay,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInVertically { height -> height } + fadeIn() togetherWith
                                slideOutVertically { height -> -height } + fadeOut()
                    } else {
                        slideInVertically { height -> -height } + fadeIn() togetherWith
                                slideOutVertically { height -> height } + fadeOut()
                    }.using(
                        SizeTransform(clip = false)
                    )
                }, label = "day_animation"
            ) { targetDay ->
                Text(
                    text = targetDay.toString(),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Soft clay status pill
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = androidx.compose.foundation.shape.CircleShape)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = phaseName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
