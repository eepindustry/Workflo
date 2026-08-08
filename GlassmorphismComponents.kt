package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(28.dp),
    content: @Composable RowScope.() -> Unit
) {
    val gradient = if (enabled) {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF81D4FA),
                Color(0xFF4FC3F7),
                Color(0xFF64B5F6)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color.Gray,
                Color.DarkGray
            )
        )
    }
    
    Box(
        modifier = modifier
            .clip(shape)
            .background(gradient)
            .clickable(enabled = enabled, onClick = onClick)
            .then(
                Modifier.defaultMinSize(
                    minWidth = ButtonDefaults.MinWidth,
                    minHeight = ButtonDefaults.MinHeight
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        ProvideTextStyle(value = androidx.compose.material3.MaterialTheme.typography.labelLarge) {
            Row(
                Modifier.padding(PaddingValues(horizontal = 24.dp, vertical = 8.dp)),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color.Black.copy(alpha = 0.85f),
            Color.Black.copy(alpha = 0.75f)
        )
    )
    val borderBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.2f),
            Color.White.copy(alpha = 0.1f)
        )
    )

    var boxModifier = modifier
        .clip(shape)
        .background(backgroundBrush)
        .border(1.dp, borderBrush, shape)
        
    if (onClick != null) {
        boxModifier = boxModifier.clickable(onClick = onClick)
    }

    Box(modifier = boxModifier) {
        content()
    }
}
