package com.junkfood.seal.ui.common

import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.AnnotatedString
import com.junkfood.seal.R
import com.junkfood.seal.download.DownloaderV2
import com.junkfood.seal.download.Task
import com.junkfood.seal.ui.common.HapticFeedback.slightHapticFeedback
import com.junkfood.seal.ui.page.downloadv2.UiAction
import com.junkfood.seal.ui.page.home.RecentAction
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.getErrorReport
import com.junkfood.seal.util.makeToast

fun handleActiveTaskAction(
    action: UiAction,
    task: Task,
    downloader: DownloaderV2,
    context: Context,
    clipboardManager: ClipboardManager,
    uriHandler: UriHandler,
    onShowDetails: () -> Unit
) {
    when (action) {
        UiAction.Pause -> downloader.pause(task)
        UiAction.Cancel -> downloader.cancel(task)
        UiAction.Delete -> downloader.remove(task)
        UiAction.Resume -> downloader.resume(task)
        UiAction.Retry -> downloader.restart(task)
        UiAction.ShowDetails -> onShowDetails()

        is UiAction.CopyErrorReport -> {
            clipboardManager.setText(AnnotatedString(getErrorReport(action.throwable, task.url)))
            context.makeToast(R.string.error_copied)
        }
        is UiAction.CopyVideoURL -> {
            clipboardManager.setText(AnnotatedString(task.url))
            context.makeToast(R.string.link_copied)
        }
        is UiAction.OpenFile -> action.filePath?.let { path ->
            FileUtil.openFile(path) { context.makeToast(R.string.file_unavailable) }
        }
        is UiAction.OpenThumbnailURL -> uriHandler.openUri(action.url)
        is UiAction.OpenVideoURL -> uriHandler.openUri(action.url)
        is UiAction.ShareFile -> {
            FileUtil.createIntentForSharingFile(action.filePath)?.let { intent ->
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)))
            }
        }
    }
}


fun handleRecentDownloadAction(
    action: RecentAction,
    context: Context,
    view: android.view.View,
    clipboardManager: ClipboardManager,
    onShowDetails: () -> Unit
) {
    view.slightHapticFeedback()
    when (action) {
        is RecentAction.OpenFile -> {
            FileUtil.openFile(action.path) { context.makeToast(R.string.file_unavailable) }
        }
        is RecentAction.Share -> {
            FileUtil.createIntentForSharingFile(action.path)?.let { intent ->
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)))
            }
        }
        is RecentAction.CopyLink -> {
            clipboardManager.setText(AnnotatedString(action.url))
            context.makeToast(R.string.link_copied)
        }
        is RecentAction.ShowDetails -> onShowDetails()
    }
}
