package com.junkfood.seal.ui.page.hidden

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.junkfood.seal.database.objects.DownloadedVideoInfo
import com.junkfood.seal.util.DatabaseUtil
import com.junkfood.seal.util.FileUtil.getFileSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HiddenContentViewModel : ViewModel() {

    val hiddenVideoListFlow = DatabaseUtil.getHiddenDownloadHistoryFlow()

    val fileSizeMapFlow =
        hiddenVideoListFlow.map { list ->
            list.associate { it.id to it.videoPath.getFileSize() }
        }.flowOn(Dispatchers.IO)

    fun unhideItem(info: DownloadedVideoInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseUtil.unhideItem(info)
        }
    }
}
