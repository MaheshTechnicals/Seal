package com.junkfood.seal.ui.page.torrent

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.R
import com.junkfood.seal.util.TorrentProgress
import com.junkfood.seal.util.TorrentUtil
import com.junkfood.seal.util.makeToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "TorrentDownloadViewModel"

class TorrentDownloadViewModel : ViewModel() {

    private val _viewStateFlow = MutableStateFlow(ViewState())
    val viewStateFlow = _viewStateFlow.asStateFlow()

    private val torrentJobs = mutableMapOf<String, Job>()

    data class ViewState(
        val activeTorrents: List<TorrentProgress> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    /**
     * Add a new torrent download
     */
    fun addTorrent(torrentUrl: String) {
        if (!TorrentUtil.isTorrentSupportEnabled()) {
            context.makeToast("Torrent support is disabled. Enable it in SealPlus Extras.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _viewStateFlow.update { it.copy(isLoading = true) }

            try {
                // Generate unique ID for this torrent
                val torrentId = System.currentTimeMillis().toString()
                val downloadDir = TorrentUtil.getTorrentDownloadDir()

                // Handle torrent URL (download .torrent file if needed)
                val torrentSource = if (TorrentUtil.isMagnetLink(torrentUrl)) {
                    Log.d(TAG, "Processing magnet link")
                    torrentUrl
                } else if (TorrentUtil.isTorrentUrl(torrentUrl)) {
                    Log.d(TAG, "Downloading .torrent file")
                    val torrentFile = TorrentUtil.downloadTorrentFile(torrentUrl).getOrThrow()
                    torrentFile.absolutePath
                } else {
                    throw IllegalArgumentException("Invalid torrent URL or magnet link")
                }

                // Start the download and monitor progress
                val job = viewModelScope.launch(Dispatchers.IO) {
                    // Monitor progress
                    TorrentUtil.monitorTorrentProgress(torrentId, downloadDir)
                        .catch { error ->
                            Log.e(TAG, "Progress monitoring error", error)
                            removeTorrentFromList(torrentId)
                            context.makeToast("Torrent download failed: ${error.message}")
                        }
                        .collect { progress ->
                            updateTorrentProgress(torrentId, progress)
                            
                            // If complete, show notification
                            if (progress.progress >= 100f) {
                                context.makeToast("Torrent download completed!")
                                torrentJobs.remove(torrentId)
                            }
                        }
                }

                torrentJobs[torrentId] = job

                // Start actual download in background
                viewModelScope.launch(Dispatchers.IO) {
                    TorrentUtil.startTorrentDownload(
                        torrentSource = torrentSource,
                        downloadDir = downloadDir
                    ) { progress ->
                        updateTorrentProgress(torrentId, progress)
                    }.onSuccess { files ->
                        Log.d(TAG, "Torrent download completed: $files")
                        context.makeToast("Downloaded: ${files.size} file(s)")
                    }.onFailure { error ->
                        Log.e(TAG, "Torrent download failed", error)
                        removeTorrentFromList(torrentId)
                        context.makeToast("Download failed: ${error.message}")
                        torrentJobs[torrentId]?.cancel()
                        torrentJobs.remove(torrentId)
                    }
                }

                // Add initial progress entry
                val initialProgress = TorrentProgress(
                    torrentId = torrentId,
                    progress = 0f,
                    downloadSpeed = 0.0,
                    uploadSpeed = 0.0,
                    totalSize = 0L,
                    downloadedSize = 0L,
                    numSeeders = 0,
                    numPeers = 0,
                    files = listOf(
                        TorrentUtil.parseMagnetLink(torrentUrl)?.displayName 
                            ?: "Connecting to peers..."
                    )
                )
                
                _viewStateFlow.update { state ->
                    state.copy(
                        activeTorrents = state.activeTorrents + initialProgress,
                        isLoading = false
                    )
                }

                context.makeToast("Torrent added")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to add torrent", e)
                context.makeToast("Failed to add torrent: ${e.message}")
                _viewStateFlow.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * Cancel an active torrent download
     */
    fun cancelTorrent(torrentId: String) {
        torrentJobs[torrentId]?.cancel()
        torrentJobs.remove(torrentId)
        removeTorrentFromList(torrentId)
        context.makeToast("Torrent cancelled")
    }

    /**
     * Remove completed torrent from list
     */
    fun removeTorrent(torrentId: String) {
        removeTorrentFromList(torrentId)
    }

    private fun updateTorrentProgress(torrentId: String, progress: TorrentProgress) {
        _viewStateFlow.update { state ->
            val updatedTorrents = state.activeTorrents.map { torrent ->
                if (torrent.torrentId == torrentId) progress else torrent
            }
            state.copy(activeTorrents = updatedTorrents)
        }
    }

    private fun removeTorrentFromList(torrentId: String) {
        _viewStateFlow.update { state ->
            state.copy(
                activeTorrents = state.activeTorrents.filter { it.torrentId != torrentId }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Cancel all active downloads
        torrentJobs.values.forEach { it.cancel() }
        torrentJobs.clear()
        
        // Cleanup temp files
        TorrentUtil.cleanupTorrentCache()
    }
}
