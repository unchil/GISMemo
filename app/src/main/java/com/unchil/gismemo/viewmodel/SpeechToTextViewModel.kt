package com.unchil.gismemo.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unchil.gismemo.data.Repository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SpeechToTextViewModel (  val repository: Repository ) : ViewModel(){



    val _currentAudioText: StateFlow<List<Pair<String, List<Uri>>>>
            = repository.currentAudioText

    fun onEvent(event: Event){
        when(event){

            is Event.SetAudioText -> {
                setAudioText(event.data)
            }
        }
    }

    private fun setAudioText(data: List<Pair<String, List<Uri>>>){
        viewModelScope.launch {
            repository.currentAudioText.emit(data)
        }
    }


    sealed class Event {
        data class SetAudioText(val data: List<Pair<String,  List<Uri>>>):Event()

    }


}