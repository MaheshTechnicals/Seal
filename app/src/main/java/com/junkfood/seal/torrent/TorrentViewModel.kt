package com.junkfood.seal.torrent

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Torrent Downloader screen.
 *
 * Responsibilities:
 *  • Expose [torrents] — a [StateFlow] of all active and completed torrents,
 *    kept alive for 5 seconds after the UI is gone (e.g. during config change).
 *  • Delegate all engine operations to [TorrentEngine] so the View layer stays
 *    free of any libtorrent4j imports.
 *  • Copy incoming `.torrent` content URIs to the app cache before handing them
 *    to the engine (Scoped Storage does not let us pass arbitrary URIs to native
 *    code directly).
 */
class TorrentViewModel(private val engine: TorrentEngine) : ViewModel() {

    val torrents: StateFlow<Map<String, TorrentDownloadState>> =
        engine.torrentsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyMap(),
        )

    // ─────────────────────────────────────────────────────────────────
    // Public commands — called from the UI layer
    // ─────────────────────────────────────────────────────────────────

    fun addMagnet(magnetLink: String) {
        if (magnetLink.isBlank()) return
        viewModelScope.launch { engine.addMagnet(magnetLink.trim()) }
    }

    /**
     * Copies the `.torrent` file from [uri] (a content:// URI from the file
     * picker) to a temporary file in the app cache, then passes it to the
     * engine so native libtorrent code can read it from a real filesystem path.
     */
    fun addTorrentUri(uri: android.net.Uri, context: Context) {
        viewModelScope.launch {
            // File copy is blocking I/O — must run on the IO dispatcher.
            withContext(Dispatchers.IO) {
                runCatching {
                    val tmp =
                        File(context.cacheDir, "incoming_${System.currentTimeMillis()}.torrent")
                    context.contentResolver.openInputStream(uri)?.use { ins ->
                        tmp.outputStream().use { out -> ins.copyTo(out) }
                    }
                    engine.addTorrentFile(tmp)
                }
            }
        }
    }

    fun pause(infoHash: String) = engine.pauseTorrent(infoHash)

    fun resume(infoHash: String) = engine.resumeTorrent(infoHash)

    /**
     * Removes the torrent from the session.
     * @param deleteFiles When `true`, downloaded files on disk are also deleted.
     */
    fun remove(infoHash: String, deleteFiles: Boolean = true) =
        engine.removeTorrent(infoHash, deleteFiles)
}
