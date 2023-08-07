package com.example.gismemo.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.gismemo.data.Repository
import com.example.gismemo.db.entity.MEMO_TBL
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MemoContainerViewModel (
    val repository: Repository
) : ViewModel() {


    val _memo: StateFlow<MEMO_TBL?>
            = repository.selectedMemo

    var  _phothoList: StateFlow<List<Uri>>
            = repository.currentPhoto

    var _videoList: StateFlow<List<Uri>>
            = repository.currentVideo

    val _audioTextList: StateFlow<List<Pair<String, List<Uri>>>>
            = repository.currentAudioText

    val _snapShotList: StateFlow<List<Uri>>
            = repository.currentSnapShot


}