package com.junkfood.seal.ui.page.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.junkfood.seal.R
import com.junkfood.seal.ui.theme.GradientDarkColors
import com.junkfood.seal.ui.theme.SealTheme

/**
 * SponsorSupportDialog — shown periodically to invite users to support development.
 *
 * Features:
 *  • Spring-animated scale+alpha entrance
 *  • Glassmorphism card matching the app's dark gradient theme
 *  • Feature highlight rows
 *  • "Support Us" (primary gradient) and "Maybe Later" (text) actions
 */
@Composable
fun SponsorSupportDialog(
    onDismiss: () -> Unit,
    onSupport: () -> Unit,
) {
    // Track whether the composable is "visible" so we can drive the entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.82f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "sponsorDialogScale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "sponsorDialogAlpha",
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .graphicsLayer { this.alpha = alpha; this.scaleX = scale; this.scaleY = scale }
                .clip(RoundedCornerShape(28.dp))
                // Glass background: dark surface with slight transparency
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GradientDarkColors.SurfaceContainer.copy(alpha = 0.96f),
                            GradientDarkColors.SurfaceVariant.copy(alpha = 0.98f),
                        )
                    )
                )
                // Subtle gradient border (glassmorphism rim)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0x33FFFFFF),
                            GradientDarkColors.GradientPrimaryEnd.copy(alpha = 0.35f),
                            Color(0x11FFFFFF),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    ),
                    shape = RoundedCornerShape(28.dp),
                ),
        ) {
            // Decorative accent glow in the top-right corner
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GradientDarkColors.GradientPrimaryEnd.copy(alpha = 0.14f),
                                Color.Transparent,
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                // ── Icon badge ──────────────────────────────────────────────
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    GradientDarkColors.GradientPrimaryStart,
                                    GradientDarkColors.GradientAccentStart,
                                )
                            )
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VolunteerActivism,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp),
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ── Title ───────────────────────────────────────────────────
                Text(
                    text = stringResource(R.string.sponsor_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(6.dp))

                // ── Subtitle badge ──────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    GradientDarkColors.GradientPrimaryStart.copy(alpha = 0.30f),
                                    GradientDarkColors.GradientAccentEnd.copy(alpha = 0.30f),
                                )
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = GradientDarkColors.GradientPrimaryEnd,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.sponsor_dialog_subtitle),
                            style = MaterialTheme.typography.labelSmall,
                            color = GradientDarkColors.GradientPrimaryEnd,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Body text ───────────────────────────────────────────────
                Text(
                    text = stringResource(R.string.sponsor_dialog_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xCCFFFFFF),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ── Feature rows ────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x14FFFFFF))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SponsorFeatureRow(
                        icon = Icons.Outlined.Update,
                        text = stringResource(R.string.sponsor_feature_updates),
                        iconColor = GradientDarkColors.GradientSecondaryEnd,
                    )
                    SponsorFeatureRow(
                        icon = Icons.Outlined.Speed,
                        text = stringResource(R.string.sponsor_feature_performance),
                        iconColor = GradientDarkColors.GradientPrimaryEnd,
                    )
                    SponsorFeatureRow(
                        icon = Icons.Outlined.Star,
                        text = stringResource(R.string.sponsor_feature_features),
                        iconColor = GradientDarkColors.GradientAccentStart,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── "Support Us" button ─────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    GradientDarkColors.GradientPrimaryStart,
                                    GradientDarkColors.GradientAccentStart,
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Button(
                        onClick = onSupport,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White,
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.support_us),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── "Maybe Later" text button ────────────────────────────────
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.maybe_later),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0x99FFFFFF),
                    )
                }
            }
        }
    }
}

@Composable
private fun SponsorFeatureRow(
    icon: ImageVector,
    text: String,
    iconColor: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconColor.copy(alpha = 0.18f)),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xDDFFFFFF),
            fontWeight = FontWeight.Medium,
        )
    }
}

@Preview(showBackground = false)
@Composable
private fun SponsorSupportDialogPreview() {
    SealTheme {
        SponsorSupportDialog(onDismiss = {}, onSupport = {})
    }
}
