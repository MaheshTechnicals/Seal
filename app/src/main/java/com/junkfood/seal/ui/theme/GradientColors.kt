package com.junkfood.seal.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Gradient Dark Theme Color Palette
 * Premium glassmorphism-based color scheme with gradient backgrounds
 */
@Immutable
data class GradientColors(
    // Primary Gradient (Purple to Pink)
    val gradientPrimaryStart: Color = Color(0xFF8B5CF6),
    val gradientPrimaryEnd: Color = Color(0xFFEC4899),
    
    // Secondary Gradient (Blue to Cyan)
    val gradientSecondaryStart: Color = Color(0xFF3B82F6),
    val gradientSecondaryEnd: Color = Color(0xFF06B6D4),
    
    // Accent Gradient (Violet to Fuchsia)
    val gradientAccentStart: Color = Color(0xFF7C3AED),
    val gradientAccentEnd: Color = Color(0xFFD946EF),
    
    // Dark Background Colors with depth
    val gradientDarkBackground: Color = Color(0xFF0A0A0F),
    val gradientDarkSurface: Color = Color(0xFF1A1A24),
    val gradientDarkSurfaceContainer: Color = Color(0xFF252535),
    val gradientDarkSurfaceContainerLow: Color = Color(0xFF1E1E2E),
    val gradientDarkSurfaceContainerHigh: Color = Color(0xFF2D2D40),
    
    // Glass/Frosted UI Elements
    val glassSurface: Color = Color(0x30FFFFFF),
    val glassSurfaceVariant: Color = Color(0x20FFFFFF),
    val glassBorder: Color = Color(0x40FFFFFF),
    val glassWhiteBorder: Color = Color(0x60FFFFFF),
    
    // Text Colors for perfect contrast
    val gradientDarkOnBackground: Color = Color(0xFFF5F5F7),
    val gradientDarkOnSurface: Color = Color(0xFFE5E5E9),
    val gradientDarkOnSurfaceVariant: Color = Color(0xFFB8B8BF),
    
    // Glow and Accent Colors
    val glowPrimary: Color = Color(0x40EC4899),
    val glowSecondary: Color = Color(0x4006B6D4),
    val glowAccent: Color = Color(0x40D946EF),
    
    // Overlay colors for glassmorphism
    val overlayLight: Color = Color(0x10FFFFFF),
    val overlayMedium: Color = Color(0x20FFFFFF),
    val overlayStrong: Color = Color(0x30FFFFFF),
)

val DefaultGradientColors = GradientColors()
