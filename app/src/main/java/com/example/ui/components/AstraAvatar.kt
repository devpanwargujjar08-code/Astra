package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AstraCyanLight
import com.example.ui.theme.AstraIndigoLight
import com.example.ui.theme.AstraRoseLight

@Composable
fun AstraAvatar(
    size: Dp = 36.dp,
    isThinking: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isThinking) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isThinking) 700 else 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isThinking) 0.8f else 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isThinking) 700 else 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size + 8.dp)
    ) {
        // Outer glowing halo
        Box(
            modifier = Modifier
                .size(size + 6.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            AstraIndigoLight.copy(alpha = glowAlpha),
                            AstraCyanLight.copy(alpha = glowAlpha * 0.5f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Main sphere
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            AstraIndigoLight,
                            AstraCyanLight
                        )
                    )
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Astra AI",
                tint = Color.White,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}
