package com.junkfood.seal.ui.page.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.download.DownloaderV2
import com.junkfood.seal.ui.alerts.DownloadDetailsDialog
import com.junkfood.seal.ui.alerts.RecentDownloadDetailsDialog
import com.junkfood.seal.ui.common.HapticFeedback.slightHapticFeedback
import com.junkfood.seal.ui.common.handleActiveTaskAction
import com.junkfood.seal.ui.common.handleRecentDownloadAction
import com.junkfood.seal.ui.component.ActiveDownloadCard
import com.junkfood.seal.ui.component.URLInputField
import com.junkfood.seal.ui.component.card.RecentDownloadCard
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel
import com.junkfood.seal.util.makeToast
import com.junkfood.seal.util.matchUrlFromClipboard

@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    urlText: String,
    onUrlChange: (String) -> Unit,
    screenState: HomeScreenState,
    downloader: DownloaderV2,
    dialogViewModel: DownloadDialogViewModel,
    clipboardManager: ClipboardManager,
    keyboardController: SoftwareKeyboardController?,
    uriHandler: UriHandler,
    view: android.view.View
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HomeHeader()
        }

        item {
            URLInputField(
                value = urlText,
                onValueChange = onUrlChange,
                onDownloadClick = {
                    if (urlText.isNotBlank()) {
                        view.slightHapticFeedback()
                        dialogViewModel.postAction(
                            DownloadDialogViewModel.Action.ShowSheet(
                                listOf(
                                    urlText
                                )
                            )
                        )
                        onUrlChange("")
                        keyboardController?.hide()
                    } else {
                        context.makeToast(R.string.url_empty)
                    }
                },
                onPasteClick = {
                    clipboardManager.getText()?.text?.let { clipText ->
                        onUrlChange(context.matchUrlFromClipboard(clipText))
                        context.makeToast(R.string.paste_msg)
                    }
                }
            )
        }

        if (screenState.activeDownloads.isNotEmpty() || screenState.recentFiveDownloads.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.recent_downloads),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }


        items(
            items = screenState.activeDownloads,
            key = { (task, _) -> "active_${task.id}" }
        ) { (task, state) ->
            var showDetailsDialog by remember { mutableStateOf(false) }

            ActiveDownloadCard(
                task = task,
                state = state,
                onAction = { action ->
                    view.slightHapticFeedback()
                    handleActiveTaskAction(
                        action = action,
                        task = task,
                        downloader = downloader,
                        context = context,
                        clipboardManager = clipboardManager,
                        uriHandler = uriHandler,
                        onShowDetails = { showDetailsDialog = true }
                    )
                }
            )

            if (showDetailsDialog) {
                DownloadDetailsDialog(
                    task = task,
                    state = state,
                    onDismiss = { showDetailsDialog = false }
                )
            }
        }

        items(
            items = screenState.recentFiveDownloads,
            key = { "recent_${it.id}" }
        ) { downloadInfo ->
            var showRecentDetailsDialog by remember { mutableStateOf(false) }

            RecentDownloadCard(
                downloadInfo = downloadInfo,
                onClick = {
                    handleRecentDownloadAction(RecentAction.OpenFile(downloadInfo.videoPath), context, view, clipboardManager) {}
                },
                onShare = {
                    handleRecentDownloadAction(RecentAction.Share(downloadInfo.videoPath), context, view, clipboardManager) {}
                },
                onCopyLink = {
                    handleRecentDownloadAction(RecentAction.CopyLink(downloadInfo.videoUrl), context, view, clipboardManager) {}
                },
                onShowDetails = {
                    handleRecentDownloadAction(RecentAction.ShowDetails, context, view, clipboardManager) {
                        showRecentDetailsDialog = true
                    }
                }
            )

            if (showRecentDetailsDialog) {
                RecentDownloadDetailsDialog(
                    downloadInfo = downloadInfo,
                    onDismiss = { showRecentDetailsDialog = false }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}





