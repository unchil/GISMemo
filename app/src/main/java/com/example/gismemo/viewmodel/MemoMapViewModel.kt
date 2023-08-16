package com.example.gismemo.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.gismemo.data.Repository
import com.example.gismemo.db.entity.CURRENTLOCATION_TBL
import com.example.gismemo.db.entity.MEMO_TBL
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MemoMapViewModel (
    val repository: Repository
) : ViewModel() {


    val currentLocationFlow: MutableStateFlow<CURRENTLOCATION_TBL?>
        = repository._currentLocation

    val markerMemoList: MutableStateFlow<List<MEMO_TBL>>
            = repository._markerMemoList

    fun onEvent(event: Event) {
        when (event) {
            is Event.SetDeviceLocation ->  setDeviceLocation(event.location)
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

    private fun setDeviceLocation(location:Location){
        viewModelScope.launch {
            repository.setDeviceLocation(location )
        }
    }


    sealed class Event {
        data class  SetDeviceLocation(val location: Location): Event()
        object  SetMarkers:Event()
        data class ToRoute(val navController: NavController, val route:String) : Event()
    }

}