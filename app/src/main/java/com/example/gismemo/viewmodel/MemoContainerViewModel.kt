package com.example.gismemo.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.gismemo.data.Repository
import com.example.gismemo.db.entity.MEMO_TBL
import com.example.gismemo.model.MemoDataContainerUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MemoContainerViewModel (
    val repository: Repository,
    val user: MemoDataContainerUser
) : ViewModel() {


/*
    var  _phothoList: StateFlow<List<Uri>>
            = repository.currentPhoto

    var _videoList: StateFlow<List<Uri>>
            = repository.currentVideo

    val _audioTextList: StateFlow<List<Pair<String, List<Uri>>>>
            = repository.currentAudioText

    val _snapShotList: StateFlow<List<Uri>>
            = repository.currentSnapShot

 */

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