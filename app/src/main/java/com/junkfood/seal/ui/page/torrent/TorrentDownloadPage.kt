package com.junkfood.seal.ui.page.torrent

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.PasteFromClipBoardButton
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.TorrentProgress
import com.junkfood.seal.util.TorrentUtil
import com.junkfood.seal.util.makeToast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TorrentDownloadPage(
    onNavigateBack: () -> Unit,
    viewModel: TorrentDownloadViewModel = viewModel()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val viewState by viewModel.viewStateFlow.collectAsStateWithLifecycle()

    var torrentUrl by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Torrent Downloads") },
                navigationIcon = { BackButton(onClick = onNavigateBack) },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Outlined.Add, contentDescription = "Add Torrent")
                    }
                }
            )
        },
        floatingActionButton = {
            if (viewState.activeTorrents.isEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    text = { Text("Add Torrent") }
                )
            }
        }
    ) { padding ->
        if (viewState.activeTorrents.isEmpty() && !viewState.isLoading) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "No Active Torrents",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Add a magnet link or .torrent file to start downloading",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewState.activeTorrents) { torrent ->
                    TorrentCard(
                        torrent = torrent,
                        onCancel = { viewModel.cancelTorrent(torrent.torrentId) },
                        onRemove = { viewModel.removeTorrent(torrent.torrentId) }
                    )
                }
            }
        }

        // Add Torrent Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Torrent") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Enter magnet link or .torrent URL",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        OutlinedTextField(
                            value = torrentUrl,
                            onValueChange = { torrentUrl = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("magnet:?xt=urn:btih:...") },
                            trailingIcon = {
                                PasteFromClipBoardButton {
                                    clipboardManager.getText()?.text?.let { text ->
                                        torrentUrl = text
                                    }
                                }
                            },
                            singleLine = false,
                            maxLines = 4
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (torrentUrl.isNotBlank()) {
                                if (TorrentUtil.isMagnetLink(torrentUrl) || 
                                    TorrentUtil.isTorrentUrl(torrentUrl)) {
                                    viewModel.addTorrent(torrentUrl)
                                    torrentUrl = ""
                                    showAddDialog = false
                                } else {
                                    context.makeToast("Invalid magnet link or torrent URL")
                                }
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TorrentCard(
    torrent: TorrentProgress,
    onCancel: () -> Unit = {},
    onRemove: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = torrent.files.firstOrNull() ?: "Downloading...",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatFileSize(torrent.totalSize),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (torrent.progress < 100f) {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.Outlined.Cancel,
                            contentDescription = "Cancel",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Remove")
                    }
                }
            }

            // Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { torrent.progress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${String.format("%.1f", torrent.progress)}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = formatSpeed(torrent.downloadSpeed),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Peers and seeds info
            AnimatedVisibility(visible = torrent.progress < 100f) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${torrent.numPeers} peers",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${torrent.numSeeders} seeds",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Downloaded size
            Text(
                text = "${formatFileSize(torrent.downloadedSize)} / ${formatFileSize(torrent.totalSize)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / 1024.0 / 1024.0)
        else -> String.format("%.2f GB", bytes / 1024.0 / 1024.0 / 1024.0)
    }
}

private fun formatSpeed(bytesPerSecond: Double): String {
    return when {
        bytesPerSecond < 1024 -> "${bytesPerSecond.toInt()} B/s"
        bytesPerSecond < 1024 * 1024 -> "${(bytesPerSecond / 1024).toInt()} KB/s"
        else -> String.format("%.2f MB/s", bytesPerSecond / 1024.0 / 1024.0)
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TorrentCardPreview() {
    SealTheme {
        Surface {
            TorrentCard(
                torrent = TorrentProgress(
                    torrentId = "test",
                    progress = 45.5f,
                    downloadSpeed = 2.5 * 1024 * 1024, // 2.5 MB/s
                    uploadSpeed = 0.5 * 1024 * 1024,
                    totalSize = 1024L * 1024 * 1024 * 2, // 2 GB
                    downloadedSize = 1024L * 1024 * 1024, // 1 GB
                    numSeeders = 25,
                    numPeers = 150,
                    files = listOf("sample-movie.mkv")
                )
            )
        }
    }
}
