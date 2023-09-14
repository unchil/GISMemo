package com.unchil.gismemo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.unchil.gismemo.data.Repository
import com.unchil.gismemo.db.entity.MEMO_TBL
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MemoMapViewModel (
    val repository: Repository
) : ViewModel() {



    val markerMemoList: MutableStateFlow<List<MEMO_TBL>>
            = repository._markerMemoList

    fun onEvent(event: Event) {
        when (event) {
            Event.SetMarkers -> setMarkers()
            is Event.ToRoute -> toRoute(event.navController, event.route)
        }

    }


    private fun toRoute(navController: NavController, route:String){
        navController.navigate(route = route)
    }


    private fun setMarkers(){
        viewModelScope.launch {
            repository.setMarkerMemoList()
        }
    }


    sealed class Event {
        object  SetMarkers:Event()
        data class ToRoute(val navController: NavController, val route:String) : Event()
    }

}