package com.example.gismemo.view


import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.speech.RecognizerIntent
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
//import androidx.compose.material.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.HighlightOff
import androidx.compose.material3.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.size.Size
import com.example.gismemo.LocalUsableHaptic
import com.example.gismemo.R
import com.example.gismemo.data.RepositoryProvider
import com.example.gismemo.db.LocalLuckMemoDB
import com.example.gismemo.db.entity.CURRENTLOCATION_TBL
import com.example.gismemo.db.entity.toLatLng
import com.example.gismemo.model.*
import com.example.gismemo.navigation.GisMemoDestinations
import com.example.gismemo.shared.composables.*
import com.example.gismemo.shared.utils.FileManager
import com.example.gismemo.shared.utils.SnackBarChannelType
import com.example.gismemo.shared.utils.snackbarChannelList
import com.example.gismemo.ui.theme.GISMemoTheme
import com.example.gismemo.utils.getDeviceLocation
import com.example.gismemo.viewmodel.MemoContainerViewModel
import com.example.gismemo.viewmodel.WeatherViewModel
import com.example.gismemo.viewmodel.WriteMemoViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.maps.android.compose.widgets.ScaleBar
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import okhttp3.internal.toImmutableList
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


enum class DrawingMenu {
DrawEraser
}

val DrawingMenuList = listOf(
    DrawingMenu.DrawEraser
)

fun DrawingMenu.getDesc():Pair<ImageVector, ImageVector?> {
    return when(this){
        DrawingMenu.DrawEraser -> {
            Pair( Icons.Outlined.Draw , Icons.Outlined.TouchApp)
        }
    }
}

enum class MapTypeMenu {
    NORMAL,TERRAIN,HYBRID
}

val MapTypeMenuList = listOf(
    MapTypeMenu.NORMAL,
    MapTypeMenu.TERRAIN,
    MapTypeMenu.HYBRID
)

fun MapTypeMenu.getDesc():Pair<ImageVector, ImageVector?> {
    return when(this){
        MapTypeMenu.NORMAL -> {
            Pair( Icons.Outlined.Map, null)
        }
        MapTypeMenu.TERRAIN -> {
            Pair( Icons.Outlined.Forest, null)
        }
        MapTypeMenu.HYBRID -> {
            Pair( Icons.Outlined.Public, null)
        }
    }
}

enum class SaveMenu{
    CLEAR,SAVE
}

val SaveMenuList = listOf(
    SaveMenu.CLEAR,
    SaveMenu.SAVE
)

fun SaveMenu.getDesc():Pair<ImageVector, ImageVector?> {
    return when(this){
        SaveMenu.CLEAR -> {
            Pair(Icons.Outlined.Replay,  null)
        }
        SaveMenu.SAVE -> {
            Pair(Icons.Outlined.PublishedWithChanges,  null)
        }
    }
}

enum class SettingMenu{
    SECRET, MARKER,TAG
}

val SettingMenuList = listOf(
    SettingMenu.SECRET,
    SettingMenu.MARKER,
    SettingMenu.TAG
)

fun SettingMenu.getDesc():Pair<ImageVector, ImageVector?> {
    return when(this){
        SettingMenu.SECRET -> {
            Pair(Icons.Outlined.Lock,  Icons.Outlined.LockOpen)
        }
        SettingMenu.MARKER -> {
            Pair(Icons.Outlined.LocationOn,  Icons.Outlined.LocationOff)
        }
        SettingMenu.TAG -> {
            Pair(Icons.Outlined.Class,  null)
        }

    }
}




enum class CreateMenu {
    SNAPSHOT,RECORD,CAMERA
}

val CreateMenuList = listOf(
    CreateMenu.SNAPSHOT,
    CreateMenu.RECORD,
    CreateMenu.CAMERA,
)


fun CreateMenu.getDesc():Pair<ImageVector, String?>{
    return  when(this){
        CreateMenu.SNAPSHOT -> {
            Pair(Icons.Outlined.Screenshot,  null)
        }
        CreateMenu.RECORD -> {
            Pair(Icons.Outlined.Mic,  GisMemoDestinations.SpeechToText.route)
        }
        CreateMenu.CAMERA -> {
            Pair(Icons.Outlined.Videocam, GisMemoDestinations.CameraCompose.route)
        }
    }
}


