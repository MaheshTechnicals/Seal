package com.junkfood.seal.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.junkfood.seal.ui.common.LocalGradientDarkEnabled
import com.junkfood.seal.ui.theme.DefaultGradientColors
import com.junkfood.seal.ui.theme.frostedCard
import com.junkfood.seal.ui.theme.glassmorphism

/**
 * Card component that adapts to Gradient Dark theme
 * Applies glassmorphism when gradient dark is enabled
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: Dp = 1.dp,
    content: @Composable () -> Unit
) {
    val isGradientDarkEnabled = LocalGradientDarkEnabled.current
    val gradientColors = DefaultGradientColors
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isGradientDarkEnabled) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "card_gradient_alpha"
    )
    
    if (isGradientDarkEnabled) {
        Surface(
            modifier = modifier
                .frostedCard(
                    backgroundColor = gradientColors.glassSurface,
                    borderColor = gradientColors.glassBorder,
                    cornerRadius = 16.dp
                )
                .graphicsLayer { alpha = animatedAlpha },
            shape = shape,
            color = Color.Transparent
        ) {
            content()
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            content()
        }
    }
}

/**
 * Surface component that adapts to Gradient Dark theme
 * Applies glassmorphism when gradient dark is enabled
 */
@Composable
fun GradientSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    val isGradientDarkEnabled = LocalGradientDarkEnabled.current
    val gradientColors = DefaultGradientColors
    
    if (isGradientDarkEnabled) {
        Surface(
            modifier = modifier
                .glassmorphism(
                    backgroundColor = gradientColors.glassSurfaceVariant,
                    borderColor = gradientColors.glassBorder,
                    shape = shape
                ),
            shape = shape,
            color = Color.Transparent,
            contentColor = gradientColors.gradientDarkOnSurface
        ) {
            content()
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = color,
            contentColor = contentColor,
            tonalElevation = tonalElevation,
            shadowElevation = shadowElevation
        ) {
            content()
        }
    }
}

/**
 * Container with glassmorphism effect for list items
 */
@Composable
fun GlassmorphicContainer(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val isGradientDarkEnabled = LocalGradientDarkEnabled.current
    val gradientColors = DefaultGradientColors
    
    if (isGradientDarkEnabled && enabled) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(gradientColors.glassSurface)
                .border(
                    width = 1.dp,
                    color = gradientColors.glassBorder,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            content()
        }
    } else {
        Box(modifier = modifier) {
            content()
        }
    }
}
