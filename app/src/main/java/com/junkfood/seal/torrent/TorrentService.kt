package com.junkfood.seal.torrent

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.junkfood.seal.util.NotificationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

private const val TAG = "TorrentService"

/**
 * Foreground [Service] that keeps the [TorrentEngine] alive in the background.
 *
 * Lifecycle
 * ─────────
 *  • **Started** by [TorrentEngine.addMagnet] / [TorrentEngine.addTorrentFile] via
 *    `startForegroundService` — it will not be started unless there is actual
 *    torrent work to do.
 *  • **Stopped** automatically by [TorrentEngine.considerStoppingService] once no
 *    torrent is actively downloading.  The user can also stop it from the
 *    persistent notification.
 *
 * The service observes [TorrentEngine.torrentsFlow] and keeps the foreground
 * notification updated with aggregate speed and active-download count.
 *
 * This is a **started service** (not bound); `onBind` returns `null`.
 */
class TorrentService : Service() {

    private val engine: TorrentEngine by inject()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var notificationJob: Job? = null

    // ─────────────────────────────────────────────────────────────────
    // Service lifecycle
    // ─────────────────────────────────────────────────────────────────

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")

        // Show the foreground notification immediately so Android doesn't ANR
        val initialNotification =
            NotificationUtil.makeTorrentServiceNotification(
                activeCount = 0,
                speedText = "Starting…",
            )
        startForeground(NotificationUtil.TORRENT_SERVICE_NOTIFICATION_ID, initialNotification)

        // Make sure the engine is running (no-op if already started)
        engine.start()

        // Subscribe to state updates and refresh the notification
        notificationJob?.cancel()
        notificationJob =
            serviceScope.launch {
                engine.torrentsFlow
                    .map { torrents ->
                        val active =
                            torrents.values.count {
                                it.state == TorrentState.DOWNLOADING ||
                                    it.state == TorrentState.DOWNLOADING_METADATA
                            }
                        val totalSpeed = torrents.values.sumOf { it.downloadSpeed }
                        Pair(active, formatSpeed(totalSpeed))
                    }
                    .distinctUntilChanged()
                    .collect { (active, speedText) ->
                        NotificationUtil.updateTorrentServiceNotification(
                            activeCount = active,
                            speedText = speedText,
                        )
                    }
            }

        // START_STICKY: if the OS kills this service, restart it automatically
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy — stopping engine")
        notificationJob?.cancel()
        serviceScope.cancel()
        engine.stop()
        super.onDestroy()
    }

    /** Not a bound service. */
    override fun onBind(intent: Intent?): IBinder? = null

    // ─────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────

    private fun formatSpeed(bytesPerSec: Long): String =
        when {
            bytesPerSec >= 1_048_576L ->
                String.format("%.1f MB/s", bytesPerSec / 1_048_576.0)
            bytesPerSec >= 1_024L ->
                String.format("%.1f KB/s", bytesPerSec / 1_024.0)
            bytesPerSec > 0 -> "$bytesPerSec B/s"
            else -> "Idle"
        }
}
