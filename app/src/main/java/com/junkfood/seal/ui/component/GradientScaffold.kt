package com.junkfood.seal.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.junkfood.seal.ui.common.LocalGradientDarkEnabled
import com.junkfood.seal.ui.theme.DefaultGradientColors
import com.junkfood.seal.ui.theme.gradientBackground

/**
 * Enhanced Scaffold with Gradient Dark Theme support
 * Automatically applies gradient background when Gradient Dark is enabled
 */
@Composable
fun GradientScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: androidx.compose.foundation.layout.WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit
) {
    val isGradientDarkEnabled = LocalGradientDarkEnabled.current
    val gradientColors = DefaultGradientColors
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isGradientDarkEnabled) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "scaffold_gradient_alpha"
    )
    
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = if (isGradientDarkEnabled) Color.Transparent else containerColor,
        contentColor = if (isGradientDarkEnabled) gradientColors.gradientDarkOnBackground else contentColor,
        contentWindowInsets = contentWindowInsets
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Gradient background layer
            if (isGradientDarkEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = animatedAlpha }
                        .gradientBackground(
                            startColor = gradientColors.gradientDarkBackground,
                            endColor = gradientColors.gradientDarkSurface,
                            angle = 135f
                        )
                )
            }
            
            // Content
            content(paddingValues)
        }
    }
}
