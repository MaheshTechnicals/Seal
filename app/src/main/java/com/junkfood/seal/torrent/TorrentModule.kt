package com.junkfood.seal.torrent

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin DI module for the torrent subsystem.
 *
 * Registered in [com.junkfood.seal.App] alongside the existing app module.
 *
 * Bindings
 * ─────────
 *  • [TorrentEngine] — **singleton**.  One libtorrent session lives for the
 *    entire process lifetime; creating multiple sessions is wasteful and
 *    causes port conflicts.  The engine is created lazily (Koin default) and
 *    is only instantiated when first injected.
 *  • [TorrentViewModel] — scoped ViewModel, one per navigation back-stack entry.
 */
val torrentModule = module {
    single { TorrentEngine(androidContext()) }
    viewModel { TorrentViewModel(get()) }
}
