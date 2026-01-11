package com.junkfood.seal.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.Window
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextDirection
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.material.color.MaterialColors
import com.junkfood.seal.ui.common.LocalGradientDarkEnabled
import com.kyant.monet.dynamicColorScheme

fun Color.applyOpacity(enabled: Boolean): Color {
    return if (enabled) this else this.copy(alpha = 0.62f)
}

@Composable
fun Color.harmonizeWith(other: Color) =
    Color(MaterialColors.harmonize(this.toArgb(), other.toArgb()))

@Composable
fun Color.harmonizeWithPrimary(): Color =
    this.harmonizeWith(other = MaterialTheme.colorScheme.primary)


private tailrec fun Context.findWindow(): Window? =
    when (this) {
        is Activity -> window
        is ContextWrapper -> baseContext.findWindow()
        else -> null
    }

@OptIn(ExperimentalTextApi::class)
@Composable
fun SealTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isHighContrastModeEnabled: Boolean = false,
    isDynamicColorEnabled: Boolean = false,
    isGradientDarkEnabled: Boolean = false,
    content: @Composable () -> Unit
) {
    val gradientColors = DefaultGradientColors
    
    // Create gradient-enhanced color scheme when gradient dark is enabled
    val baseColorScheme = dynamicColorScheme(!darkTheme)
    
    val colorScheme = when {
        isGradientDarkEnabled && darkTheme -> {
            // Apply gradient dark theme with glassmorphism colors
            baseColorScheme.copy(
                primary = Color(gradientColors.gradientPrimaryEnd.toArgb()),
                onPrimary = Color(gradientColors.gradientDarkOnSurface.toArgb()),
                primaryContainer = Color(gradientColors.gradientDarkSurfaceContainer.toArgb()),
                onPrimaryContainer = Color(gradientColors.gradientDarkOnSurface.toArgb()),
                secondary = Color(gradientColors.gradientSecondaryEnd.toArgb()),
                onSecondary = Color(gradientColors.gradientDarkOnSurface.toArgb()),
                secondaryContainer = Color(gradientColors.gradientDarkSurfaceContainerLow.toArgb()),
                onSecondaryContainer = Color(gradientColors.gradientDarkOnSurface.toArgb()),
                tertiary = Color(gradientColors.gradientAccentEnd.toArgb()),
                onTertiary = Color(gradientColors.gradientDarkOnSurface.toArgb()),
                tertiaryContainer = Color(gradientColors.gradientDarkSurfaceContainerHigh.toArgb()),
                onTertiaryContainer = Color(gradientColors.gradientDarkOnSurface.toArgb()),
                background = Color(gradientColors.gradientDarkBackground.toArgb()),
                onBackground = Color(gradientColors.gradientDarkOnBackground.toArgb()),
                surface = Color(gradientColors.gradientDarkSurface.toArgb()),
                onSurface = Color(gradientColors.gradientDarkOnSurface.toArgb()),
                surfaceVariant = Color(gradientColors.gradientDarkSurfaceContainer.toArgb()),
                onSurfaceVariant = Color(gradientColors.gradientDarkOnSurfaceVariant.toArgb()),
                surfaceTint = Color(gradientColors.gradientPrimaryEnd.toArgb()),
                outline = Color(gradientColors.glassBorder.toArgb()),
                outlineVariant = Color(gradientColors.glassSurfaceVariant.toArgb()),
            )
        }
        isHighContrastModeEnabled && darkTheme -> {
            baseColorScheme.copy(
                surface = Color.Black,
                background = Color.Black,
            )
        }
        else -> baseColorScheme
    }
    
    val window = LocalView.current.context.findWindow()
    val view = LocalView.current

    window?.let {
        WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = darkTheme
    }

    rememberSystemUiController(window).setSystemBarsColor(Color.Transparent, !darkTheme)

    ProvideTextStyle(
        value = LocalTextStyle.current.copy(
            lineBreak = LineBreak.Paragraph,
            textDirection = TextDirection.Content
        )
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

@Composable
fun PreviewThemeLight(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = dynamicColorScheme(),
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}