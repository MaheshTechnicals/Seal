package com.junkfood.seal.torrent

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.io.File
import java.util.Collections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.libtorrent4j.AlertListener
import org.libtorrent4j.SessionManager
import org.libtorrent4j.SessionParams
import org.libtorrent4j.SettingsPack
import org.libtorrent4j.Sha1Hash
import org.libtorrent4j.TorrentHandle
import org.libtorrent4j.TorrentInfo
import org.libtorrent4j.TorrentStatus
import org.libtorrent4j.alerts.Alert
import org.libtorrent4j.alerts.AlertType
import org.libtorrent4j.alerts.MetadataReceivedAlert
import org.libtorrent4j.alerts.StateChangedAlert
import org.libtorrent4j.alerts.TorrentAddedAlert
import org.libtorrent4j.alerts.TorrentErrorAlert
import org.libtorrent4j.alerts.TorrentFinishedAlert
import org.libtorrent4j.alerts.TorrentRemovedAlert
import org.libtorrent4j.swig.settings_pack as swig_settings

private const val TAG = "TorrentEngine"

/**
 * Singleton torrent engine that wraps a libtorrent4j [SessionManager].
 *
 * Lifecycle:
 *  • [start] is called lazily the first time a torrent is added; it starts the
 *    libtorrent session and the [TorrentService] foreground service.
 *  • [stop] is called by [TorrentService.onDestroy]; it tears down the session
 *    gracefully.
 *
 * Progress is broadcast through [torrentsFlow] — a [StateFlow] of
 * `Map<infoHashHex, TorrentDownloadState>`.  The UI collects this flow with
 * `collectAsStateWithLifecycle()` for zero-overhead reactive updates.
 *
 * Two update mechanisms keep the flow fresh:
 *  1. **Alert callbacks** — immediate state changes (added, finished, error, removed).
 *  2. **1 s polling loop** — smooth progress / speed / ETA updates every second.
 */
class TorrentEngine(private val context: Context) {

    // ─────────────────────────────────────────────────────────────────
    // Core session
    // ─────────────────────────────────────────────────────────────────

    private val session = SessionManager()
    private val engineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Internal handle registry. Keys are hex info-hash strings.
     * All reads/writes are guarded by [handlesLock].
     */
    private val handles = mutableMapOf<String, TorrentHandle>()
    private val handlesLock = Any()

    /**
     * Tracks info-hashes that the user has explicitly paused.
     * This is used in [buildState] to reliably report paused state
     * without depending on libtorrent4j's flags bitmask API, which
     * changed between minor versions.
     */
    private val pausedHashes: MutableSet<String> =
        Collections.synchronizedSet(mutableSetOf())

    // ─────────────────────────────────────────────────────────────────
    // Public reactive state
    // ─────────────────────────────────────────────────────────────────

    private val _torrentsFlow =
        MutableStateFlow<Map<String, TorrentDownloadState>>(emptyMap())

    /**
     * Live snapshot of every torrent tracked by this engine, keyed by
     * their hex info-hash.  Collect this in your ViewModel with
     * [StateFlow.collectAsStateWithLifecycle].
     */
    val torrentsFlow: StateFlow<Map<String, TorrentDownloadState>> =
        _torrentsFlow.asStateFlow()

    // ─────────────────────────────────────────────────────────────────
    // Alert listener
    // ─────────────────────────────────────────────────────────────────

    private val alertListener =
        object : AlertListener {
            /** null → subscribe to every alert type */
            override fun types(): IntArray? = null

            override fun alert(alert: Alert<*>) {
                try {
                    handleAlert(alert)
                } catch (e: Exception) {
                    Log.e(TAG, "Uncaught exception processing alert ${alert.type()}", e)
                }
            }
        }

