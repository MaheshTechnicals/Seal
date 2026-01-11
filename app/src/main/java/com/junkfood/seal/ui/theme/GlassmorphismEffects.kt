package com.junkfood.seal.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Glassmorphism Modifier Extensions for Seal's Gradient Dark Theme
 */

/**
 * Apply glassmorphism effect with frosted glass appearance
 * @param backgroundColor Base color with transparency
 * @param borderColor Color for the glass border
 * @param blur Amount of blur to apply
 * @param shape Shape of the glass surface
 */
fun Modifier.glassmorphism(
    backgroundColor: Color,
    borderColor: Color,
    blur: Dp = 12.dp,
    shape: Shape = RoundedCornerShape(16.dp)
): Modifier = this
    .clip(shape)
    .background(backgroundColor)
    .border(BorderStroke(1.dp, borderColor), shape)

/**
 * Apply animated glassmorphism effect with Material You motion
 */
@Composable
fun Modifier.animatedGlassmorphism(
    enabled: Boolean,
    backgroundColor: Color,
    borderColor: Color,
    blur: Dp = 12.dp,
    shape: Shape = RoundedCornerShape(16.dp)
): Modifier {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "glassmorphism_alpha"
    )
    
    return this
        .clip(shape)
        .graphicsLayer { alpha = animatedAlpha }
        .background(backgroundColor)
        .border(BorderStroke(1.dp, borderColor), shape)
}

/**
 * Apply gradient background with smooth blending
 */
fun Modifier.gradientBackground(
    startColor: Color,
    endColor: Color,
    angle: Float = 135f
): Modifier = this.drawBehind {
    val angleRad = Math.toRadians(angle.toDouble())
    val x = kotlin.math.cos(angleRad).toFloat()
    val y = kotlin.math.sin(angleRad).toFloat()
    
    val start = Offset(
        x = if (x > 0) 0f else size.width,
        y = if (y > 0) 0f else size.height
    )
    val end = Offset(
        x = if (x > 0) size.width else 0f,
        y = if (y > 0) size.height else 0f
    )
    
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(startColor, endColor),
            start = start,
            end = end
        )
    )
}

/**
 * Apply animated gradient background with smooth transitions
 */
@Composable
fun Modifier.animatedGradientBackground(
    enabled: Boolean,
    startColor: Color,
    endColor: Color,
    angle: Float = 135f
): Modifier {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "gradient_alpha"
    )
    
    return this.drawBehind {
        if (enabled) {
            val angleRad = Math.toRadians(angle.toDouble())
            val x = kotlin.math.cos(angleRad).toFloat()
            val y = kotlin.math.sin(angleRad).toFloat()
            
            val start = Offset(
                x = if (x > 0) 0f else size.width,
                y = if (y > 0) 0f else size.height
            )
            val end = Offset(
                x = if (x > 0) size.width else 0f,
                y = if (y > 0) size.height else 0f
            )
            
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(startColor, endColor),
                    start = start,
                    end = end
                ),
                alpha = animatedAlpha
            )
        }
    }
}

/**
 * Apply glow effect around content
 */
fun Modifier.glowEffect(
    glowColor: Color,
    blurRadius: Dp = 16.dp,
    shape: Shape = RoundedCornerShape(16.dp)
): Modifier = this
    .graphicsLayer {
        shadowElevation = blurRadius.toPx()
        this.shape = shape
        clip = false
    }
    .drawBehind {
        drawRect(color = glowColor)
    }

/**
 * Apply frosted card effect with blur and glass border
 */
fun Modifier.frostedCard(
    backgroundColor: Color,
    borderColor: Color,
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 0.dp
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(backgroundColor)
    .border(
        BorderStroke(1.dp, borderColor),
        RoundedCornerShape(cornerRadius)
    )

/**
 * Animated frosted card with Material You motion
 */
@Composable
fun Modifier.animatedFrostedCard(
    enabled: Boolean,
    backgroundColor: Color,
    borderColor: Color,
    cornerRadius: Dp = 16.dp
): Modifier {
    val animatedRadius by animateDpAsState(
        targetValue = if (enabled) cornerRadius else 0.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "card_radius"
    )
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "card_alpha"
    )
    
    return this
        .clip(RoundedCornerShape(animatedRadius))
        .graphicsLayer { alpha = animatedAlpha }
        .background(backgroundColor)
        .border(
            BorderStroke(1.dp, borderColor),
            RoundedCornerShape(animatedRadius)
        )
}

/**
 * Apply multi-layer gradient background
 */
fun Modifier.multiLayerGradient(
    layers: List<Pair<Color, Color>>,
    angle: Float = 135f
): Modifier = this.drawBehind {
    layers.forEach { (start, end) ->
        val angleRad = Math.toRadians(angle.toDouble())
        val x = kotlin.math.cos(angleRad).toFloat()
        val y = kotlin.math.sin(angleRad).toFloat()
        
        val startOffset = Offset(
            x = if (x > 0) 0f else size.width,
            y = if (y > 0) 0f else size.height
        )
        val endOffset = Offset(
            x = if (x > 0) size.width else 0f,
            y = if (y > 0) size.height else 0f
        )
        
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(start, end),
                start = startOffset,
                end = endOffset
            )
        )
    }
}

/**
 * Apply radial gradient from center
 */
fun Modifier.radialGradientBackground(
    centerColor: Color,
    edgeColor: Color
): Modifier = this.drawBehind {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(centerColor, edgeColor),
            center = Offset(size.width / 2, size.height / 2),
            radius = maxOf(size.width, size.height) / 2
        )
    )
}

/**
 * Composable wrapper for gradient dark background
 */
@Composable
fun GradientDarkBackground(
    modifier: Modifier = Modifier,
    gradientColors: GradientColors = DefaultGradientColors,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .gradientBackground(
                startColor = gradientColors.gradientDarkBackground,
                endColor = gradientColors.gradientDarkSurface,
                angle = 135f
            )
    ) {
        content()
    }
}

/**
 * Animated gradient dark background with smooth transitions
 */
@Composable
fun AnimatedGradientDarkBackground(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    gradientColors: GradientColors = DefaultGradientColors,
    content: @Composable BoxScope.() -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "background_alpha"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = animatedAlpha }
            .gradientBackground(
                startColor = gradientColors.gradientDarkBackground,
                endColor = gradientColors.gradientDarkSurface,
                angle = 135f
            )
    ) {
        content()
    }
}
