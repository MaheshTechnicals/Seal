package com.junkfood.seal.ui.page.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.junkfood.seal.database.objects.DownloadedVideoInfo
import com.junkfood.seal.download.DownloaderV2
import com.junkfood.seal.download.Task

class HomeScreenState(
    private val context: Context,
    private val downloader: DownloaderV2,
    private val recentDownloads: List<DownloadedVideoInfo>,
    private val lifecycleRefreshTrigger: Int
) {

    val hasNotificationPermission: Boolean by derivedStateOf {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    val isBatteryOptimizationDisabled: Boolean by derivedStateOf {
        val pm = context.getSystemService(PowerManager::class.java)
        pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    val recentFiveDownloads: List<DownloadedVideoInfo> by derivedStateOf {
        recentDownloads
            .distinctBy { it.videoUrl + it.videoPath }
            .takeLast(5)
            .reversed()
    }


    val activeDownloads: List<Pair<Task, Task.State>> by derivedStateOf {
        val taskStateMap = downloader.getTaskStateMap()
        val recentIdentifiers = recentFiveDownloads.flatMap {
            listOf(it.videoUrl, it.videoPath, "${it.videoUrl}|${it.videoPath}")
        }.toSet()

        taskStateMap.toList().filter { (task, state) ->
            val downloadState = state.downloadState
            if (downloadState is Task.DownloadState.Completed) {
                val filePath = downloadState.filePath
                val taskUrl = task.url
                !recentIdentifiers.contains(taskUrl) &&
                        !recentIdentifiers.contains(filePath) &&
                        !recentIdentifiers.contains("$taskUrl|$filePath")
            } else true
        }
    }
}

@Composable
fun rememberHomeScreenState(
    context: Context,
    downloader: DownloaderV2,
    recentDownloads: List<DownloadedVideoInfo>,
    lifecycleRefreshTrigger: Int
) = remember(context, downloader, recentDownloads, lifecycleRefreshTrigger) {
    HomeScreenState(context, downloader, recentDownloads, lifecycleRefreshTrigger)
}