package com.junkfood.seal

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.torrent.TorrentEngine
import com.junkfood.seal.torrent.TorrentStorageUtil
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.junkfood.seal.ui.common.SettingsProvider
import com.junkfood.seal.ui.common.ThemedToastHost
import com.junkfood.seal.ui.page.AppEntry
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel
import com.junkfood.seal.ui.page.onboarding.OnboardingScreen
import com.junkfood.seal.ui.page.security.LockScreen
import com.junkfood.seal.ui.page.splash.SplashScreen
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.AuthenticationManager
import com.junkfood.seal.util.ONBOARDING_COMPLETED
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.PreferenceUtil.updateBoolean
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
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.compose.KoinContext

class MainActivity : AppCompatActivity() {
    private val dialogViewModel: DownloadDialogViewModel by viewModel()
    private val torrentEngine: TorrentEngine by inject()
    private val activityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isAppInBackground = false

    /**
     * Reactive destination requested by the torrent notification's `navigate_to` extra.
     * Read by [AppEntry] via a `LaunchedEffect` and reset after consumption.
     */
    private val _navigateTo = mutableStateOf<String?>(null)

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT < 33) {
            runBlocking { setLanguage(PreferenceUtil.getLocaleFromPreference()) }
        }
        enableEdgeToEdge()

        context = this.baseContext

        // Torrent intents (magnet: / .torrent) must be checked FIRST.
        // getSharedURL() returns dataString for ACTION_VIEW, which would include
        // magnet URIs — routing them to yt-dlp before the torrent engine sees them.
        val torrentInput = intent.getTorrentInput()
        if (torrentInput != null) {
            torrentInput.dispatch(torrentEngine, this, activityScope)
        } else {
            // Check for navigate_to deep-link (torrent notification tap)
            intent.getStringExtra("navigate_to")?.let { _navigateTo.value = it }

            // Not a torrent intent — fall through to normal yt-dlp URL handling
            intent.getSharedURL()?.let { url ->
                dialogViewModel.setSharedUrl(url)
            }
        }
        
        setContent {
            KoinContext {
                val windowSizeClass = calculateWindowSizeClass(this)
                var showSplash by remember { mutableStateOf(true) }
                var showOnboarding by remember { mutableStateOf(!ONBOARDING_COMPLETED.getBoolean()) }
                var isLocked by remember { 
                    mutableStateOf(
                        AuthenticationManager.isSecurityEnabled() && 
                        AuthenticationManager.isAuthenticationNeeded()
                    )
                }
                
                SettingsProvider(windowWidthSizeClass = windowSizeClass.widthSizeClass) {
                    SealTheme(
                        darkTheme = LocalDarkTheme.current.isDarkTheme(),
                        isHighContrastModeEnabled = LocalDarkTheme.current.isHighContrastModeEnabled,
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            when {
                                showSplash -> {
                                    SplashScreen(
                                        onSplashFinished = {
                                            showSplash = false
                                        }
                                    )
                                }
                                showOnboarding -> {
                                    OnboardingScreen(
                                        onFinish = {
                                            ONBOARDING_COMPLETED.updateBoolean(true)
                                            showOnboarding = false
                                        }
                                    )
                                }
                                else -> {
                                    AppEntry(
                                        dialogViewModel = dialogViewModel,
                                        navigateTo = _navigateTo,
                                    )
                                    
                                    // Show lock screen overlay if locked
                                    if (isLocked) {
                                        LockScreen(
                                            onUnlocked = {
                                                isLocked = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Themed toast overlay – always on top
                            ThemedToastHost()
                        }
                    }
                }
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        isAppInBackground = true
    }
    
    override fun onResume() {
        super.onResume()
        if (isAppInBackground && AuthenticationManager.isSecurityEnabled() && 
            AuthenticationManager.isAuthenticationNeeded()) {
            // Trigger re-authentication by recreating activity
            recreate()
        }
        isAppInBackground = false
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        // navigate_to deep-link (torrent notification tap while app is open)
        intent.getStringExtra("navigate_to")?.let { _navigateTo.value = it }

        // Torrent check FIRST — getSharedURL() returns dataString for ACTION_VIEW,
        // which would incorrectly capture magnet: URIs as yt-dlp download targets.
        val torrentInput = intent.getTorrentInput()
        if (torrentInput != null) {
            torrentInput.dispatch(torrentEngine, this, activityScope)
            return
        }
        intent.getSharedURL()?.let { url ->
            dialogViewModel.setSharedUrl(url)
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Torrent intent parsing
    // ─────────────────────────────────────────────────────────────────

    /** Sealed result of parsing an incoming [Intent] for torrent content. */
    private sealed interface TorrentInput {
        data class Magnet(val uri: String) : TorrentInput
        data class TorrentFile(val uri: android.net.Uri) : TorrentInput

        /**
         * Sends the input to [TorrentEngine] after verifying storage permission.
         * File I/O is dispatched to [Dispatchers.IO] via [scope].
         */
        fun dispatch(
            engine: TorrentEngine,
            context: android.content.Context,
            scope: CoroutineScope,
        ) {
            if (!TorrentStorageUtil.isStoragePermissionGranted(context)) {
                TorrentStorageUtil.openManageStorageSettings(context)
                return
            }
            when (this) {
                is Magnet -> engine.addMagnet(uri)
                is TorrentFile -> {
                    // Copy the .torrent file off the main thread, then hand it to the engine.
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            runCatching {
                                val tmp = File(
                                    context.cacheDir,
                                    "incoming_${System.currentTimeMillis()}.torrent",
                                )
                                context.contentResolver.openInputStream(uri)?.use { ins ->
                                    tmp.outputStream().use { out -> ins.copyTo(out) }
                                }
                                engine.addTorrentFile(tmp)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Parses the [Intent] looking for a magnet URI (`magnet:` scheme) or a
     * `.torrent` file (`application/x-bittorrent` MIME type).
     * Returns `null` if no torrent content is found so the normal URL path
     * can continue.
     */
    private fun Intent.getTorrentInput(): TorrentInput? {
        if (action == Intent.ACTION_VIEW) {
            val uri = dataString ?: return null
            if (uri.startsWith("magnet:")) return TorrentInput.Magnet(uri)
            if (type == "application/x-bittorrent") return TorrentInput.TorrentFile(data ?: return null)
        }
        if (action == Intent.ACTION_SEND && type == "text/plain") {
            val text = getStringExtra(Intent.EXTRA_TEXT) ?: return null
            if (text.trimStart().startsWith("magnet:")) return TorrentInput.Magnet(text.trim())
        }
        return null
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
                    matchUrlFromSharedText(sharedContent).also { matchedUrl ->
                        if (sharedUrlCached != matchedUrl) {
                            sharedUrlCached = matchedUrl
                        }
                    }
                }
            }

            else -> {
                null
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private var sharedUrlCached = ""
    }
}
