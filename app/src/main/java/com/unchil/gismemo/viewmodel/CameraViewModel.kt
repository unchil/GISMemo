package com.unchil.gismemo.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unchil.gismemo.data.Repository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CameraViewModel  ( val repository: Repository) : ViewModel(){



    var _currentPhoto: StateFlow<List<Uri>>
            = repository.currentPhoto


    var _currentVideo:  StateFlow<List<Uri>>
            = repository.currentVideo

    fun onEvent(event: Event){
            when(event){
                is Event.SetPhotoVideo -> {
                    setPhotoVideo(event.photoList, event.videoList)
                }
            }
    }

    private fun setPhotoVideo(photoList:List<Uri>, videoList:List<Uri>){
        viewModelScope.launch {
            repository.currentPhoto.emit(photoList)
            repository.currentVideo.emit(videoList)

        }
    }

    sealed class Event {
        data class SetPhotoVideo(val photoList:List<Uri>, val videoList:List<Uri>):Event()
    }

    private val _effect = MutableSharedFlow<Effect>()
    val effect: SharedFlow<Effect> = _effect

    sealed class Effect {
        object NoAction: Effect()
    }


}