package com.junkfood.seal.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.junkfood.seal.ui.common.LocalGradientDarkEnabled

/**
 * Quick Migration Helpers for Gradient Dark Theme
 * 
 * These extension functions make it easy to add gradient dark support
 * to existing screens without major refactoring.
 */

/**
 * Checks if gradient dark mode is currently active
 */
@Composable
fun isGradientDarkActive(): Boolean = LocalGradientDarkEnabled.current

/**
 * Returns appropriate background color based on gradient dark state
 */
@Composable
fun adaptiveBackgroundColor(
    lightColor: Color,
    darkColor: Color,
    gradientColor: Color = DefaultGradientColors.gradientDarkBackground
): Color {
    return when {
        isGradientDarkActive() -> gradientColor
        else -> darkColor
    }
}

/**
 * Returns appropriate surface color based on gradient dark state
 */
@Composable
fun adaptiveSurfaceColor(
    normalColor: Color,
    gradientColor: Color = DefaultGradientColors.gradientDarkSurface
): Color {
    return if (isGradientDarkActive()) gradientColor else normalColor
}

/**
 * Returns appropriate text color for gradient dark theme
 */
@Composable
fun adaptiveTextColor(
    normalColor: Color,
    gradientColor: Color = DefaultGradientColors.gradientDarkOnSurface
): Color {
    return if (isGradientDarkActive()) gradientColor else normalColor
}

/**
 * Wrap any screen content with automatic gradient background
 * 
 * Usage:
 * ```kotlin
 * WithGradientBackground {
 *     YourExistingScreenContent()
 * }
 * ```
 */
@Composable
fun WithGradientBackground(
    content: @Composable BoxScope.() -> Unit
) {
    val isGradientDark = LocalGradientDarkEnabled.current
    val gradientColors = DefaultGradientColors
    
    if (isGradientDark) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .gradientBackground(
                    startColor = gradientColors.gradientDarkBackground,
                    endColor = gradientColors.gradientDarkSurface,
                    angle = 135f
                )
        ) {
            content()
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

/**
 * Get gradient brush for custom gradient implementations
 */
@Composable
fun getPrimaryGradientBrush(): Brush {
    val colors = DefaultGradientColors
    return Brush.linearGradient(
        colors = listOf(
            colors.gradientPrimaryStart,
            colors.gradientPrimaryEnd
        )
    )
}

/**
 * Get secondary gradient brush
 */
@Composable
fun getSecondaryGradientBrush(): Brush {
    val colors = DefaultGradientColors
    return Brush.linearGradient(
        colors = listOf(
            colors.gradientSecondaryStart,
            colors.gradientSecondaryEnd
        )
    )
}

/**
 * Get accent gradient brush
 */
@Composable
fun getAccentGradientBrush(): Brush {
    val colors = DefaultGradientColors
    return Brush.linearGradient(
        colors = listOf(
            colors.gradientAccentStart,
            colors.gradientAccentEnd
        )
    )
}

/**
 * Extension for getting glassmorphism colors
 */
object GlassColors {
    @Composable
    fun surface() = DefaultGradientColors.glassSurface
    
    @Composable
    fun surfaceVariant() = DefaultGradientColors.glassSurfaceVariant
    
    @Composable
    fun border() = DefaultGradientColors.glassBorder
    
    @Composable
    fun whiteBorder() = DefaultGradientColors.glassWhiteBorder
}

/**
 * Extension for getting glow colors
 */
object GlowColors {
    @Composable
    fun primary() = DefaultGradientColors.glowPrimary
    
    @Composable
    fun secondary() = DefaultGradientColors.glowSecondary
    
    @Composable
    fun accent() = DefaultGradientColors.glowAccent
}

/**
 * Quick glassmorphism modifier that's always safe to use
 * Only applies effect when gradient dark is enabled
 */
@Composable
fun Modifier.autoGlassmorphism(): Modifier {
    val isEnabled = LocalGradientDarkEnabled.current
    return if (isEnabled) {
        this.glassmorphism(
            backgroundColor = GlassColors.surface(),
            borderColor = GlassColors.border()
        )
    } else {
        this
    }
}