@SuppressLint("SuspiciousIndentation")
@OptIn(
    ExperimentalMaterial3Api::class,
    MapsComposeExperimentalApi::class, ExperimentalComposeUiApi::class
)
@Composable
fun WriteMemoView(navController: NavController ){






    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current


    val db = LocalLuckMemoDB.current
    val viewModel = remember {
        WriteMemoViewModel(repository = RepositoryProvider.getRepository().apply { database = db }  )
    }


    val isUsableHaptic = LocalUsableHaptic.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    fun hapticProcessing(){
        if(isUsableHaptic){
            coroutineScope.launch {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }



    val currentLocation =
        viewModel.currentLocationStateFlow.collectAsState().value
            ?: CURRENTLOCATION_TBL(0L,0f,0f,0f)


    val markerState = MarkerState( position = currentLocation.toLatLng() )

    val defaultCameraPosition = CameraPosition.fromLatLngZoom(currentLocation.toLatLng(), 16f)
    var cameraPositionState = CameraPositionState(position = defaultCameraPosition)
    var mapProperties by remember {  mutableStateOf( MapProperties(mapType = MapType.NORMAL,   isMyLocationEnabled = false) )  }
    val uiSettings by remember {  mutableStateOf(  MapUiSettings(  zoomControlsEnabled = false) ) }



    val sheetState = SheetState(
        skipPartiallyExpanded = false,
        initialValue = SheetValue.Hidden,
        skipHiddenState = false)

    val scaffoldState =  rememberBottomSheetScaffoldState( bottomSheetState = sheetState )



    var isTagDialog by  rememberSaveable { mutableStateOf(false) }
    val isAlertDialog =  rememberSaveable { mutableStateOf(false) }

    var isSnapShot by remember { mutableStateOf(false) }
    var isMapClear by  remember { mutableStateOf(false) }

    val currentPolyline  =   mutableStateListOf<LatLng>( )


    var isDrawing by rememberSaveable { mutableStateOf(false) }

    val selectedTagArray: MutableState<ArrayList<Int>> = rememberSaveable{ mutableStateOf(arrayListOf())  }
    var isLock by rememberSaveable { mutableStateOf(false) }
    var isMark by rememberSaveable { mutableStateOf(false) }


// Not recompose rememberSaveable 에 mutableStatelist 는

    var polylineList: SnapshotStateList<List<LatLng>>

    val polylineListR:MutableList<DrawingPolyline> = rememberSaveable { mutableListOf() }
    val snapShotList:MutableList<Uri> = rememberSaveable { mutableListOf() }


    val configuration = LocalConfiguration.current

    val alignmentSaveMenuList: Alignment
    val alignmentMyLocation: Alignment
    val alignmentCreateMenuList: Alignment
    val alignmentSettingMenuList: Alignment
    val alignmentDrawingMenuList: Alignment
    val alignmentMapTypeMenuList: Alignment


    when (configuration.orientation) {

        Configuration.ORIENTATION_PORTRAIT -> {
            polylineList = polylineListR.toMutableStateList()

            alignmentSaveMenuList = Alignment.TopCenter
            alignmentMyLocation = Alignment.TopEnd
            alignmentCreateMenuList = Alignment.CenterStart
            alignmentSettingMenuList = Alignment.BottomStart
            alignmentDrawingMenuList = Alignment.BottomEnd
            alignmentMapTypeMenuList = Alignment.CenterEnd

        }
        else -> {
            polylineList = polylineListR.toMutableStateList()

            alignmentSaveMenuList = Alignment.TopCenter
            alignmentMyLocation = Alignment.TopEnd
            alignmentCreateMenuList = Alignment.TopStart
            alignmentSettingMenuList = Alignment.BottomStart
            alignmentDrawingMenuList = Alignment.BottomEnd
            alignmentMapTypeMenuList = Alignment.CenterEnd
        }
    }


    val snackbarHostState = remember { SnackbarHostState() }
    val channel = remember { Channel<Int>(Channel.CONFLATED) }
    LaunchedEffect(channel) {
        channel.receiveAsFlow().collect { index ->
            val channelData = snackbarChannelList.first {
                it.channel == index
            }

            //----------
            val message = when(channelData.channelType){
                else -> { channelData.message}
            }
            //----------

            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = channelData.actionLabel,
                withDismissAction = channelData.withDismissAction,
                duration = channelData.duration
            )
            when (result) {
                SnackbarResult.ActionPerformed -> {

                    when(channelData.channelType) {
                        SnackBarChannelType.MEMO_CLEAR_REQUEST -> {
                            viewModel.onEvent( WriteMemoViewModel.Event.InitMemo  )


                            snapShotList.clear()
                            isLock = false
                            isMark = false
                            isMapClear = true
                            selectedTagArray.value = arrayListOf()


                            channel.trySend(snackbarChannelList.first {
                                it.channelType == SnackBarChannelType.MEMO_CLEAR_RESULT
                            }.channel)
                        }
                        else -> {}
                    }


                }
                SnackbarResult.Dismissed -> {

                }
            }
        }
    }


    val onMapLongClickHandler: (LatLng) -> Unit = {
        //isPressed.value = true
        hapticProcessing()
        markerState.position = it
       // cameraPositionState = CameraPositionState( position =  CameraPosition.fromLatLngZoom(it, 16f))
        viewModel.onEvent(WriteMemoViewModel.Event.SetCurrentLocation(it))
    }

    val addPolyline:(MotionEvent)-> Unit = { event ->
        lifecycleOwner.lifecycleScope.launch {
            cameraPositionState.projection?.let {

                val latLng = it.fromScreenLocation( Point( Math.round(event.x), Math.round(event.y) ) )

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        currentPolyline.add(latLng)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        currentPolyline.add(latLng)
                    }
                    MotionEvent.ACTION_UP -> {

                        polylineList.add(currentPolyline.toList())
                        polylineListR.add(currentPolyline.toList())
                        currentPolyline.clear()

                    }
                    else -> {}
                }
            }
        }
    }


    val deleteSnapshotHandle:((page:Int) -> Unit) = { page ->
        snapShotList.removeAt(page)
    }


    val isVisibleMenu = rememberSaveable {
        mutableStateOf(false)
    }

    val saveHandler: (title:String) -> Unit = { title ->

        val id = System.currentTimeMillis()

        viewModel.onEvent(
            WriteMemoViewModel.Event.UploadMemo(
                id = id,
                isLock = isLock,
                isMark = isMark,
                selectedTagArrayList = selectedTagArray.value,
                title = title,
                location = currentLocation
            )
        )

        channel.trySend(snackbarChannelList.first {
            it.channelType == SnackBarChannelType.MEMO_SAVE
        }.channel)


        snapShotList.clear()
        isLock = false
        isMark = false
        isMapClear = true
        selectedTagArray.value = arrayListOf()

    }


    LaunchedEffect(key1 = viewModel ){
        context.getDeviceLocation {it?.let {
            viewModel.onEvent(WriteMemoViewModel.Event.SetDeviceLocation(it))
        }}
    }

        BottomSheetScaffold(
            modifier = Modifier.statusBarsPadding(),
            scaffoldState = scaffoldState,
            sheetContent = {
                MemoDataContainer(onEvent = viewModel::onEvent, deleteHandle = deleteSnapshotHandle, channel = channel)
            },
            sheetPeekHeight = 110.dp,
            sheetContainerColor = Color.White,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { padding ->


            Box(Modifier.fillMaxSize()) {


                GoogleMap(
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = uiSettings,
                    onMapLongClick = onMapLongClickHandler ,
                    onMyLocationButtonClick = {
                        context.getDeviceLocation {it?.let {
                            viewModel.onEvent(WriteMemoViewModel.Event.SetDeviceLocation(it))
                        }}
                        return@GoogleMap true
                    }

                ) {


                    MapEffect(key1 = isSnapShot, key2 =  isMapClear){map ->
                        if(isSnapShot) {
                            map.snapshot {bitmap ->
                                val filePath = FileManager.getFilePath(context , FileManager.Companion.OUTPUTFILE.IMAGE )
                                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(filePath))
                                snapShotList.add(filePath.toUri())
                                viewModel.onEvent(WriteMemoViewModel.Event.SetSnapShot(snapShotList.toList()))
                                isSnapShot = false

                                channel.trySend(snackbarChannelList.first {
                                    it.channelType == SnackBarChannelType.SNAPSHOT_RESULT
                                }.channel)

                            }
                        }

                        if(isMapClear){
                            map.clear()
                            isMapClear = false
                        }

                    }



                       polylineList.forEach {
                        Polyline(
                            clickable = !isDrawing,
                            points = it,
                            geodesic = true,
                            color = Color.Yellow,
                            width = 20F,
                            onClick = { polyline ->
                                //isPressed.value = true
                                hapticProcessing()
                                polylineList.remove(polyline.points)
                                polylineListR.remove(polyline.points)
                            }

                        )
                    }

                    if(currentPolyline.isNotEmpty()){
                        Polyline(
                            points = currentPolyline.toList(),
                            geodesic = true,
                            color = Color.Red,
                            width = 5F,
                        )
                    }

                    Marker(
                        state = markerState,
                        title = "lat/lng:(${String.format("%.5f", markerState.position.latitude)},${String.format("%.5f", markerState.position.longitude)})",
                    )

                }

                if(isDrawing){
                    Canvas(modifier = Modifier
                        .fillMaxSize()
                        .pointerInteropFilter { event ->
                            addPolyline(event)
                            true
                        }
                    ){}
                }

                ScaleBar( modifier = Modifier.align(Alignment.TopStart),
                    cameraPositionState = cameraPositionState )


                Row(
                    modifier = Modifier
                        .align(alignmentSaveMenuList)
                        .clip(RoundedCornerShape(6.dp))
                        .background(color = Color.LightGray.copy(alpha = 0.7f))) {

                    SaveMenuList.forEach {
                        AnimatedVisibility(visible = isVisibleMenu.value,
                        ) {

                            androidx.compose.material3.IconButton(
                                onClick = {
                                    //isPressed.value = true
                                    hapticProcessing()
                                when(it){
                                    SaveMenu.CLEAR -> {
                                        channel.trySend(snackbarChannelList.first {
                                            it.channelType == SnackBarChannelType.MEMO_CLEAR_REQUEST
                                        }.channel)
                                    }
                                    SaveMenu.SAVE -> {
                                        if(snapShotList.isEmpty()) {  isSnapShot = true   }
                                        isAlertDialog.value = true

                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = it.getDesc().first,
                                    contentDescription = it.name,
                                )
                            }

                        }
                    }


                }


                Column(
                    modifier = Modifier
                        .align(alignmentMyLocation)
                        .clip(RoundedCornerShape(6.dp))
                        .background(color = Color.LightGray.copy(alpha = 0.7f))) {

                    androidx.compose.material3.IconButton(

                        onClick = {
                            //isPressed.value = true
                            hapticProcessing()
                            context.getDeviceLocation {it?.let {
                                viewModel.onEvent(WriteMemoViewModel.Event.SetDeviceLocation(it))
                                cameraPositionState.position = defaultCameraPosition
                                markerState.position = currentLocation.toLatLng()

                            }}


                        }
                    ) {
                        Icon(
                            modifier = Modifier.scale(1f),
                            imageVector = Icons.Outlined.GpsFixed,
                            contentDescription = "My Location",
                        )
                    }

                }


                Column(
                    modifier = Modifier
                        .align(alignmentCreateMenuList)
                        .clip(RoundedCornerShape(6.dp))
                        .background(color = Color.LightGray.copy(alpha = 0.7f))){
                    CreateMenuList.forEach {
                        AnimatedVisibility(visible = isVisibleMenu.value,
                        ) {
                            androidx.compose.material3.IconButton(
                                onClick = {
                                    //isPressed.value = true
                                    hapticProcessing()
                                when(it){
                                    CreateMenu.SNAPSHOT -> {
                                        isSnapShot = true
                                    }
                                    CreateMenu.RECORD -> {
                                        viewModel.onEvent(
                                            WriteMemoViewModel.Event.ToRoute(
                                                navController, it.getDesc().second?: ""))
                                    }
                                    CreateMenu.CAMERA -> {
                                        viewModel.onEvent(
                                            WriteMemoViewModel.Event.ToRoute(
                                                navController, it.getDesc().second?: ""))
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = it.getDesc().first,
                                    contentDescription = it.name,
                                )
                            }
                        }
                    }
                }


                Row(
                    modifier = Modifier
                        .align(alignmentDrawingMenuList)
                        .clip(RoundedCornerShape(6.dp))
                        .background(color = Color.LightGray.copy(alpha = 0.7f))) {

                    DrawingMenuList.forEach {
                        AnimatedVisibility(visible = isVisibleMenu.value,
                        ) {

                            androidx.compose.material3.IconButton(
                                onClick = {
                                    //isPressed.value = true
                                    hapticProcessing()
                                when(it){

                                    DrawingMenu.DrawEraser -> {
                                        isDrawing = !isDrawing
                                        viewModel.onEvent(WriteMemoViewModel.Event.UpdateIsDrawing(isDrawing))
                                        viewModel.onEvent(WriteMemoViewModel.Event.UpdateIsEraser(!isDrawing))
                                    }
                                }
                            }) {

                                val icon = when(it){

                                    DrawingMenu.DrawEraser -> {
                                        if (!isDrawing)  it.getDesc().first else it.getDesc().second?: it.getDesc().first
                                    }
                                }

                                Icon(
                                    imageVector = icon,
                                    contentDescription = it.name,
                                )
                            }

                        }
                    }

                }


                Row(
                    modifier = Modifier
                        .align(alignmentSettingMenuList)
                        .clip(RoundedCornerShape(6.dp))
                        .background(color = Color.LightGray.copy(alpha = 0.7f))) {

                    androidx.compose.material3.IconButton(
                        onClick = {
                            //isPressed.value = true
                            hapticProcessing()
                            isVisibleMenu.value = !isVisibleMenu.value
                        }
                    ) {
                        Icon(
                            modifier = Modifier.scale(1f),
                            imageVector = if (isVisibleMenu.value) Icons.Outlined.OpenWith else Icons.Outlined.Api,
                            contentDescription = "Menu",
                        )
                    }

                    SettingMenuList.forEach {
                        AnimatedVisibility(visible = isVisibleMenu.value,
                        ) {
                            androidx.compose.material3.IconButton(

                                onClick = {
                                    //isPressed.value = true
                                    hapticProcessing()
                                when(it){
                                    SettingMenu.SECRET -> {
                                        isLock = !isLock
                                        viewModel.onEvent(WriteMemoViewModel.Event.UpdateIsLock(isLock))
                                    }
                                    SettingMenu.MARKER -> {
                                        isMark = !isMark
                                        viewModel.onEvent(WriteMemoViewModel.Event.UpdateIsMarker(isMark))
                                    }
                                    SettingMenu.TAG -> {
                                        isTagDialog = !isTagDialog

                                    }
                                    else -> {}
                                }
                            }) {
                                val icon = when(it){
                                    SettingMenu.SECRET -> {
                                        if (isLock)  it.getDesc().first else it.getDesc().second?: it.getDesc().first
                                    }
                                    SettingMenu.MARKER -> {
                                        if (isMark)  it.getDesc().first else it.getDesc().second?: it.getDesc().first
                                    }
                                    else -> {
                                        it.getDesc().first
                                    }

                                }

                                Icon(
                                    imageVector = icon,
                                    contentDescription = it.name,
                                )
                            }
                        }
                    }


                    androidx.compose.material3.IconButton(

                        onClick = {

                            coroutineScope.launch {
                                if(scaffoldState.bottomSheetState.currentValue == SheetValue.Hidden
                                    || scaffoldState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded){
                                    scaffoldState.bottomSheetState.expand()
                                }else {
                                    scaffoldState.bottomSheetState.hide()
                                }

                            }
                        },

                    ) {
                        Icon(
                            modifier = Modifier,
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = "Data Container",

                            )
                    }



                }



                Column(modifier = Modifier
                    .align(alignmentMapTypeMenuList)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color = Color.LightGray.copy(alpha = 0.7f)),
                ) {
                    MapTypeMenuList.forEach {
                        AnimatedVisibility(visible = isVisibleMenu.value,
                        ) {
                            androidx.compose.material3.IconButton(

                                onClick = {
                                    //isPressed.value = true
                                    hapticProcessing()
                                val mapType = MapType.values().first { mapType ->
                                    mapType.name == it.name
                                }
                                mapProperties = mapProperties.copy(mapType = mapType)
                            }){

                                Icon(
                                    imageVector = it.getDesc().first,
                                    contentDescription = it.name,
                                )
                            }
                        }
                    }
                }


                Box(
                    modifier = Modifier
                        .clip(ShapeDefaults.ExtraSmall)
                        .background(color = Color.White.copy(alpha = 0.7f))
                        .align(Alignment.Center)
                        .fillMaxWidth(0.7f),
                    contentAlignment = Alignment.Center
                ){
                    AssistChipGroupView(
                        isVisible = isTagDialog,
                        setState = selectedTagArray,
                        getState = {
                            selectedTagArray.value = it
                        }
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                        ) {

                            Divider()

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {


                                IconButton(
                                    modifier = Modifier,
                                    onClick = {
                                        //isPressed.value = true
                                        hapticProcessing()
                                        tagInfoDataList.clear()
                                        selectedTagArray.value = arrayListOf()
                                        viewModel.onEvent(
                                            WriteMemoViewModel.Event.UpdateSelectedTags(
                                                arrayListOf()
                                            )
                                        )
                                    },
                                    content = {
                                        Icon(
                                            modifier = Modifier,
                                            imageVector = Icons.Outlined.Replay,
                                            contentDescription = "Clear"
                                        )
                                    }
                                )


                                IconButton(
                                    modifier = Modifier,
                                    onClick = {
                                        //isPressed.value = true
                                        hapticProcessing()
                                        isTagDialog = false
                                              },
                                    content = {
                                        Icon(
                                            modifier = Modifier,
                                            imageVector = Icons.Outlined.PublishedWithChanges,
                                            contentDescription = "Save"
                                        )
                                    }
                                )

                            }
                        }
                    }
                }





                if(isAlertDialog.value){
                    ConfirmDialog(
                        isAlertDialog = isAlertDialog,
                        onEvent = saveHandler )
                }


            }// Box



        }// BottomSheetScaffold



}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDialog(
    isAlertDialog: MutableState<Boolean> ,
    onEvent: (title:String) -> Unit,
){


    val isUsableHaptic = LocalUsableHaptic.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    fun hapticProcessing(){
        if(isUsableHaptic){
            coroutineScope.launch {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }


    val titleTimeStamp = SimpleDateFormat(
        "yyyy-MM-dd HH:mm",
        Locale.getDefault()
    ).format(System.currentTimeMillis())

    val titleText = rememberSaveable {
        mutableStateOf(titleTimeStamp)
    }

    val recognizerIntent = remember { recognizerIntent  }

    val startLauncherRecognizerIntent = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {

        if (it.resultCode == Activity.RESULT_OK) {

            val result =
                it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            titleText.value = titleText.value + result?.get(0).toString() + " "

        }
    }


    AlertDialog(
        onDismissRequest = {
            isAlertDialog.value = false
        }
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = ShapeDefaults.Large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Text(
                    modifier = Modifier.padding(bottom = 10.dp),
                    text = "Memo Save",
                    textAlign = TextAlign.Center,
                     style  = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    )

                    OutlinedTextField(
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth(),
                        singleLine = false,
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {

                                androidx.compose.material3.IconButton(
                                    modifier = Modifier,

                                    onClick = {
                                       // isPressed.value = true
                                        hapticProcessing()
                                        titleText.value  = ""
                                        startLauncherRecognizerIntent.launch(recognizerIntent())
                                    },
                                    content = {
                                        Icon(
                                            modifier = Modifier,
                                            imageVector = Icons.Outlined.Mic,
                                            contentDescription = "SpeechToText"
                                        )
                                    }
                                )



                                IconButton(
                                    onClick = {
                                        // isPressed.value = true
                                        hapticProcessing()
                                    titleText.value = ""
                                }) {
                                    Icon(
                                        imageVector = Icons.Rounded.HighlightOff,
                                        contentDescription = "Clear"
                                    )
                                }

                            }

                        },
                        value =  titleText.value,
                        onValueChange = {  titleText.value = it },
                        label = { androidx.compose.material3.Text("Title") },
                        shape = OutlinedTextFieldDefaults.shape,
                        keyboardActions = KeyboardActions.Default
                    )

                Text(text = "Save the written memo and clear the screen." )

                Row (modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,  ){


                    androidx.compose.material3.TextButton(

                        onClick = {
                            // isPressed.value = true
                            hapticProcessing()
                            isAlertDialog.value = false
                        }
                    ) {
                        Text("Cancel",
                            textAlign = TextAlign.Center,
                            style  = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp))
                    }


                    androidx.compose.material3.TextButton(

                        onClick = {
                            // isPressed.value = true
                            hapticProcessing()
                            isAlertDialog.value = false
                            onEvent(titleText.value)
                        }
                    ) {
                        Text("Confirm",
                            textAlign = TextAlign.Center,
                            style  = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        )
                    }


                }
            }

        }

    }

}


