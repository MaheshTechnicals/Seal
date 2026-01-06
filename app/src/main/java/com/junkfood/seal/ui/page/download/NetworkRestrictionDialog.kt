package com.junkfood.seal.ui.page.download

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SignalCellularConnectedNoInternet4Bar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.SealDialog
import com.junkfood.seal.util.PreferenceUtil

@Composable
fun NetworkRestrictionDialog(
    onDismissRequest: () -> Unit = {},
) {
    val message = PreferenceUtil.getNetworkErrorMessage()
    
    SealDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.dismiss))
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.SignalCellularConnectedNoInternet4Bar,
                contentDescription = null,
            )
        },
        title = { Text(text = stringResource(id = R.string.network_unavailable)) },
        text = { Text(text = stringResource(id = message)) },
    )
}
