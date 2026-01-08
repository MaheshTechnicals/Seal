# 🚀 Torrent Feature - Quick Reference

## 📦 New Files Created

```
app/src/main/java/com/junkfood/seal/
├── util/
│   ├── TorrentUtil.kt                    # Core torrent utilities & aria2c integration
│   └── CoroutineExtensions.kt            # Safe coroutine helpers
│
└── ui/page/torrent/
    ├── TorrentDownloadPage.kt            # Torrent UI (Compose)
    └── TorrentDownloadViewModel.kt       # Torrent business logic

docs/
├── TORRENT_FEATURE_GUIDE.md             # User documentation
└── TORRENT_IMPLEMENTATION_SUMMARY.md    # Technical summary
```

## 🔧 Modified Files

```
✏️  PreferenceUtil.kt         - Added TORRENT_SUPPORT constant
✏️  SealPlusExtrasPage.kt     - Added toggle + navigation
✏️  Route.kt                   - Added TORRENT_DOWNLOAD route
✏️  AppEntry.kt                - Added torrent page navigation
✏️  MainActivity.kt            - Added magnet link handling
✏️  AndroidManifest.xml        - Added magnet: intent filter
✏️  strings.xml                - Added "Advanced Features" string
```

## 🎯 Key Components

### TorrentUtil.kt
```kotlin
// Check if URL is magnet/torrent
TorrentUtil.isMagnetLink(url)
TorrentUtil.isTorrentUrl(url)

// Check if feature enabled
TorrentUtil.isTorrentSupportEnabled()

// Download torrent
TorrentUtil.startTorrentDownload(source, dir) { progress ->
    // Handle progress
}

// Monitor progress
TorrentUtil.monitorTorrentProgress(id, dir)
    .collect { progress -> /* Update UI */ }

// Parse magnet link
val info = TorrentUtil.parseMagnetLink(magnetUrl)
```

### TorrentDownloadViewModel.kt
```kotlin
// Add torrent
viewModel.addTorrent(magnetOrTorrentUrl)

// Cancel download
viewModel.cancelTorrent(torrentId)

// Remove from list
viewModel.removeTorrent(torrentId)

// Observe state
val state by viewModel.viewStateFlow.collectAsStateWithLifecycle()
```

## 🔌 Integration Points

### 1. Preference Toggle
**Location:** Settings → SealPlus Extras  
**Key:** `TORRENT_SUPPORT`  
**Type:** Boolean (MMKV)

### 2. Navigation Route
**Route:** `Route.TORRENT_DOWNLOAD`  
**From:** SealPlusExtrasPage → TorrentDownloadPage

### 3. Intent Handling
**Scheme:** `magnet:`  
**Handler:** MainActivity.onNewIntent()  
**Flow:** Browser → Intent → MainActivity → Navigation

### 4. Download Directory
```kotlin
FileUtil.getExternalDownloadDirectory()
    .resolve("Torrents")
```

## 📱 UI Flow

```
┌─────────────────┐
│   Settings      │
│   SealPlus      │
│   Extras        │
└────────┬────────┘
         │
         ├─► [Toggle] Torrent Support
         │
         └─► [Navigate] Torrent Downloads
                    │
                    ▼
         ┌─────────────────┐
         │ Torrent         │
         │ Download        │
         │ Page            │
         └────────┬────────┘
                  │
                  ├─► [Empty State] No torrents
                  ├─► [FAB] Add torrent
                  ├─► [List] Active torrents
                  │      ├─► Progress bar
                  │      ├─► Speed/Peers
                  │      ├─► Cancel button
                  │      └─► Remove button
                  └─► [Dialog] Add torrent
                         └─► Paste magnet/URL
```

## 🧪 Testing Commands

### Build & Install
```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/*.apk
```

### Check Logs
```bash
adb logcat | grep -E "TorrentUtil|TorrentDownloadViewModel"
```

### Test Magnet Link
```bash
adb shell am start -a android.intent.action.VIEW \
  -d "magnet:?xt=urn:btih:08ada5a7a6183aae1e09d831df6748d566095a10&dn=Sintel"
```

### Check Preference
```bash
adb shell run-as com.junkfood.sealplus cat /data/data/com.junkfood.sealplus/files/mmkv/mmkv.default
```

## 🐛 Debugging Tips

### Common Issues

**1. Torrent not starting**
- Check: `TORRENT_SUPPORT.getBoolean()` returns true
- Check: aria2c library exists in `nativeLibraryDir`
- Check: Storage permissions granted

**2. Progress not updating**
- Check: Flow collection in ViewModel
- Check: StateFlow updates in ViewModel
- Check: Download directory exists and is writable

**3. Magnet links not handled**
- Check: AndroidManifest has magnet intent filter
- Check: MainActivity.onNewIntent() receives intent
- Check: TorrentUtil.isMagnetLink() returns true

### Logging
Add at critical points:
```kotlin
Log.d("TorrentUtil", "Starting download: $url")
Log.d("TorrentDownloadViewModel", "Progress: $progress%")
```

## 📊 State Machine

```
┌─────────┐
│  IDLE   │ Initial state
└────┬────┘
     │ addTorrent()
     ▼
┌─────────┐
│ LOADING │ Downloading .torrent / Connecting
└────┬────┘
     │
     ▼
┌─────────┐
│PROGRESS │ Downloading (0-100%)
└────┬────┘
     │
     ├─► [Cancel] → CANCELED
     │
     └─► [100%] → COMPLETED
```

## 🎨 UI Components

### TorrentCard
Shows individual torrent with:
- Title (first file name)
- Progress bar (0-100%)
- Speed (MB/s)
- Peers/Seeds count
- Total/Downloaded size
- Cancel/Remove button

### EmptyState
Shows when no torrents:
- Cloud icon
- "No Active Torrents" text
- Instructions
- FAB to add

### AddDialog
Modal dialog with:
- TextField for URL input
- Paste button (from clipboard)
- Add/Cancel buttons

## 💡 Best Practices

1. **Always check feature enabled** before torrent operations
2. **Clean up temp files** (.torrent cache)
3. **Cancel jobs** in ViewModel.onCleared()
4. **Handle errors gracefully** with user feedback
5. **Use Flow** for reactive progress updates
6. **Follow MVVM** - keep UI logic in ViewModel
7. **Test with legal content** (Ubuntu ISOs, CC videos)

## 🔗 Useful Resources

- [aria2c Documentation](https://aria2.github.io/)
- [BitTorrent Protocol](http://bittorrent.org/beps/bep_0003.html)
- [Magnet URI](http://magnet-uri.sourceforge.net/)
- [Material Icons](https://fonts.google.com/icons)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)

---

**Quick Start:** Enable toggle in Settings → SealPlus Extras → Torrent Support ✅