@Composable
fun MemoDataContainer(
    onEvent:((WriteMemoViewModel.Event)->Unit)? = null,
    deleteHandle:((index:Int)->Unit)? = null,
    channel:Channel<Int>? = null){


    val db = LocalLuckMemoDB.current
    val viewModel = remember {
        MemoContainerViewModel(
            repository = RepositoryProvider.getRepository().apply { database = db } ,
            user = if (onEvent == null ) MemoDataContainerUser.DetailMemoView else MemoDataContainerUser.WriteMemoView
        )
    }

    val isUsableHaptic = LocalUsableHaptic.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    fun hapticProcessing(){
        if(isUsableHaptic){
            coroutineScope.launch {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }

    val currentTabView = remember {
        mutableStateOf(WriteMemoDataType.SNAPSHOT)
    }

    val currentTabIndex =  remember {
        mutableStateOf(0)
    }

    BottomNavigation(
        backgroundColor = MaterialTheme.colors.background,
        modifier = Modifier  ) {


        WriteMemoDataTypeList.forEachIndexed { index, it ->
            BottomNavigationItem(
                icon = {
                    Icon(
                        imageVector = it.getDesc().second,
                        contentDescription = it.getDesc().first ,
                        tint = if( currentTabView.value ==  it) Color.Red else Color.Black
                    )
                },
                label = { Text(it.getDesc().first)  },
                selected = currentTabView.value ==  it,
                selectedContentColor =  if( currentTabView.value == it) Color.Red else Color.Black,
                onClick = {
                 //   isPressed.value = true
                    hapticProcessing()
                    currentTabView.value = it
                    currentTabIndex.value = index
                },
            )
        }
    }

    Row(
        modifier = Modifier
            .background(color = Color.White)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
            repeat(WriteMemoDataTypeList.count()) { iteration ->
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .fillMaxWidth(1f / (WriteMemoDataTypeList.count() - iteration))
                        .clip(CircleShape)
                        .background(if (currentTabIndex.value == iteration) Color.Red else Color.LightGray))
            }
    }

    val memoData:MutableState<MemoData?> = mutableStateOf(
        when (currentTabView.value){
            WriteMemoDataType.PHOTO ->  MemoData.Photo(dataList = viewModel.phothoList.collectAsState().value.toMutableList())
            WriteMemoDataType.AUDIOTEXT -> MemoData.AudioText(dataList = viewModel.audioTextList.collectAsState().value.toMutableList())
            WriteMemoDataType.VIDEO ->  MemoData.Video(dataList = viewModel.videoList.collectAsState().value.toMutableList())
            WriteMemoDataType.SNAPSHOT ->  MemoData.SnapShot(dataList = viewModel.snapShotList.collectAsState().value.toMutableList())
        }

    )


    val onDelete:((page:Int) -> Unit)  =   { page ->

            if (currentTabView.value ==  WriteMemoDataType.SNAPSHOT ) {
                deleteHandle?.let {
                    it (page)
                }
            }

            onEvent?.let {
                it(WriteMemoViewModel.Event.DeleteMemoItem(currentTabView.value, page))
            }

    }


        Column(modifier = Modifier
        ) {
            memoData.value?.let {
                PagerMemoDataView(item = it, onDelete = if(deleteHandle != null) onDelete else null, channel = channel)
            }
        }




}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerMemoDataView(item: MemoData, onDelete:((page:Int) -> Unit)? = null, channel:Channel<Int>? = null){


    val isUsableHaptic = LocalUsableHaptic.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    fun hapticProcessing(){
        if(isUsableHaptic){
            coroutineScope.launch {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }


    val pagerState =   rememberPagerState()

    val defaultData:Pair<String, Int> = when(item){
        is MemoData.Photo ->  Pair(WriteMemoDataType.PHOTO.name, item.dataList.size)
        is MemoData.AudioText -> Pair(WriteMemoDataType.AUDIOTEXT.name, item.dataList.size)
        is MemoData.SnapShot -> Pair(WriteMemoDataType.SNAPSHOT.name, item.dataList.size)
        is MemoData.Video -> Pair(WriteMemoDataType.VIDEO.name, item.dataList.size)
    }




        Column(modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)) {


            Box (
                modifier = Modifier.fillMaxWidth(),
                contentAlignment =Alignment.Center
            ){

                androidx.compose.material3.Text(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(color = Color.White)
                        .padding(top = 10.dp),
                    textAlign = TextAlign.Center,
                    text = defaultData.first,
                    style = MaterialTheme.typography.h6
                )

                onDelete?.let {

                    if(defaultData.second > 0) {
                        IconButton(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            onClick = {
                           //     isPressed.value = true
                                hapticProcessing()
                                    it(pagerState.currentPage)
                                    channel?.let {channel ->
                                        channel.trySend(snackbarChannelList.first {snackBarChannelData ->
                                            snackBarChannelData.channelType == SnackBarChannelType.ITEM_DELETE
                                        }.channel)
                                    }

                            },
                            content = {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete"
                                )
                            }
                        )


                    }



                }

            }


            Row(
                modifier = Modifier
                    .background(color = Color.White)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {

                repeat(defaultData.second) { iteration ->
                    val color =  if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                    Box( modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(10.dp) )
                }

            }

            if(defaultData.second > 0) {

                // .verticalScroll(state = scrollState) 사용시 ExoplayerCompose minimum size 가 된다.
                HorizontalPager(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.White),
                    pageCount = defaultData.second,
                    state = pagerState,
                ) { page ->

                    when (item) {
                        is MemoData.AudioText -> AudioTextView(data = item.dataList[page])
                        is MemoData.Photo -> ImageViewer(
                            data = (item.dataList[page]),
                            size = Size.ORIGINAL,
                            isZoomable = false
                        )
                        is MemoData.SnapShot -> ImageViewer(
                            data = (item.dataList[page]),
                            size = Size.ORIGINAL,
                            isZoomable = false
                        )
                        is MemoData.Video -> ExoplayerCompose(
                            uri = item.dataList[page],
                            isVisibleAmplitudes = false
                        )
                    }
                }

            } else {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .background(color = Color.LightGray))
            }


        } // Column



}




@Preview
@Composable
fun PrevWriteMemo(){
    val permissionsManager = PermissionsManager()
    val navController = rememberNavController()

    CompositionLocalProvider(LocalPermissionsManager provides permissionsManager) {


            GISMemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.onPrimary,
                    contentColor = MaterialTheme.colors.primary
                ) {
                    WriteMemoView(navController = navController)
                }
            }

    }


}



