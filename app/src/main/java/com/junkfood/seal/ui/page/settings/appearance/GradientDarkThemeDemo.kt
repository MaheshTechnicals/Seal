package com.junkfood.seal.ui.page.settings.appearance

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkfood.seal.ui.common.LocalGradientDarkEnabled
import com.junkfood.seal.ui.component.GradientCard
import com.junkfood.seal.ui.component.GradientScaffold
import com.junkfood.seal.ui.theme.DefaultGradientColors
import com.junkfood.seal.ui.theme.glassmorphism
import com.junkfood.seal.ui.theme.gradientBackground

/**
 * Example Screen Demonstrating Gradient Dark Theme
 * 
 * This is a reference implementation showing how to use all gradient dark features.
 * Use this as a template for updating existing screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradientDarkThemeDemo() {
    val isGradientDarkEnabled = LocalGradientDarkEnabled.current
    val gradientColors = DefaultGradientColors
    
    // Animated scale effect for demonstration
    val scale by animateFloatAsState(
        targetValue = if (isGradientDarkEnabled) 1f else 0.98f,
        animationSpec = tween(durationMillis = 300),
        label = "demo_scale"
    )
    
    GradientScaffold(
        topBar = {
            // Top App Bar with gradient when enabled
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isGradientDarkEnabled) {
                            Modifier.gradientBackground(
                                startColor = gradientColors.gradientPrimaryStart,
                                endColor = gradientColors.gradientPrimaryEnd,
                                angle = 90f
                            )
                        } else Modifier
                    ),
                color = if (isGradientDarkEnabled) 
                    androidx.compose.ui.graphics.Color.Transparent 
                else MaterialTheme.colorScheme.surface,
                tonalElevation = if (isGradientDarkEnabled) 0.dp else 3.dp
            ) {
                TopAppBar(
                    title = { 
                        Text(
                            "Gradient Dark Demo",
                            color = if (isGradientDarkEnabled) 
                                gradientColors.gradientDarkOnSurface 
                            else MaterialTheme.colorScheme.onSurface
                        ) 
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .graphicsLayer { scaleX = scale; scaleY = scale },
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card with Primary Gradient
            item {
                GradientCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (isGradientDarkEnabled) {
                                    Modifier.gradientBackground(
                                        startColor = gradientColors.gradientPrimaryStart,
                                        endColor = gradientColors.gradientPrimaryEnd
                                    )
                                } else Modifier
                            )
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = if (isGradientDarkEnabled)
                                    gradientColors.gradientDarkOnSurface
                                else MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Gradient Dark Active",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGradientDarkEnabled)
                                    gradientColors.gradientDarkOnSurface
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            
            // Feature Cards
            item {
                Text(
                    "Features",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = if (isGradientDarkEnabled)
                        gradientColors.gradientDarkOnBackground
                    else MaterialTheme.colorScheme.onBackground
                )
            }
            
            // Glassmorphism Card
            item {
                FeatureCard(
                    icon = Icons.Outlined.Visibility,
                    title = "Glassmorphism",
                    description = "Frosted glass effect with soft blur and borders",
                    isGradient = isGradientDarkEnabled
                )
            }
            
            // Gradient Background Card
            item {
                FeatureCard(
                    icon = Icons.Outlined.Gradient,
                    title = "Gradient Backgrounds",
                    description = "Multi-layer gradients blending purple, pink, and blue",
                    isGradient = isGradientDarkEnabled
                )
            }
            
            // Animations Card
            item {
                FeatureCard(
                    icon = Icons.Outlined.Animation,
                    title = "Smooth Animations",
                    description = "Material You motion with fade, scale, and slide effects",
                    isGradient = isGradientDarkEnabled
                )
            }
            
            // Responsive Design Card
            item {
                FeatureCard(
                    icon = Icons.Outlined.DevicesOther,
                    title = "Responsive Design",
                    description = "Adaptive spacing and corners for all screen sizes",
                    isGradient = isGradientDarkEnabled
                )
            }
            
            // Color Palette Section
            item {
                Text(
                    "Color Palette",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = if (isGradientDarkEnabled)
                        gradientColors.gradientDarkOnBackground
                    else MaterialTheme.colorScheme.onBackground
                )
            }
            
            // Primary Gradient Sample
            item {
                ColorSampleCard(
                    title = "Primary Gradient",
                    startColor = gradientColors.gradientPrimaryStart,
                    endColor = gradientColors.gradientPrimaryEnd,
                    isActive = isGradientDarkEnabled
                )
            }
            
            // Secondary Gradient Sample
            item {
                ColorSampleCard(
                    title = "Secondary Gradient",
                    startColor = gradientColors.gradientSecondaryStart,
                    endColor = gradientColors.gradientSecondaryEnd,
                    isActive = isGradientDarkEnabled
                )
            }
            
            // Accent Gradient Sample
            item {
                ColorSampleCard(
                    title = "Accent Gradient",
                    startColor = gradientColors.gradientAccentStart,
                    endColor = gradientColors.gradientAccentEnd,
                    isActive = isGradientDarkEnabled
                )
            }
        }
    }
}

@Composable
private fun FeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isGradient: Boolean
) {
    val gradientColors = DefaultGradientColors
    
    GradientCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .then(
                        if (isGradient) {
                            Modifier.glassmorphism(
                                backgroundColor = gradientColors.glassSurfaceVariant,
                                borderColor = gradientColors.glassBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                        } else Modifier
                    ),
                shape = RoundedCornerShape(12.dp),
                color = if (isGradient) 
                    androidx.compose.ui.graphics.Color.Transparent 
                else MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (isGradient)
                            gradientColors.gradientPrimaryEnd
                        else MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isGradient)
                        gradientColors.gradientDarkOnSurface
                    else MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isGradient)
                        gradientColors.gradientDarkOnSurfaceVariant
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ColorSampleCard(
    title: String,
    startColor: androidx.compose.ui.graphics.Color,
    endColor: androidx.compose.ui.graphics.Color,
    isActive: Boolean
) {
    val gradientColors = DefaultGradientColors
    
    GradientCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = if (isActive)
                    gradientColors.gradientDarkOnSurface
                else MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(Modifier.height(12.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .gradientBackground(
                        startColor = startColor,
                        endColor = endColor,
                        angle = 135f
                    )
            )
        }
    }
}
