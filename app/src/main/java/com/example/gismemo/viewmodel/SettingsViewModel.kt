package com.example.gismemo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gismemo.data.Repository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel ( val repository: Repository) : ViewModel() {



    fun onEvent(event: Event) {
        when (event) {
            is Event.UpdateIsUsableHaptic -> {
                updateIsUsableHaptic(event.isUsable)
            }
            Event.clearAllMemo -> {
                clearAllMemo()
            }
        }
    }

    private fun clearAllMemo () {
        viewModelScope.launch {
            repository.deleteAllMemo()
        }

    }

    private fun updateIsUsableHaptic (isUsable:Boolean) {
        repository.updateIsUsableHaptic(isUsable)
    }


    sealed class Event {
        data class UpdateIsUsableHaptic(val isUsable:Boolean): Event()
        object clearAllMemo: Event()
    }
}