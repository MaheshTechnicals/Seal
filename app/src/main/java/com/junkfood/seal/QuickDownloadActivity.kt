package com.junkfood.seal

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.junkfood.seal.ui.common.SettingsProvider
import com.junkfood.seal.ui.common.ThemedToastHost
import com.junkfood.seal.ui.page.downloadv2.configure.Config
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialog
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel.Action
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel.SelectionState
import com.junkfood.seal.ui.page.downloadv2.configure.FormatPage
import com.junkfood.seal.ui.page.downloadv2.configure.PlaylistSelectionPage
import com.junkfood.seal.torrent.TorrentEngine
import com.junkfood.seal.torrent.TorrentStorageUtil
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.matchUrlFromSharedText
import com.junkfood.seal.util.setLanguage
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel

private const val TAG = "QuickDownloadActivity"

class QuickDownloadActivity : ComponentActivity() {
    private var sharedUrlCached: String = ""
    private val torrentEngine: TorrentEngine by inject()
    private val activityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * Checks whether [intent] is a torrent-related intent (magnet URI or .torrent file).
     * If so, dispatches it to [TorrentEngine] and returns `true` so the caller can
     * `finish()` immediately — no yt-dlp dialog needed.
     *
     * When storage permission is missing the user is sent to the system settings
     * page and `true` is still returned so the magnet/torrent link doesn't
     * accidentally fall through to yt-dlp.
     */
    private fun handleTorrentIntent(intent: Intent): Boolean {
        val isTorrent = when {
            intent.action == Intent.ACTION_VIEW &&
                intent.dataString?.startsWith("magnet:") == true -> true
            intent.action == Intent.ACTION_VIEW &&
                intent.type == "application/x-bittorrent" &&
                intent.data != null -> true
            intent.action == Intent.ACTION_SEND &&
                intent.type == "text/plain" &&
                intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()
                    ?.startsWith("magnet:") == true -> true
            else -> false
        }
        if (!isTorrent) return false

        // Gate on storage permission — prompt but don't fall through to yt-dlp
        if (!TorrentStorageUtil.isStoragePermissionGranted(this)) {
            TorrentStorageUtil.openManageStorageSettings(this)
            return true
        }

        return when {
            // magnet: URI opened directly
            intent.action == Intent.ACTION_VIEW &&
                intent.dataString?.startsWith("magnet:") == true -> {
                torrentEngine.addMagnet(intent.dataString!!)
                true
            }
            // .torrent file opened via file manager / browser
            intent.action == Intent.ACTION_VIEW &&
                intent.type == "application/x-bittorrent" &&
                intent.data != null -> {
                // File I/O must NOT run on the main thread
                val data = intent.data!!
                activityScope.launch {
                    withContext(Dispatchers.IO) {
                        runCatching {
                            val tmp = File(cacheDir, "incoming_${System.currentTimeMillis()}.torrent")
                            contentResolver.openInputStream(data)?.use { ins ->
                                tmp.outputStream().use { out -> ins.copyTo(out) }
                            }
                            torrentEngine.addTorrentFile(tmp)
                        }
                    }
                }
                true
            }
            // magnet: link shared as text (e.g. from browser share sheet)
            intent.action == Intent.ACTION_SEND &&
                intent.type == "text/plain" -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim() ?: return false
                if (text.startsWith("magnet:")) {
                    torrentEngine.addMagnet(text)
                    true
                } else {
                    false
                }
            }
            else -> false
        }
    }

    private fun Intent.getSharedURL(): String? {
        val intent = this

        return when (intent.action) {
            Intent.ACTION_VIEW -> {
                intent.dataString
            }

            Intent.ACTION_SEND -> {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedContent ->
                    intent.removeExtra(Intent.EXTRA_TEXT)
                    matchUrlFromSharedText(sharedContent)
                }
            }

            else -> {
                null
            }
        }
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── Torrent intercept: magnet link or .torrent file ──────────
        // These must NOT be passed to yt-dlp (DownloadDialogViewModel).
        // Instead hand them to TorrentEngine and close this activity.
        if (handleTorrentIntent(intent)) {
            finish()
            return
        }

        intent.getSharedURL()?.let { sharedUrlCached = it }

        if (sharedUrlCached.isEmpty()) {
            finish()
        }

        App.startService()

        enableEdgeToEdge()

        window.run {
            setBackgroundDrawable(ColorDrawable(0))
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
            } else {
                setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
            }
        }

        if (Build.VERSION.SDK_INT < 33) {
            runBlocking { setLanguage(PreferenceUtil.getLocaleFromPreference()) }
        }

        val viewModel: DownloadDialogViewModel = getViewModel()
        viewModel.postAction(Action.ShowSheet(listOf(sharedUrlCached)))

        setContent {
            SettingsProvider(calculateWindowSizeClass(this).widthSizeClass) {
                SealTheme(
                    darkTheme = LocalDarkTheme.current.isDarkTheme(),
                    isHighContrastModeEnabled = LocalDarkTheme.current.isHighContrastModeEnabled,
                ) {
                    var preferences by remember {
                        mutableStateOf(DownloadUtil.DownloadPreferences.createFromPreferences())
                    }

                    val sheetValue = viewModel.sheetValueFlow.collectAsStateWithLifecycle().value

                    val state = viewModel.sheetStateFlow.collectAsStateWithLifecycle().value

                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                    val selectionState =
                        viewModel.selectionStateFlow.collectAsStateWithLifecycle().value

                    var showDialog by remember { mutableStateOf(false) }

                    LaunchedEffect(sheetValue, selectionState) {
                        if (sheetValue == DownloadDialogViewModel.SheetValue.Expanded) {
                            showDialog = true
                        } else if (sheetValue == DownloadDialogViewModel.SheetValue.Hidden) {
                            launch { sheetState.hide() }
                                .invokeOnCompletion {
                                    showDialog = false
                                    if (selectionState == SelectionState.Idle) {
                                        this@QuickDownloadActivity.finish()
                                    }
                                }
                        }
                    }

                    if (showDialog) {
                        DownloadDialog(
                            state = state,
                            sheetState = sheetState,
                            config = Config(),
                            preferences = preferences,
                            onPreferencesUpdate = { preferences = it },
                            onActionPost = { viewModel.postAction(it) },
                        )
                    }

                    when (selectionState) {
                        is SelectionState.FormatSelection ->
                            FormatPage(
                                state = selectionState,
                                onDismissRequest = {
                                    viewModel.postAction(Action.Reset)
                                    this.finish()
                                },
                            )

                        SelectionState.Idle -> {}
                        is SelectionState.PlaylistSelection -> {
                            PlaylistSelectionPage(
                                state = selectionState,
                                onDismissRequest = {
                                    viewModel.postAction(Action.Reset)
                                    this.finish()
                                },
                            )
                        }
                    }

                    // Themed toast overlay for QuickDownloadActivity
                    ThemedToastHost()
                }
            }
        }
    }
}
