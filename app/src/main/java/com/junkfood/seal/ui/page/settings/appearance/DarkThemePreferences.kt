package com.junkfood.seal.ui.page.settings.appearance


import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import com.junkfood.seal.ui.component.GradientScaffold
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.PreferenceSingleChoiceItem
import com.junkfood.seal.ui.component.PreferenceSubtitle
import com.junkfood.seal.ui.component.PreferenceSwitch
import com.junkfood.seal.util.DarkThemePreference.Companion.FOLLOW_SYSTEM
import com.junkfood.seal.util.DarkThemePreference.Companion.OFF
import com.junkfood.seal.util.DarkThemePreference.Companion.ON
import com.junkfood.seal.util.PreferenceUtil


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkThemePreferences(onNavigateBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true }
    )
    val darkThemePreference = LocalDarkTheme.current
    val isHighContrastModeEnabled = darkThemePreference.isHighContrastModeEnabled
    val isGradientDarkEnabled = darkThemePreference.isGradientDarkEnabled
    val isDarkThemeActive = darkThemePreference.isDarkTheme()
    
    GradientScaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        modifier = Modifier,
                        text = stringResource(id = R.string.dark_theme),
                    )
                }, navigationIcon = {
                    BackButton {
                        onNavigateBack()
                    }
                }, scrollBehavior = scrollBehavior
            )
        }, content = {
            LazyColumn(modifier = Modifier.padding(it)) {
                if (Build.VERSION.SDK_INT >= 29)
                    item {
                        PreferenceSingleChoiceItem(
                            text = stringResource(R.string.follow_system),
                            selected = darkThemePreference.darkThemeValue == FOLLOW_SYSTEM
                        ) { PreferenceUtil.modifyDarkThemePreference(FOLLOW_SYSTEM) }
                    }
                item {
                    PreferenceSingleChoiceItem(
                        text = stringResource(R.string.on),
                        selected = darkThemePreference.darkThemeValue == ON
                    ) { PreferenceUtil.modifyDarkThemePreference(ON) }
                }
                item {
                    PreferenceSingleChoiceItem(
                        text = stringResource(R.string.off),
                        selected = darkThemePreference.darkThemeValue == OFF
                    ) { 
                        // Auto-disable Gradient Dark when turning dark theme off
                        PreferenceUtil.modifyDarkThemePreference(
                            darkThemeValue = OFF,
                            isGradientDarkEnabled = false
                        ) 
                    }
                }
                item {
                    PreferenceSubtitle(text = stringResource(R.string.additional_settings))
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.gradient_dark),
                        description = stringResource(R.string.gradient_dark_desc),
                        icon = Icons.Outlined.AutoAwesome,
                        isChecked = isGradientDarkEnabled,
                        enabled = isDarkThemeActive,
                        onClick = {
                            if (isDarkThemeActive) {
                                PreferenceUtil.modifyDarkThemePreference(
                                    isGradientDarkEnabled = !isGradientDarkEnabled
                                )
                            }
                        }
                    )
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.high_contrast),
                        icon = Icons.Outlined.Contrast,
                        isChecked = isHighContrastModeEnabled, 
                        enabled = isDarkThemeActive && !isGradientDarkEnabled,
                        onClick = {
                            if (isDarkThemeActive && !isGradientDarkEnabled) {
                                PreferenceUtil.modifyDarkThemePreference(
                                    isHighContrastModeEnabled = !isHighContrastModeEnabled
                                )
                            }
                        }
                    )
                }
            }
        })
}