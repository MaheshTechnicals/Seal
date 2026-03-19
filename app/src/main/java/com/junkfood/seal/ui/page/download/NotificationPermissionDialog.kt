package com.junkfood.seal.ui.page.download

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.junkfood.seal.R

@Preview
@Composable
fun NotificationPermissionDialog(
    onDismissRequest: () -> Unit = {},
    onConfirm: (Intent) -> Unit = {},
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                imageVector = Icons.Outlined.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.notification_permission_required),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = stringResource(id = R.string.notification_permission_desc))
        },
        confirmButton = {
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        onConfirm(intent)
                    }
                }
            ) {
                Text(text = stringResource(id = R.string.grant_permission))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.skip))
            }
        },
    )
}