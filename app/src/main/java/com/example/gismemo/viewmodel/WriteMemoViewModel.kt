package com.example.gismemo.viewmodel

import android.location.Location
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.gismemo.data.Repository
import com.example.gismemo.db.entity.CURRENTLOCATION_TBL
import com.example.gismemo.model.WriteMemoDataType
import com.example.gismemo.view.DrawingPolyline
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class WriteMemoViewModel (
    val repository: Repository
) : ViewModel() {



    val currentLocationStateFlow: StateFlow<CURRENTLOCATION_TBL?>
            = repository._currentLocation



    /*

    val currentTagArrayList: StateFlow< ArrayList<Int>>
            = repository.selectedTagList

    val currentSnapShot: StateFlow<List<Uri>>
            = repository.currentSnapShot



    val currentIsDrawing: StateFlow<Boolean>
            = repository.currentIsDrawing

    val currentIsEraser: StateFlow<Boolean>
            = repository.currentIsEraser


    val currentIsLock: StateFlow<Boolean>
            = repository.currentIsLock

    val currentIsMarker: StateFlow<Boolean>
            = repository.currentIsMarker

    val currentPolylineList: StateFlow<List<DrawingPolyline>>
            = repository.currentPolylineList



     */


    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    private val _effect = MutableSharedFlow<Effect>(replay = 0)
    val effect: SharedFlow<Effect> = _effect


        fun onEvent(event: Event){
            when(event){
                is  Event.Error -> onError()
                is Event.SetSnapShot -> {
                    setSnapShot(event.snapShotList)
                }
                is Event.SetSelectedTab -> {
                    setSelectedTab(event.selectedTab)
                }
                /*
                is Event.SetSelectedMainTab -> {
                    setSelectedMainTab(event.selectedMainTab)
                }

                 */
                is Event.ToRoute -> {
                    toRoute(event.navController, event.route)
                }
                is Event.SetSheetContent -> {
                    setSheetContent(event.selectedContent)
                }
                Event.InitMemo -> {
                    initMemo()
                }
                is Event.UploadMemo -> {
                    upLoadMemo(
                        id = event.id,
                        isLock = event.isLock,
                        isMark = event.isMark,
                       // selectTagList = event.selectTagList,
                        selectedTagArrayList = event.selectedTagArrayList,
                        title = event.title,
                        location = event.location
                    )
                }
                is Event.DeleteMemoItem -> {
                    deleteMemoItem(type = event.type, index = event.index)
                }
                is Event.SetDeviceLocation -> {
                    setDeviceLocation(event.location)
                }
                is Event.DeleteSnapShot -> {
                    deleteSnapshot(index = event.index)
                }
                is Event.SetCurrentLocation -> {
                    setCurrentLocation(event.latlng)
                }

                is Event.UpdatePolylineList -> {
                    updatePolyLinelist(event.polylineList)
                }
                is Event.UpdateIsLock -> {
                    updateIsLock(event.isLock)
                }
                is Event.UpdateIsMarker -> {
                    updateIsMarker(event.isMarker)
                }

                is Event.UpdateSelectedTags -> {
                    updateSelectedTags(event.selectedTags)
                }
                Event.ClearCurrentValue -> {
                    clearCurrentValue()
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


    private fun clearCurrentValue(){
        repository.clearCurrentValue()
    }

    private fun updateSelectedTags(tags: ArrayList<Int>) {
        repository.updateCurrentTags(tags)
    }


    private fun updatePolyLinelist(polylineList: List<DrawingPolyline>) {
        repository.updateCurrentPolylineList(polylineList)
    }


    private fun updateIsLock(isLock:Boolean) {
        repository.updateCurrentIsLock(isLock)
    }

    private fun updateIsMarker(isMarker:Boolean) {
       repository.updateCurrentIsMarker(isMarker)
    }



    private fun setCurrentLocation(latlng: LatLng){

        viewModelScope.launch {
           repository.setCurrentLocation(
               CURRENTLOCATION_TBL(
               dt = System.currentTimeMillis(),
               latitude = latlng.latitude.toFloat(),
               longitude = latlng.longitude.toFloat(),
               altitude = 0f)
           )

            repository.getWeatherData(
                latlng.latitude.toString(), latlng.longitude.toString())
        }
    }

    private fun deleteSnapshot(  index:Int) {
        viewModelScope.launch {
            _effect.emit(Effect.DeleteSnapshot(index))
        }
    }

    private fun setDeviceLocation(location:Location){
        viewModelScope.launch {
            repository.setDeviceLocation(location )

            repository.getWeatherData(
                location.latitude.toString(), location.longitude.toString())
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
   //     selectTagList:List<Pair<Long,Int>>,
        selectedTagArrayList: ArrayList<Int>,
        title:String,
        location: CURRENTLOCATION_TBL ){

        viewModelScope.launch {
          //  repository.insertMemo(id, isLock,isMark,selectTagList,title,location)
            repository.insertMemo(id, isLock,isMark,selectedTagArrayList,title,location)
        }
    }



    private fun toRoute(navController: NavController, route:String){
        navController.navigate(route = route)
    }


    private fun setSheetContent(selectedContent: WriteMemoSheetContent){
        viewModelScope.launch {
            _effect.emit(Effect.SetSheetContent(selectedContent))
        }
    }

    private fun setSelectedTab(selectedTab: WriteMemoDataType){
        viewModelScope.launch {
            repository.currentSelectedTab.emit(selectedTab)
        }
    }

    /*
    private fun setSelectedMainTab(selectedTab: MainTabType){
        viewModelScope.launch {
            repository.updateSelectedMainTab(selectedTab)
        }
    }

     */

    private fun setSnapShot(snapShotList:  List<Uri>){
        viewModelScope.launch {
            repository.currentSnapShot.emit(snapShotList)
            setSelectedTab(WriteMemoDataType.SNAPSHOT)
            setSheetContent(WriteMemoSheetContent.DATACONTAINER)
        }
    }


    private fun onError() {
        viewModelScope.launch {

        }
    }

    enum class WriteMemoSheetContent {
        CAMERA, TAGSELECT, DATACONTAINER, SPEECHTOTEXT, NOACTION
    }



    data class State (
        val tags : List<String> =  listOf(""),
        val isMarking : Boolean = false,
        val isSecret: Boolean = false,
        val audioIsEmpty:Boolean = false,
        val videoIsEmpty:Boolean = false,
        val snapShotIsEmpty:Boolean = false,
        val photoIsEmpty:Boolean = false

    )

        sealed class Event {
            data class SetSnapShot(val snapShotList: List<Uri>): Event()
            data class SetSelectedTab(val selectedTab:WriteMemoDataType): Event()

         //   data class SetSelectedMainTab(val selectedMainTab:MainTabType): Event()

            data class SetSheetContent(val selectedContent:WriteMemoSheetContent): Event()

            data class ToRoute(val navController: NavController, val route:String) :Event()

            data class DeleteMemoItem(val type:WriteMemoDataType, val index:Int): Event()
            data class DeleteSnapShot( val index:Int): Event()


            data class UpdateIsDrawing(val isDrawing:Boolean):Event()
            data class UpdateIsEraser(val isEraser:Boolean): Event()

            data class UpdateIsLock(val isLock:Boolean):Event()
            data class UpdateIsMarker(val isMarker:Boolean): Event()
            data class UpdateSelectedTags(val selectedTags: ArrayList<Int>):Event()
            data class UpdatePolylineList(val polylineList: List<DrawingPolyline>):Event()

            object  ClearCurrentValue:Event()


            //object UploadMemo: Event()
            data class  UploadMemo(val id:Long,
                                   val isLock:Boolean,
                                   val isMark:Boolean,
                              //     var selectTagList:List<Pair<Long,Int>>  ,
                                   var selectedTagArrayList: ArrayList<Int>,
                                   var title:String,
                                   var location:CURRENTLOCATION_TBL
                                   ): Event()



            object InitMemo: Event()

            data class  SetDeviceLocation(val location: Location): Event()
            data class SetCurrentLocation(val latlng:LatLng): Event()

            data class Error(val throwable: Throwable?) : Event()
        }


        sealed class Effect {
            data class SetSheetContent(val selectedContent:WriteMemoSheetContent): Effect()


             data class SetSelectedTab(val selectedTab:WriteMemoDataType): Effect()

            object NoAction:Effect()
            data class DeleteSnapshot(val index:Int):Effect()

        }


}