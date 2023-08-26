package com.example.gismemo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gismemo.data.Repository
import kotlinx.coroutines.launch

class SettingsViewModel ( val repository: Repository) : ViewModel() {



    fun onEvent(event: Event) {
        when (event) {
            is Event.UpdateIsUsableHaptic -> {
                updateIsUsableHaptic(event.isUsable)
            }
            is Event.UpdateIsUsableDarkMode -> {
                updateIsUsableDarkMode(event.isUsableDarkMode)
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

    private fun updateIsUsableDarkMode (isUsableDarkMode:Boolean) {
        repository.updateIsUsableDarkMode(isUsableDarkMode)
    }

    sealed class Event {
        data class UpdateIsUsableHaptic(val isUsable:Boolean): Event()

        data class UpdateIsUsableDarkMode(val isUsableDarkMode:Boolean): Event()
        object clearAllMemo: Event()
    }
}