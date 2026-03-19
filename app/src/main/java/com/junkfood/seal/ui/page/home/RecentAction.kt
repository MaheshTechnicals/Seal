package com.junkfood.seal.ui.page.home

sealed class RecentAction {
    data class OpenFile(val path: String) : RecentAction()
    data class Share(val path: String) : RecentAction()
    data class CopyLink(val url: String) : RecentAction()
    object ShowDetails : RecentAction()
}