    private fun handleAlert(alert: Alert<*>) {
        when (alert.type()) {
            // ── Torrent successfully added to the session ──────────────────
            AlertType.TORRENT_ADDED -> {
                val handle = (alert as TorrentAddedAlert).handle()
                if (!handle.isValid) return
                val hash = handle.infoHash().toHex()
                synchronized(handlesLock) { handles[hash] = handle }
                _torrentsFlow.update { it + (hash to buildState(hash, handle)) }
                Log.d(TAG, "Torrent added: $hash  name=${handle.torrentFile()?.name()}")
            }

            // ── Metadata received from peers (magnet-link flow) ────────────
            AlertType.METADATA_RECEIVED -> {
                val handle = (alert as MetadataReceivedAlert).handle()
                if (!handle.isValid) return
                val hash = handle.infoHash().toHex()
                _torrentsFlow.update { it + (hash to buildState(hash, handle)) }
                Log.d(TAG, "Metadata received: $hash  name=${handle.torrentFile()?.name()}")
            }

            // ── State machine transition ──────────────────────────────────
            AlertType.STATE_CHANGED -> {
                val handle = (alert as StateChangedAlert).handle()
                if (!handle.isValid) return
                val hash = handle.infoHash().toHex()
                _torrentsFlow.update { it + (hash to buildState(hash, handle)) }
            }

            // ── All pieces downloaded ─────────────────────────────────────
            AlertType.TORRENT_FINISHED -> {
                val handle = (alert as TorrentFinishedAlert).handle()
                val hash = handle.infoHash().toHex()
                _torrentsFlow.update { current ->
                    val existing = current[hash] ?: buildState(hash, handle)
                    current +
                        (hash to
                            existing.copy(
                                progress = 1f,
                                downloadSpeed = 0L,
                                uploadSpeed = 0L,
                                eta = 0L,
                                state = TorrentState.FINISHED,
                            ))
                }
                Log.d(TAG, "Torrent finished: $hash")
                considerStoppingService()
            }

            // ── Unrecoverable error ───────────────────────────────────────
            AlertType.TORRENT_ERROR -> {
                val a = alert as TorrentErrorAlert
                val handle = a.handle()
                val hash = handle.infoHash().toHex()
                val msg = a.error().message()
                Log.e(TAG, "Torrent error: $hash  $msg")
                _torrentsFlow.update { current ->
                    val existing = current[hash] ?: buildState(hash, handle)
                    current + (hash to existing.copy(state = TorrentState.ERROR, errorMessage = msg))
                }
                considerStoppingService()
            }

            // ── Torrent removed from session ──────────────────────────────
            // NOTE: In libtorrent4j 2.x, TorrentRemovedAlert's handle may no
            // longer be valid. We retrieve the hash via the swig object which
            // always remains accessible.
            AlertType.TORRENT_REMOVED -> {
                val a = alert as TorrentRemovedAlert
                val hash =
                    runCatching {
                            // Primary: wrap the swig sha1_hash in libtorrent4j's Sha1Hash so
                            // we use its well-tested toString() path rather than raw swig .to_hex()
                            Sha1Hash(a.swig().info_hashes().get_best()).toString()
                        }
                        .getOrElse {
                            // Fallback: try the legacy handle.infoHash() path
                            runCatching { a.handle().infoHash().toHex() }.getOrElse { return }
                        }
                synchronized(handlesLock) { handles.remove(hash) }
                pausedHashes.remove(hash)
                _torrentsFlow.update { it - hash }
                Log.d(TAG, "Torrent removed: $hash")
                considerStoppingService()
            }

            else -> { /* ignored */ }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Session lifecycle
    // ─────────────────────────────────────────────────────────────────

    /**
     * Starts the libtorrent session.  Idempotent — safe to call multiple times.
     *
     * The [SettingsPack] is configured for **maximum download performance**:
     *  • No bandwidth rate limits
     *  • DHT bootstrap with 4 well-known routers
     *  • LPD / LSD (Local Peer Discovery) enabled
     *  • PeX (Peer Exchange) is on by default in libtorrent; not disabled
     *  • All TCP + µTP transport combinations enabled
     *  • Generous connection / active-torrent limits
     */
    fun start() {
        if (session.isRunning) return
        Log.d(TAG, "Starting TorrentEngine session")
        session.addListener(alertListener)
        session.start(SessionParams(buildSettingsPack()))
        startProgressPoller()
    }

    /** Stops the session and clears the handle registry.  Called from [TorrentService]. */
    fun stop() {
        if (!session.isRunning) return
        Log.d(TAG, "Stopping TorrentEngine session")
        session.removeListener(alertListener)
        synchronized(handlesLock) { handles.clear() }
        pausedHashes.clear()
        session.stop()
    }

    val isRunning: Boolean
        get() = session.isRunning

    // ─────────────────────────────────────────────────────────────────
    // SettingsPack — maximum-performance configuration
    // ─────────────────────────────────────────────────────────────────

    private fun buildSettingsPack(): SettingsPack =
        SettingsPack().apply {

            // ── Bandwidth: no artificial caps ──────────────────────────────
            downloadRateLimit(0) // 0 = unlimited
            uploadRateLimit(0)

            // ── Connection / concurrency limits ────────────────────────────
            connectionsLimit(500) // global max open connections
            activeDownloads(20) // max simultaneously downloading torrents
            activeSeeds(20) // max simultaneously seeding torrents
            activeLimit(500) // total active torrents (dl + seed)
            activeDhtLimit(300) // DHT messages per second

            // ── DHT: enable + bootstrap nodes ──────────────────────────────
            setBoolean(swig_settings.bool_types.enable_dht.swigValue(), true)
            dhtBootstrapNodes(
                "router.bittorrent.com:6881," +
                    "router.utorrent.com:6881," +
                    "dht.transmissionbt.com:6881," +
                    "dht.aelitis.com:6881"
            )

            // ── LPD / LSD: Local Peer Discovery ────────────────────────────
            broadcastLsd(true)

            // ── Transport: every combination on, minimal encryption overhead ─
            setBoolean(
                swig_settings.bool_types.enable_outgoing_utp.swigValue(),
                true,
            )
            setBoolean(
                swig_settings.bool_types.enable_incoming_utp.swigValue(),
                true,
            )
            setBoolean(
                swig_settings.bool_types.enable_outgoing_tcp.swigValue(),
                true,
            )
            setBoolean(
                swig_settings.bool_types.enable_incoming_tcp.swigValue(),
                true,
            )
            // Disable RC4 overhead (not needed for public torrents)
            setBoolean(swig_settings.bool_types.prefer_rc4.swigValue(), false)

            // ── Request queue tuning ───────────────────────────────────────
            setInteger(
                swig_settings.int_types.request_timeout.swigValue(),
                20,
            ) // give slow peers up to 20 s
            setInteger(
                swig_settings.int_types.peer_connect_timeout.swigValue(),
                15,
            ) // timeout stalled connects after 15 s
            setInteger(
                swig_settings.int_types.max_allowed_in_request_queue.swigValue(),
                2000,
            )
            setInteger(
                swig_settings.int_types.max_out_request_queue.swigValue(),
                1500,
            )
            setInteger(
                swig_settings.int_types.whole_pieces_threshold.swigValue(),
                20,
            ) // download whole pieces when < 20 remain
        }

    // ─────────────────────────────────────────────────────────────────
    // Torrent operations
    // ─────────────────────────────────────────────────────────────────

    /**
     * Starts downloading from a magnet link.
     * The engine is started automatically if not yet running.
     * Content is saved to [TorrentStorageUtil.getTorrentSavePath].
     */
    fun addMagnet(magnetLink: String) {
        ensureStarted()
        session.download(magnetLink, File(TorrentStorageUtil.getTorrentSavePath()))
    }

    /**
     * Starts downloading from a `.torrent` file.
     * The engine is started automatically if not yet running.
     * Content is saved to [TorrentStorageUtil.getTorrentSavePath].
     */
    fun addTorrentFile(torrentFile: File) {
        ensureStarted()
        val ti = TorrentInfo(torrentFile)
        session.download(ti, File(TorrentStorageUtil.getTorrentSavePath()))
    }

    /** Pauses a torrent by its info-hash hex string. */
    fun pauseTorrent(infoHash: String) {
        val handle = validHandle(infoHash) ?: return
        handle.pause()
        pausedHashes.add(infoHash)
        _torrentsFlow.update { current ->
            val existing = current[infoHash] ?: return
            current + (infoHash to existing.copy(state = TorrentState.PAUSED))
        }
    }

    /** Resumes a paused torrent. */
    fun resumeTorrent(infoHash: String) {
        val handle = validHandle(infoHash) ?: return
        handle.resume()
        pausedHashes.remove(infoHash)
        _torrentsFlow.update { current ->
            val existing = current[infoHash] ?: return
            current + (infoHash to existing.copy(state = TorrentState.DOWNLOADING))
        }
    }

    /**
     * Removes a torrent from the session.
     *
     * @param deleteFiles When `true` the downloaded files on disk are also deleted.
     */
    fun removeTorrent(infoHash: String, deleteFiles: Boolean = false) {
        val handle = validHandle(infoHash)
        if (handle == null) {
            // Guard: handle already removed (e.g. via alert), just sync the state map
            synchronized(handlesLock) { handles.remove(infoHash) }
            pausedHashes.remove(infoHash)
            _torrentsFlow.update { it - infoHash }
            return
        }
        if (deleteFiles) {
            // 1 = libtorrent's delete_files flag
            session.swig().remove_torrent(handle.swig(), 1)
        } else {
            session.remove(handle)
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────────────────────────────

    /**
     * Builds the current [TorrentDownloadState] by reading live status from the
     * libtorrent handle.  Must *not* be called on UI thread; libtorrent reads are
     * cheap but blocking.
     */
    private fun buildState(hash: String, handle: TorrentHandle): TorrentDownloadState {
        val status = handle.status()
        val ti = handle.torrentFile()
        val name =
            ti?.name()?.takeIf { it.isNotBlank() }
                ?: status.name().takeIf { it.isNotBlank() }
                ?: hash.take(8)

        val dlSpeed = status.downloadPayloadRate().toLong()
        val totalWanted = status.totalWanted()
        val totalDone = status.totalWantedDone()
        val eta =
            if (dlSpeed > 0 && totalWanted > totalDone) {
                (totalWanted - totalDone) / dlSpeed
            } else {
                -1L
            }

        // If the user explicitly paused this torrent, honour that state regardless
        // of what libtorrent currently reports (avoids flags bitmask API uncertainty).
        val resolvedState =
            if (pausedHashes.contains(hash)) TorrentState.PAUSED
            else status.state().toTorrentState()

        return TorrentDownloadState(
            infoHash = hash,
            name = name,
            progress = status.progress(),
            downloadSpeed = dlSpeed,
            uploadSpeed = status.uploadPayloadRate().toLong(),
            eta = eta,
            state = resolvedState,
            totalSize = totalWanted,
            downloaded = totalDone,
            peers = status.numPeers(),
            seeds = status.numSeeds(),
            savePath = handle.savePath(),
        )
    }

    /**
     * Background coroutine that polls every active handle once per second.
     * This ensures the UI receives smooth speed / ETA / progress numbers even
     * between sparse alert callbacks.
     */
    private fun startProgressPoller() {
        engineScope.launch {
            while (isActive && session.isRunning) {
                delay(1_000L)
                val snapshot: Map<String, TorrentHandle>
                synchronized(handlesLock) { snapshot = handles.toMap() }
                if (snapshot.isEmpty()) continue

                _torrentsFlow.update { current ->
                    val updated = current.toMutableMap()
                    snapshot.forEach { (hash, handle) ->
                        if (handle.isValid) {
                            try {
                                updated[hash] = buildState(hash, handle)
                            } catch (e: Exception) {
                                Log.w(TAG, "Poll error for $hash", e)
                            }
                        }
                    }
                    updated
                }
            }
        }
    }

    /** Returns a valid [TorrentHandle] for [infoHash], or `null` if not found / invalid. */
    private fun validHandle(infoHash: String): TorrentHandle? {
        return synchronized(handlesLock) { handles[infoHash] }?.takeIf { it.isValid }
    }

    /** Ensures the engine is running, then starts the [TorrentService]. */
    private fun ensureStarted() {
        if (!session.isRunning) start()
        startTorrentService()
    }

    private fun startTorrentService() {
        val intent = Intent(context, TorrentService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /**
     * Stops the [TorrentService] if there are no actively downloading or
     * metadata-fetching torrents left.  Seeding / finished torrents do not
     * keep the service alive.
     */
    private fun considerStoppingService() {
        val hasActive =
            _torrentsFlow.value.values.any {
                it.state == TorrentState.DOWNLOADING ||
                    it.state == TorrentState.DOWNLOADING_METADATA ||
                    it.state == TorrentState.CHECKING_FILES
            }
        if (!hasActive) {
            context.stopService(Intent(context, TorrentService::class.java))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Extension: map libtorrent's internal state enum → our UI enum
// ─────────────────────────────────────────────────────────────────────────────

private fun TorrentStatus.State.toTorrentState(): TorrentState =
    when (this) {
        TorrentStatus.State.CHECKING_FILES -> TorrentState.CHECKING_FILES
        TorrentStatus.State.DOWNLOADING_METADATA -> TorrentState.DOWNLOADING_METADATA
        TorrentStatus.State.DOWNLOADING -> TorrentState.DOWNLOADING
        TorrentStatus.State.FINISHED -> TorrentState.FINISHED
        TorrentStatus.State.SEEDING -> TorrentState.SEEDING
        TorrentStatus.State.ALLOCATING -> TorrentState.CHECKING_FILES
        TorrentStatus.State.CHECKING_RESUME_DATA -> TorrentState.CHECKING_FILES
        else -> TorrentState.UNKNOWN
    }

// ─────────────────────────────────────────────────────────────────────────────
// Extension: convert Sha1Hash → hex string (libtorrent4j 2.x)
// ─────────────────────────────────────────────────────────────────────────────

/** Returns the 40-char lower-case hex representation of this [Sha1Hash]. */
private fun Sha1Hash.toHex(): String = this.toString()
