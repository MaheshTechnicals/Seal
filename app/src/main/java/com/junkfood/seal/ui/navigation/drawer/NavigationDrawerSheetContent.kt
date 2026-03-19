package com.junkfood.seal.ui.navigation.drawer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.Route
import com.junkfood.seal.ui.common.ThemedIconColors
import kotlinx.coroutines.launch

@Composable
fun NavigationDrawerSheetContent(
    modifier: Modifier = Modifier,
    currentRoute: String? = null,
    showQuickSettings: Boolean = true,
    onNavigateToRoute: (String) -> Unit,
    onDismissRequest: suspend () -> Unit,
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier =
            modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
    ) {
        // Modern gradient header
        DrawerHeader()

        Spacer(Modifier.height(16.dp))

        // Group 1: Primary Destinations
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.home)) },
                    icon = { Icon(Icons.Filled.Download, null, tint = ThemedIconColors.primary) },
                    onClick = {
                        scope
                            .launch { onDismissRequest() }
                            .invokeOnCompletion { onNavigateToRoute(Route.HOME) }
                    },
                    selected = false,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.downloads_history)) },
                    icon = {
                        Icon(
                            Icons.Outlined.Subscriptions,
                            null,
                            tint = ThemedIconColors.secondary
                        )
                    },
                    onClick = {
                        scope
                            .launch { onDismissRequest() }
                            .invokeOnCompletion { onNavigateToRoute(Route.DOWNLOADS) }
                    },
                    selected = false,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.custom_command)) },
                    icon = {
                        Icon(
                            Icons.Outlined.Terminal,
                            null,
                            tint = ThemedIconColors.tertiary
                        )
                    },
                    onClick = {
                        scope
                            .launch { onDismissRequest() }
                            .invokeOnCompletion { onNavigateToRoute(Route.TASK_LIST) }
                    },
                    selected = false,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Group 2: Utilities & Support
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.settings)) },
                    icon = { Icon(Icons.Outlined.Settings, null, tint = ThemedIconColors.primary) },
                    onClick = {
                        scope
                            .launch { onDismissRequest() }
                            .invokeOnCompletion { onNavigateToRoute(Route.SETTINGS) }
                    },
                    selected = false,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.trouble_shooting)) },
                    icon = {
                        Icon(
                            Icons.Rounded.BugReport,
                            null,
                            tint = ThemedIconColors.secondary
                        )
                    },
                    onClick = {
                        scope
                            .launch { onDismissRequest() }
                            .invokeOnCompletion { onNavigateToRoute(Route.TROUBLESHOOTING) }
                    },
                    selected = false,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.sponsor)) },
                    icon = {
                        Icon(
                            Icons.Outlined.VolunteerActivism,
                            null,
                            tint = ThemedIconColors.tertiary
                        )
                    },
                    onClick = {
                        scope
                            .launch { onDismissRequest() }
                            .invokeOnCompletion { onNavigateToRoute(Route.DONATE) }
                    },
                    selected = false,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.about)) },
                    icon = { Icon(Icons.Rounded.Info, null, tint = ThemedIconColors.primary) },
                    onClick = {
                        scope
                            .launch { onDismissRequest() }
                            .invokeOnCompletion { onNavigateToRoute(Route.ABOUT) }
                    },
                    selected = false,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
        Spacer(Modifier.weight(1f))
    }
}
