package com.junkfood.seal.torrent

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat
import java.io.File

/**
 * Handles everything related to the torrent save directory:
 *   `<ExternalStorage>/Download/SealPlus/Torrents`
 *
 * Strategy:
 *  - Android 10 (API 29) and below → WRITE_EXTERNAL_STORAGE + requestLegacyExternalStorage
 *    Both are already declared in AndroidManifest.xml; no extra work needed.
 *  - Android 11+ (API 30+) → MANAGE_EXTERNAL_STORAGE.
 *    The manifest already declares the permission.  At runtime we check
 *    [Environment.isExternalStorageManager]; if false we open the system settings
 *    page via [openManageStorageSettings].
 *
 * This combination lets us write to the **public** Download tree without any
 * MediaStore overhead, which is critical for large multi-file torrent downloads.
 */
object TorrentStorageUtil {

    /** Sub-path appended to the public Downloads directory. */
    private const val TORRENT_SUB_PATH = "SealPlus/Torrents"

    // ─────────────────────────────────────────────────────────────────
    // Directory helpers
    // ─────────────────────────────────────────────────────────────────

    /**
     * Returns (and creates if absent) the public torrent save directory.
     * Path: `<ExternalStorage>/Download/SealPlus/Torrents`
     */
    fun getTorrentDirectory(): File {
        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return File(root, TORRENT_SUB_PATH).also { dir ->
            if (!dir.exists()) dir.mkdirs()
        }
    }

    /**
     * Returns the absolute path string of the torrent save directory.
     * Convenience wrapper used by [TorrentEngine].
     */
    fun getTorrentSavePath(): String = getTorrentDirectory().absolutePath

    // ─────────────────────────────────────────────────────────────────
    // Permission helpers
    // ─────────────────────────────────────────────────────────────────

    /**
     * Returns `true` when the app has sufficient storage access to write torrent
     * files to the public Downloads directory.
     *
     * - API 30+: checks [Environment.isExternalStorageManager]
     * - API ≤ 29: checks [Manifest.permission.WRITE_EXTERNAL_STORAGE]
     */
    fun isStoragePermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Opens the system "All Files Access" settings page for this app so the user
     * can grant [Manifest.permission.MANAGE_EXTERNAL_STORAGE].
     *
     * Only applicable on API 30+; no-op on older APIs.
     */
    fun openManageStorageSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.parse("package:${context.packageName}"),
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }.onFailure {
                // Fallback: open the general manage-storage screen
                context.startActivity(
                    Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }

    /**
     * Ensures the torrent directory exists and is writable, returning `true` on
     * success.  Combines the permission check with the directory creation so
     * callers get a single-call gate.
     */
    fun ensureDirectoryReady(context: Context): Boolean {
        if (!isStoragePermissionGranted(context)) return false
        val dir = getTorrentDirectory()
        return dir.exists() || dir.mkdirs()
    }
}
