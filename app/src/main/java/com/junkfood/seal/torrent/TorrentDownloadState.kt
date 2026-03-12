package com.junkfood.seal.torrent

/**
 * The lifecycle state of a single torrent download, mapped from libtorrent4j's internal
 * [org.libtorrent4j.TorrentStatus.State].
 */
enum class TorrentState {
    /** Initial state before libtorrent has assigned a real state. */
    UNKNOWN,

    /** Verifying existing pieces on disk (pre-download check or resume). */
    CHECKING_FILES,

    /** Fetching the .torrent metadata from swarm peers (magnet-link flow). */
    DOWNLOADING_METADATA,

    /** Actively downloading payload data. */
    DOWNLOADING,

    /** All requested pieces downloaded; not yet seeding. */
    FINISHED,

    /** Actively uploading to peers (fully seeded). */
    SEEDING,

    /** User-paused or manually suspended. */
    PAUSED,

    /** A fatal error occurred; see [TorrentDownloadState.errorMessage]. */
    ERROR,
}

/**
 * Immutable snapshot of a torrent's live state, broadcast by [TorrentEngine] every second
 * via its [TorrentEngine.torrentsFlow].
 *
 * @param infoHash       Hex-encoded SHA-1 info-hash (40 chars); used as the unique map key.
 * @param name           Human-readable torrent name (may be the short hash while metadata loads).
 * @param progress       Download progress in the range [0.0, 1.0].
 * @param downloadSpeed  Payload download speed in **bytes/second**.
 * @param uploadSpeed    Payload upload   speed in **bytes/second**.
 * @param eta            Estimated seconds until download completes; -1 when unknown/unlimited.
 * @param state          Current [TorrentState] driven by libtorrent alerts + polling.
 * @param totalSize      Total bytes to download (-1 while metadata is not yet received).
 * @param downloaded     Bytes of payload already written to disk.
 * @param peers          Number of connected peers (seeders + leechers).
 * @param seeds          Number of connected peers that have the complete file.
 * @param savePath       Absolute path to the directory where content is being saved.
 * @param errorMessage   Non-null only when [state] == [TorrentState.ERROR].
 */
data class TorrentDownloadState(
    val infoHash: String,
    val name: String,
    val progress: Float = 0f,
    val downloadSpeed: Long = 0L,
    val uploadSpeed: Long = 0L,
    val eta: Long = -1L,
    val state: TorrentState = TorrentState.UNKNOWN,
    val totalSize: Long = -1L,
    val downloaded: Long = 0L,
    val peers: Int = 0,
    val seeds: Int = 0,
    val savePath: String = "",
    val errorMessage: String? = null,
)
