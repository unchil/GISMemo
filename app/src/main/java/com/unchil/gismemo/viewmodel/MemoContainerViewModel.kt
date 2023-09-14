package com.unchil.gismemo.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.unchil.gismemo.data.Repository
import com.unchil.gismemo.model.MemoDataContainerUser
import kotlinx.coroutines.flow.StateFlow

class MemoContainerViewModel (
    val repository: Repository,
    val user: MemoDataContainerUser
) : ViewModel() {

    val  phothoList: StateFlow<List<Uri>>
    val videoList: StateFlow<List<Uri>>
    val audioTextList: StateFlow<List<Pair<String, List<Uri>>>>
    val snapShotList: StateFlow<List<Uri>>

    init {
        when(user){
            MemoDataContainerUser.DetailMemoView -> {
                phothoList  = repository.detailPhoto
                videoList  = repository.detailVideo
                audioTextList  = repository.detailAudioText
                snapShotList  = repository.detailSnapShot
            }
            MemoDataContainerUser.WriteMemoView -> {
                phothoList  = repository.currentPhoto
                 videoList  = repository.currentVideo
                 audioTextList  = repository.currentAudioText
                 snapShotList  = repository.currentSnapShot
            }
        }
    }

}