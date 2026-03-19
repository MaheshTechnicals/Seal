package com.junkfood.seal.preview

import androidx.compose.material3.DrawerValue
import androidx.compose.ui.tooling.preview.PreviewParameterProvider


data class AppVersion(
    val versionName: String?
)

class AppVersionProvider : PreviewParameterProvider<AppVersion> {
    override val values = sequenceOf(
        AppVersion("2.4.0-debug"),
        AppVersion(null)
    )
}

class NavDrawerStateOpen : PreviewParameterProvider<DrawerValue> {
    override val values: Sequence<DrawerValue>
        get() = sequenceOf(DrawerValue.Open)
}

class NavDrawerStateClosed : PreviewParameterProvider<DrawerValue> {
    override val values: Sequence<DrawerValue>
        get() = sequenceOf(DrawerValue.Closed)
}