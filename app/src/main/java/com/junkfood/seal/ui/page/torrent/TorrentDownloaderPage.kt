package com.junkfood.seal.ui.page.torrent

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.seal.R
import com.junkfood.seal.torrent.TorrentDownloadState
import com.junkfood.seal.torrent.TorrentState
import com.junkfood.seal.torrent.TorrentStorageUtil
import com.junkfood.seal.torrent.TorrentViewModel
import com.junkfood.seal.ui.component.BackButton
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Main screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TorrentDownloaderPage(
    onNavigateBack: () -> Unit,
    viewModel: TorrentViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val torrents by viewModel.torrents.collectAsStateWithLifecycle()
    val torrentList = torrents.values.toList()

    // ── BottomSheet state ────────────────────────────────────────────
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // ── Storage permission dialog ────────────────────────────────────
    var showPermissionDialog by remember { mutableStateOf(false) }

    // ── Delete-confirmation dialog ────────────────────────────────────
    var pendingDeleteHash by remember { mutableStateOf<String?>(null) }

    // ── .torrent file picker ─────────────────────────────────────────
    val filePicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                if (!TorrentStorageUtil.isStoragePermissionGranted(context)) {
                    showPermissionDialog = true
                } else {
                    viewModel.addTorrentUri(it, context)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showAddSheet = false }
                }
            }
        }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.torrent_downloader)) },
                navigationIcon = { BackButton(onNavigateBack) },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (!TorrentStorageUtil.isStoragePermissionGranted(context)) {
                        showPermissionDialog = true
                    } else {
                        showAddSheet = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation =
                    FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 10.dp,
                    ),
            ) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.torrent_add_magnet))
            }
        },
    ) { paddingValues ->

        // ── Empty state ──────────────────────────────────────────────
        if (torrentList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        Icons.Outlined.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    )
                    Text(
                        text = stringResource(R.string.torrent_empty_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.torrent_empty_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding =
                    androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp,
                    ),
            ) {
                items(torrentList, key = { it.infoHash }) { torrentState ->
                    TorrentItemCard(
                        state = torrentState,
                        onPause = { viewModel.pause(torrentState.infoHash) },
                        onResume = { viewModel.resume(torrentState.infoHash) },
                        onDelete = { pendingDeleteHash = torrentState.infoHash },
                    )
                }
                // Bottom padding so FAB never occludes the last card
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // ── Add torrent BottomSheet ──────────────────────────────────────
    if (showAddSheet) {
        AddTorrentBottomSheet(
            sheetState = sheetState,
            onDismiss = { showAddSheet = false },
            onMagnetSubmit = { magnet ->
                viewModel.addMagnet(magnet)
                scope.launch { sheetState.hide() }.invokeOnCompletion { showAddSheet = false }
            },
            onPickFile = { filePicker.launch("application/x-bittorrent") },
        )
    }

    // ── Storage permission rationale dialog ──────────────────────────
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            icon = { Icon(Icons.Outlined.Warning, contentDescription = null) },
            title = { Text(stringResource(R.string.torrent_permission_title)) },
            text = { Text(stringResource(R.string.torrent_permission_desc)) },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        TorrentStorageUtil.openManageStorageSettings(context)
                    }
                ) {
                    Text(stringResource(R.string.torrent_permission_open_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }

    // ── Delete confirmation dialog ───────────────────────────────────
    pendingDeleteHash?.let { hash ->
        val name = torrents[hash]?.name ?: hash.take(8)
        AlertDialog(
            onDismissRequest = { pendingDeleteHash = null },
            icon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.torrent_cancel_delete)) },
            text = {
                Text(
                    "\"$name\"\n\n" +
                        stringResource(R.string.torrent_delete_confirm_desc)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.remove(hash, deleteFiles = true)
                        pendingDeleteHash = null
                    },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        ),
                ) {
                    Text(stringResource(R.string.torrent_cancel_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteHash = null }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Torrent BottomSheet
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTorrentBottomSheet(
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onMagnetSubmit: (String) -> Unit,
    onPickFile: () -> Unit,
) {
    var magnetText by rememberSaveable { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Sheet title ──────────────────────────────────────────
            Text(
                text = stringResource(R.string.torrent_add_magnet),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            // ── Magnet link input ────────────────────────────────────
            OutlinedTextField(
                value = magnetText,
                onValueChange = { magnetText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.torrent_paste_magnet_hint)) },
                leadingIcon = {
                    Icon(Icons.Outlined.Link, contentDescription = null)
                },
                singleLine = false,
                maxLines = 4,
                shape = RoundedCornerShape(16.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor =
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions =
                    KeyboardActions(
                        onDone = {
                            keyboard?.hide()
                            if (magnetText.trimStart().startsWith("magnet:")) {
                                onMagnetSubmit(magnetText)
                            }
                        }
                    ),
            )

            // Download button
            Button(
                onClick = {
                    keyboard?.hide()
                    if (magnetText.trimStart().startsWith("magnet:")) {
                        onMagnetSubmit(magnetText)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = magnetText.trimStart().startsWith("magnet:"),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(
                    Icons.Outlined.CloudDownload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.torrent_download),
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            // ── Divider ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    "OR",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            // ── File picker button ───────────────────────────────────
            Button(
                onClick = onPickFile,
                modifier = Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(
                    Icons.Outlined.AttachFile,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.torrent_add_file),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Torrent item card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TorrentItemCard(
    state: TorrentDownloadState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Gradient border color pair driven by the current torrent state
    val (gradientStart, gradientEnd) =
        when (state.state) {
            TorrentState.DOWNLOADING ->
                MaterialTheme.colorScheme.primary to
                    MaterialTheme.colorScheme.tertiary
            TorrentState.DOWNLOADING_METADATA ->
                MaterialTheme.colorScheme.secondary to
                    MaterialTheme.colorScheme.primary
            TorrentState.SEEDING ->
                MaterialTheme.colorScheme.tertiary to
                    MaterialTheme.colorScheme.secondary
            TorrentState.FINISHED ->
                MaterialTheme.colorScheme.tertiary to
                    MaterialTheme.colorScheme.tertiary
            TorrentState.PAUSED ->
                MaterialTheme.colorScheme.outline to
                    MaterialTheme.colorScheme.outline
            TorrentState.ERROR ->
                MaterialTheme.colorScheme.error to
                    MaterialTheme.colorScheme.errorContainer
            else ->
                MaterialTheme.colorScheme.outlineVariant to
                    MaterialTheme.colorScheme.outlineVariant
        }

    val borderBrush =
        Brush.horizontalGradient(listOf(gradientStart.copy(alpha = 0.8f), gradientEnd.copy(alpha = 0.8f)))

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .border(width = 1.5.dp, brush = borderBrush, shape = RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // ── Name row ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Outlined.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = gradientStart,
                )
                Text(
                    text = state.name,
                    style =
                        MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                TorrentStateChip(state.state)
            }

            // ── Progress bar ─────────────────────────────────────────
            val animatedProgress by animateFloatAsState(
                targetValue = state.progress.coerceIn(0f, 1f),
                animationSpec = tween(600),
                label = "torrent_progress",
            )
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = gradientStart,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )

            // ── Speed / ETA / size row ───────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    // Speed
                    if (state.downloadSpeed > 0) {
                        Text(
                            text = "↓ ${formatSpeed(state.downloadSpeed)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    // Peers & seeds
                    if (state.peers > 0) {
                        Text(
                            text =
                                stringResource(
                                    R.string.torrent_peers_seeds,
                                    state.peers,
                                    state.seeds,
                                ),
                            style = MaterialTheme.typography.labelSmall,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                    // Error message
                    if (state.state == TorrentState.ERROR && state.errorMessage != null) {
                        Text(
                            text = state.errorMessage,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                // ETA + progress percentage
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "${(state.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = gradientStart,
                        fontWeight = FontWeight.Bold,
                    )
                    if (state.eta > 0) {
                        Text(
                            text = stringResource(R.string.torrent_eta, formatEta(state.eta)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // ── Action buttons ───────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when (state.state) {
                    TorrentState.DOWNLOADING,
                    TorrentState.DOWNLOADING_METADATA,
                    TorrentState.CHECKING_FILES -> {
                        IconButton(onClick = onPause) {
                            Icon(
                                Icons.Outlined.Pause,
                                contentDescription = "Pause",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    TorrentState.PAUSED -> {
                        IconButton(onClick = onResume) {
                            Icon(
                                Icons.Outlined.PlayArrow,
                                contentDescription = "Resume",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    TorrentState.SEEDING -> {
                        IconButton(onClick = onPause) {
                            Icon(
                                Icons.Outlined.Stop,
                                contentDescription = "Stop seeding",
                                tint = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                    else -> {}
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// State chip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TorrentStateChip(state: TorrentState) {
    val (label, containerColor) =
        when (state) {
            TorrentState.DOWNLOADING ->
                stringResource(R.string.torrent_state_downloading) to
                    MaterialTheme.colorScheme.primaryContainer
            TorrentState.DOWNLOADING_METADATA ->
                stringResource(R.string.torrent_state_metadata) to
                    MaterialTheme.colorScheme.secondaryContainer
            TorrentState.SEEDING ->
                stringResource(R.string.torrent_state_seeding) to
                    MaterialTheme.colorScheme.tertiaryContainer
            TorrentState.FINISHED ->
                stringResource(R.string.torrent_state_finished) to
                    MaterialTheme.colorScheme.tertiaryContainer
            TorrentState.PAUSED ->
                stringResource(R.string.torrent_state_paused) to
                    MaterialTheme.colorScheme.surfaceVariant
            TorrentState.ERROR ->
                stringResource(R.string.torrent_state_error) to
                    MaterialTheme.colorScheme.errorContainer
            TorrentState.CHECKING_FILES ->
                stringResource(R.string.torrent_state_checking) to
                    MaterialTheme.colorScheme.secondaryContainer
            else ->
                stringResource(R.string.torrent_state_unknown) to
                    MaterialTheme.colorScheme.surfaceVariant
        }

    SuggestionChip(
        onClick = {},
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
            )
        },
        colors =
            SuggestionChipDefaults.suggestionChipColors(containerColor = containerColor),
        border = null,
        shape = RoundedCornerShape(8.dp),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Formatters
// ─────────────────────────────────────────────────────────────────────────────

private fun formatSpeed(bytesPerSec: Long): String =
    when {
        bytesPerSec >= 1_048_576L -> String.format("%.1f MB/s", bytesPerSec / 1_048_576.0)
        bytesPerSec >= 1_024L -> String.format("%.0f KB/s", bytesPerSec / 1_024.0)
        else -> "$bytesPerSec B/s"
    }

private fun formatEta(seconds: Long): String {
    if (seconds < 0 || seconds > 86_400 * 30) return "∞"
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return when {
        h > 0 -> String.format("%dh %02dm", h, m)
        m > 0 -> String.format("%dm %02ds", m, s)
        else -> "${s}s"
    }
}
