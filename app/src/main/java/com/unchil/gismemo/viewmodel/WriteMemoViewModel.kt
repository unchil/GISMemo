package com.unchil.gismemo.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.unchil.gismemo.data.Repository
import com.unchil.gismemo.db.entity.CURRENTLOCATION_TBL
import com.unchil.gismemo.model.WriteMemoDataType
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class WriteMemoViewModel (
    val repository: Repository
) : ViewModel() {

    val  phothoList: StateFlow<List<Uri>> = repository.currentPhoto
    val videoList: StateFlow<List<Uri>> =  repository.currentVideo
    val audioTextList: StateFlow<List<Pair<String, List<Uri>>>> =  repository.currentAudioText
    val snapShotList: StateFlow<List<Uri>> = repository.currentSnapShot

        fun onEvent(event: Event){
            when(event){

                is Event.SetSnapShot -> {
                    setSnapShot(event.snapShotList)
                }

                is Event.ToRoute -> {
                    toRoute(event.navController, event.route)
                }

                Event.InitMemo -> {
                    initMemo()
                }
                is Event.UploadMemo -> {

                    upLoadMemo(
                        id = event.id,
                        isLock = event.isLock,
                        isMark = event.isMark,
                        selectedTagArrayList = event.selectedTagArrayList,
                        title = event.title,
                        desc = event.desc,
                        snippets = event.snippets,
                        location = event.location
                    )
                }
                is Event.DeleteMemoItem -> {
                    deleteMemoItem(type = event.type, index = event.index)
                }

                is Event.SearchWeather -> {
                    searchWeather(event.location)
                }

                is Event.UpdateIsLock -> {
                    updateIsLock(event.isLock)
                }
                is Event.UpdateIsMarker -> {
                    updateIsMarker(event.isMarker)
                }

                is Event.UpdateIsDrawing -> {
                    updateIsDrawing(event.isDrawing)
                }
                is Event.UpdateIsEraser -> {
                    updateIsEraser(event.isEraser)
                }

            }
        }



    private fun updateIsDrawing (isDrawing:Boolean) {
        repository.updateCurrentIsDrawing(isDrawing)
    }


    private fun updateIsEraser(isEraser:Boolean) {
        repository.updateCurrentIsEraser(isEraser)
    }



    private fun updateIsLock(isLock:Boolean) {
        repository.updateCurrentIsLock(isLock)
    }

    private fun updateIsMarker(isMarker:Boolean) {
       repository.updateCurrentIsMarker(isMarker)
    }


    private fun searchWeather(location: LatLng) {
        viewModelScope.launch {
            repository.getWeatherData(
                location.latitude.toString(), location.longitude.toString()
            )
        }
    }

    private fun deleteMemoItem( type:WriteMemoDataType,  index:Int) {
        viewModelScope.launch {
            repository.deleteMemoItem(type, index)
        }
    }

    private fun initMemo(){
        viewModelScope.launch {
            repository.initMemoItem()
        }
    }




    private fun upLoadMemo(
        id:Long,
        isLock:Boolean,
        isMark:Boolean,
        selectedTagArrayList: ArrayList<Int>,
        title:String,
        desc:String,
        snippets:String,
        location: CURRENTLOCATION_TBL ){

        viewModelScope.launch {
            repository.insertMemo(id, isLock,isMark,selectedTagArrayList,title, desc, snippets, location)
        }
    }



    private fun toRoute(navController: NavController, route:String){
        navController.navigate(route = route)
    }



    private fun setSelectedTab(selectedTab: WriteMemoDataType){
        viewModelScope.launch {
            repository.currentSelectedTab.emit(selectedTab)
        }
    }



    private fun setSnapShot(snapShotList:  List<Uri>){
        viewModelScope.launch {
            repository.currentSnapShot.emit(snapShotList)
            setSelectedTab(WriteMemoDataType.SNAPSHOT)
        }
    }


        sealed class Event {
            data class SetSnapShot(val snapShotList: List<Uri>): Event()
            data class ToRoute(val navController: NavController, val route:String) :Event()

            data class DeleteMemoItem(val type:WriteMemoDataType, val index:Int): Event()

            data class UpdateIsDrawing(val isDrawing:Boolean):Event()
            data class UpdateIsEraser(val isEraser:Boolean): Event()

            data class UpdateIsLock(val isLock:Boolean):Event()
            data class UpdateIsMarker(val isMarker:Boolean): Event()

            data class  UploadMemo(val id:Long,
                                   val isLock:Boolean,
                                   val isMark:Boolean,
                                   var selectedTagArrayList: ArrayList<Int>,
                                   var title:String,
                                   var desc:String,
                                   var snippets:String,
                                   var location:CURRENTLOCATION_TBL
                                   ): Event()



            object InitMemo: Event()
            data class  SearchWeather(val location: LatLng): Event()

        }


}