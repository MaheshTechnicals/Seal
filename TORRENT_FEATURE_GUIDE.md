# Torrent Support Feature

## Overview
Seal Plus now includes full BitTorrent protocol support, allowing you to download files via magnet links and .torrent files using the integrated aria2c engine.

## Features
- ✅ Magnet link support (`magnet:?xt=urn:btih:...`)
- ✅ .torrent file URL support
- ✅ Real-time download progress tracking
- ✅ Peer/seed information display
- ✅ Download speed monitoring
- ✅ Multiple concurrent torrent downloads
- ✅ Download cancellation
- ✅ System integration (handle magnet: links from browser)

## How to Use

### Enable Torrent Support
1. Go to **Settings** → **SealPlus Extras**
2. Enable **Torrent Support** toggle
3. You'll see a new **Torrent Downloads** menu item appear

### Download Torrents
There are multiple ways to download torrents:

#### Method 1: From Browser (Magnet Links)
1. Click on a magnet link in your browser
2. Select **Seal Plus** from the app chooser
3. The app will open and navigate to the torrent page
4. The download will start automatically

#### Method 2: Manual Entry
1. Open **Settings** → **SealPlus Extras** → **Torrent Downloads**
2. Tap the **+** button or FAB
3. Paste your magnet link or .torrent URL
4. Tap **Add**

#### Method 3: Share to Seal Plus
1. Copy a magnet link or .torrent URL
2. Share it to Seal Plus
3. The app will detect it's a torrent and handle it accordingly

### Monitor Downloads
- View active torrents in the **Torrent Downloads** page
- See real-time progress, speed, peers, and seeds
- Cancel downloads at any time
- Remove completed downloads from the list

### Download Location
All torrent files are saved to:
```
/storage/emulated/0/Download/Torrents/
```

## Technical Details

### Architecture
- **Engine**: aria2c (built-in BitTorrent client)
- **Progress Monitoring**: Kotlin Flow-based reactive updates
- **State Management**: MVVM with ViewModel and StateFlow
- **UI**: Jetpack Compose Material 3

### Supported Protocols
- BitTorrent Protocol (BEP-0003)
- Magnet URI scheme (BEP-0009)
- DHT (Distributed Hash Table)
- Peer Exchange (PEX)
- Local Peer Discovery (LPD)

### Settings
Torrent downloads are configured with optimal defaults:
- No seeding after download (seed-time=0)
- DHT enabled for magnet link resolution
- Maximum 50 peers per torrent
- 16 connections per server
- Multi-threaded downloading (16 splits)

## Files Created
```
app/src/main/java/com/junkfood/seal/
├── util/
│   ├── TorrentUtil.kt              # Core torrent utilities
│   └── CoroutineExtensions.kt      # Helper extensions
└── ui/page/torrent/
    ├── TorrentDownloadPage.kt      # Main UI
    └── TorrentDownloadViewModel.kt # Business logic
```

## Preferences
- `TORRENT_SUPPORT`: Boolean preference to enable/disable feature
- Stored in MMKV key-value store

## Limitations
- Seeding is disabled by default (downloads only)
- No torrent file management (create/edit torrents)
- No advanced aria2c configuration UI (uses optimal defaults)

## Future Enhancements
- [ ] Custom download directory selection
- [ ] Seeding support with configurable ratio
- [ ] Torrent file management
- [ ] Advanced aria2c settings UI
- [ ] RSS feed support for automatic downloads
- [ ] Scheduler for bandwidth management

## Troubleshooting

### Torrent not starting
- Ensure **Torrent Support** is enabled in SealPlus Extras
- Check internet connection
- Verify the magnet link or .torrent URL is valid
- Check storage permissions

### Slow download speeds
- Check number of available seeders
- Ensure you're on a good network connection
- Some torrents may have limited seeders

### App crashes when opening magnet link
- Make sure you're using the latest version
- Check logcat for errors
- Report issue on GitHub with logs

## Privacy & Security
- No tracking of downloaded content
- No analytics or data collection
- All downloads are local
- Magnet links are processed locally
- .torrent files are temporarily cached and cleaned up

## Credits
- aria2c by Tatsuhiro Tsujikawa
- Integration by Mahesh Technicals for Seal Plus
