package com.junkfood.seal.ui.page.home

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.seal.R
import com.junkfood.seal.download.DownloaderV2
import com.junkfood.seal.ui.alerts.BatteryOptimizationDialog
import com.junkfood.seal.ui.alerts.ExitConfirmationDialog
import com.junkfood.seal.ui.page.download.NotificationPermissionDialog
import com.junkfood.seal.ui.page.downloadv2.configure.Config
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialog
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel.Action
import com.junkfood.seal.ui.page.downloadv2.configure.FormatPage
import com.junkfood.seal.ui.page.downloadv2.configure.PlaylistSelectionPage
import com.junkfood.seal.util.DatabaseUtil
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.PreferenceUtil.getInt
import com.junkfood.seal.util.PreferenceUtil.getLong
import com.junkfood.seal.util.PreferenceUtil.updateLong
import com.junkfood.seal.util.SPONSOR_DIALOG_FREQUENCY
import com.junkfood.seal.util.SPONSOR_DIALOG_LAST_SHOWN
import com.junkfood.seal.util.SPONSOR_FREQ_OFF
import com.junkfood.seal.util.SPONSOR_FREQ_WEEKLY
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@SuppressLint("BatteryLife")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewHomePage(
    modifier: Modifier = Modifier,
    onMenuOpen: () -> Unit = {},
    onNavigateToDownloads: () -> Unit = {},
    onNavigateToSupport: () -> Unit = {},
    dialogViewModel: DownloadDialogViewModel,
    downloader: DownloaderV2 = koinInject(),
) {
    val view = LocalView.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current
    val activity = context as? Activity
    val keyboardController = LocalSoftwareKeyboardController.current

    // --- State Management ---
    var showExitDialog by remember { mutableStateOf(false) }
    var urlText by remember { mutableStateOf("") }
    var permissionsChecked by remember { mutableStateOf(false) }
    var showNotificationPermissionDialog by remember { mutableStateOf(false) }
    var showBatteryOptimizationDialog by remember { mutableStateOf(false) }
    var showSponsorDialog by remember { mutableStateOf(false) }

    // Lifecycle tracking
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var lifecycleRefreshTrigger by remember { mutableIntStateOf(0) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) lifecycleRefreshTrigger++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Database flow
    val recentDownloadsRaw by remember(lifecycleRefreshTrigger) {
        DatabaseUtil.getDownloadHistoryFlow()
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    // Использование нового HomeScreenState
    val screenState = rememberHomeScreenState(
        context = context,
        downloader = downloader,
        recentDownloads = recentDownloadsRaw,
        lifecycleRefreshTrigger = lifecycleRefreshTrigger
    )

    // --- Actions & Launchers ---
    val notificationSettingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    val batteryOptimizationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    val sharedUrl by dialogViewModel.sharedUrlFlow.collectAsState()
    LaunchedEffect(sharedUrl) {
        if (sharedUrl.isNotBlank()) {
            urlText = sharedUrl
            dialogViewModel.consumeSharedUrl()
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionsChecked) {
            permissionsChecked = true
            if (!screenState.hasNotificationPermission) {
                showNotificationPermissionDialog = true
            } else if (!screenState.isBatteryOptimizationDisabled) {
                showBatteryOptimizationDialog = true
            }
        }
        delay(600L)
        val frequency = SPONSOR_DIALOG_FREQUENCY.getInt()
        if (frequency != SPONSOR_FREQ_OFF) {
            val lastShown = SPONSOR_DIALOG_LAST_SHOWN.getLong()
            val intervalMs = if (frequency == SPONSOR_FREQ_WEEKLY) 7L * 24 * 60 * 60 * 1000 else 30L * 24 * 60 * 60 * 1000
            val now = System.currentTimeMillis()
            if (lastShown == 0L || now - lastShown >= intervalMs) showSponsorDialog = true
        }
    }

    LaunchedEffect(screenState.hasNotificationPermission, screenState.isBatteryOptimizationDisabled) {
        if (permissionsChecked) {
            if (!showNotificationPermissionDialog && screenState.hasNotificationPermission && !screenState.isBatteryOptimizationDisabled) {
                showBatteryOptimizationDialog = true
            }
        }
    }

    BackHandler { showExitDialog = true }

    // --- Dialogs (Вынесенные) ---
    if (showNotificationPermissionDialog) {
        NotificationPermissionDialog(
            onDismissRequest = {
                showNotificationPermissionDialog = false
                if (!screenState.isBatteryOptimizationDisabled) showBatteryOptimizationDialog = true
            },
            onConfirm = { notificationSettingsLauncher.launch(it) }
        )
    }

    if (showBatteryOptimizationDialog) {
        BatteryOptimizationDialog(
            onDismiss = { showBatteryOptimizationDialog = false },
            onConfirm = { intent ->
                showBatteryOptimizationDialog = false
                batteryOptimizationLauncher.launch(intent)
            }
        )
    }

    if (showSponsorDialog) {
        SponsorSupportDialog(
            onDismiss = {
                showSponsorDialog = false
                SPONSOR_DIALOG_LAST_SHOWN.updateLong(System.currentTimeMillis())
            },
            onSupport = {
                showSponsorDialog = false
                SPONSOR_DIALOG_LAST_SHOWN.updateLong(System.currentTimeMillis())
                onNavigateToSupport()
            },
        )
    }

    if (showExitDialog) {
        ExitConfirmationDialog(
            onDismiss = { showExitDialog = false },
            onConfirm = {
                showExitDialog = false
                activity?.finish()
            }
        )
    }

    // --- UI Structure ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuOpen) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSupport) {
                        Icon(
                            imageVector = Icons.Outlined.AttachMoney,
                            contentDescription = "Support Developer",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    IconButton(onClick = onNavigateToDownloads) {
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = stringResource(R.string.downloads_history),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        // Используем вынесенный HomeContent для оптимизации производительности
        HomeContent(
            paddingValues = paddingValues,
            urlText = urlText,
            onUrlChange = { urlText = it },
            screenState = screenState,
            downloader = downloader,
            dialogViewModel = dialogViewModel,
            clipboardManager = clipboardManager,
            keyboardController = keyboardController,
            uriHandler = uriHandler,
            view = view
        )
    }


    // --- Bottom Sheet logic (Download Dialog) ---
    var preferences by remember { mutableStateOf(DownloadUtil.DownloadPreferences.createFromPreferences()) }
    val sheetValue by dialogViewModel.sheetValueFlow.collectAsStateWithLifecycle()
    val dialogState by dialogViewModel.sheetStateFlow.collectAsStateWithLifecycle()
    val selectionState = dialogViewModel.selectionStateFlow.collectAsStateWithLifecycle().value
    var showDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(sheetValue) {
        if (sheetValue == DownloadDialogViewModel.SheetValue.Expanded) {
            showDialog = true
        } else {
            launch { sheetState.hide() }.invokeOnCompletion { showDialog = false }
        }
    }

    if (showDialog) {
        DownloadDialog(
            state = dialogState,
            sheetState = sheetState,
            config = Config(),
            preferences = preferences,
            onPreferencesUpdate = { preferences = it },
            onActionPost = { dialogViewModel.postAction(it) },
        )
    }

    when (selectionState) {
        is DownloadDialogViewModel.SelectionState.FormatSelection -> {
            FormatPage(
                state = selectionState,
                onDismissRequest = { dialogViewModel.postAction(Action.Reset) }
            )
        }
        is DownloadDialogViewModel.SelectionState.PlaylistSelection -> {
            PlaylistSelectionPage(
                state = selectionState,
                onDismissRequest = { dialogViewModel.postAction(Action.Reset) }
            )
        }
        DownloadDialogViewModel.SelectionState.Idle -> {}
    }
}