package com.example.gismemo.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Point
import android.location.Location
import android.net.Uri
import android.speech.RecognizerIntent
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.HighlightOff
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
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
import com.example.gismemo.LocalUsableDarkMode
import com.example.gismemo.LocalUsableHaptic
import com.example.gismemo.R
import com.example.gismemo.data.RepositoryProvider
import com.example.gismemo.db.HHmmss
import com.example.gismemo.db.LocalLuckMemoDB
import com.example.gismemo.db.UnixTimeToString
import com.example.gismemo.db.entity.CURRENTLOCATION_TBL
import com.example.gismemo.model.*
import com.example.gismemo.navigation.GisMemoDestinations
import com.example.gismemo.shared.composables.*
import com.example.gismemo.shared.utils.FileManager
import com.example.gismemo.shared.utils.SnackBarChannelType
import com.example.gismemo.shared.utils.snackbarChannelList
import com.example.gismemo.ui.theme.GISMemoTheme
import com.example.gismemo.viewmodel.MemoContainerViewModel
import com.example.gismemo.viewmodel.WriteMemoViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.google.maps.android.compose.widgets.ScaleBar
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


enum class DrawingMenu {
Draw,Swipe,Eraser
}

val DrawingMenuList = listOf(
    DrawingMenu.Draw,
    DrawingMenu.Swipe,
    DrawingMenu.Eraser
)

fun DrawingMenu.getDesc():Pair<ImageVector, Color> {
    return when(this){
        DrawingMenu.Draw -> {
            Pair( Icons.Outlined.Draw , Color.Red)
        }
        DrawingMenu.Swipe -> {
            Pair( Icons.Outlined.Swipe ,  Color.Red)
        }
        DrawingMenu.Eraser -> {
            Pair( Icons.Outlined.Toll ,  Color.Red)
        }

    }
}

enum class MapTypeMenu {
    NORMAL,TERRAIN,HYBRID
}

val MapTypeMenuList = listOf(
    MapTypeMenu.NORMAL,
    MapTypeMenu.TERRAIN,
    MapTypeMenu.HYBRID,

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


@SuppressLint("SuspiciousIndentation", "MissingPermission")
@OptIn(
    ExperimentalMaterial3Api::class,
    MapsComposeExperimentalApi::class, ExperimentalComposeUiApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun WriteMemoView(navController: NavController ){


    val permissions = listOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val multiplePermissionsState = rememberMultiplePermissionsState( permissions)
    CheckPermission(multiplePermissionsState = multiplePermissionsState)

    var isGranted by mutableStateOf(true)
    permissions.forEach { chkPermission ->
        isGranted =  isGranted && multiplePermissionsState.permissions.find { it.permission == chkPermission }?.status?.isGranted
            ?: false
    }

    PermissionRequiredCompose(
        isGranted = isGranted,
        multiplePermissions = permissions ,
        viewType = PermissionRequiredComposeFuncName.MemoMap
    ) {


        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val db = LocalLuckMemoDB.current
        val viewModel = remember {
            WriteMemoViewModel(
                repository = RepositoryProvider.getRepository().apply { database = db })
        }

        val isUsableHaptic = LocalUsableHaptic.current
        val hapticFeedback = LocalHapticFeedback.current
        val coroutineScope = rememberCoroutineScope()
        fun hapticProcessing() {
            if (isUsableHaptic) {
                coroutineScope.launch {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
        }

        val fusedLocationProviderClient = remember {
            LocationServices.getFusedLocationProviderClient(context)
        }

        var currentLocation by remember {
            mutableStateOf(LatLng(0.0, 0.0))
        }

        var location: Location? by remember {
            mutableStateOf(null)
        }


        var isGoCurrentLocation by remember { mutableStateOf(false) }
        val isUsableDarkMode = LocalUsableDarkMode.current
        var isDarkMode by remember { mutableStateOf(isUsableDarkMode) }
        var mapTypeIndex by rememberSaveable { mutableStateOf(0) }
        val markerState = MarkerState(position = currentLocation)
        val defaultCameraPosition = CameraPosition.fromLatLngZoom(currentLocation, 16f)
        val cameraPositionState = CameraPositionState(position = defaultCameraPosition)

        var mapProperties by remember {
            mutableStateOf(
                MapProperties(
               //     mapType = MapType.NORMAL,
                    mapType =    MapType.values().first { mapType ->
                        mapType.name == MapTypeMenuList[mapTypeIndex].name
                    },
                    isMyLocationEnabled = true,
                    mapStyleOptions = if(isDarkMode) {
                         MapStyleOptions.loadRawResourceStyle(
                            context,
                            R.raw.mapstyle_night
                        )
                    } else { null }
                )
            )
        }

        val uiSettings by remember { mutableStateOf(MapUiSettings(zoomControlsEnabled = false)) }

        val sheetState = SheetState(
            skipPartiallyExpanded = false,
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )





        val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
        var isTagDialog by rememberSaveable { mutableStateOf(false) }
        val isAlertDialog = rememberSaveable { mutableStateOf(false) }
        var isSnapShot by remember { mutableStateOf(false) }
        var isDefaultSnapShot by rememberSaveable { mutableStateOf(false) }
        var isMapClear by remember { mutableStateOf(false) }
        val currentPolyline = mutableStateListOf<LatLng>()
        var isDrawing by rememberSaveable { mutableStateOf(false) }

        val selectedTagArray: MutableState<ArrayList<Int>> =
            rememberSaveable { mutableStateOf(arrayListOf()) }
        var isLock by rememberSaveable { mutableStateOf(false) }
        var isMark by rememberSaveable { mutableStateOf(false) }


// Not recompose rememberSaveable 에 mutableStatelist 는

        val polylineList: SnapshotStateList<List<LatLng>>
        val polylineListR: MutableList<DrawingPolyline> = rememberSaveable { mutableListOf() }
        val snapShotList: MutableList<Uri> = rememberSaveable { mutableListOf() }

        val alignmentSaveMenuList: Alignment
        val alignmentMyLocation: Alignment
        val alignmentCreateMenuList: Alignment
        val alignmentSettingMenuList: Alignment
        val alignmentDrawingMenuList: Alignment
        val alignmentMapTypeMenuList: Alignment

        val configuration = LocalConfiguration.current
        when (configuration.orientation) {

            Configuration.ORIENTATION_PORTRAIT -> {
                polylineList = polylineListR.toMutableStateList()
                alignmentSaveMenuList = Alignment.TopCenter
                alignmentMyLocation = Alignment.TopStart
                alignmentCreateMenuList = Alignment.CenterStart
                alignmentSettingMenuList = Alignment.BottomStart
                alignmentDrawingMenuList = Alignment.BottomEnd
                alignmentMapTypeMenuList = Alignment.CenterEnd

            }
            else -> {
                polylineList = polylineListR.toMutableStateList()
                alignmentSaveMenuList = Alignment.TopCenter
                alignmentMyLocation = Alignment.TopStart
                alignmentCreateMenuList = Alignment.CenterStart
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


                val result = snackbarHostState.showSnackbar(
                    message = context.resources.getString( channelData.message),
                    actionLabel = channelData.actionLabel,
                    withDismissAction = channelData.withDismissAction,
                    duration = channelData.duration
                )
                when (result) {
                    SnackbarResult.ActionPerformed -> {
                        hapticProcessing()
                        when (channelData.channelType) {
                            SnackBarChannelType.MEMO_CLEAR_REQUEST -> {

                                viewModel.onEvent(WriteMemoViewModel.Event.InitMemo)
                                snapShotList.clear()
                                isLock = false
                                isMark = false
                                isMapClear = true
                                selectedTagArray.value = arrayListOf()

                                channel.trySend(snackbarChannelList.first {
                                    it.channelType == SnackBarChannelType.MEMO_CLEAR_RESULT
                                }.channel)
                            }
                            else -> {

                            }
                        }


                    }
                    SnackbarResult.Dismissed -> {
                        hapticProcessing()
                    }
                }
            }
        }


        LaunchedEffect(key1 = currentLocation) {
            if (currentLocation == LatLng(0.0, 0.0)) {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(context.mainExecutor) { task ->
                    if (task.isSuccessful && task.result != null) {
                        location = task.result
                        currentLocation = task.result.toLatLng()
                    }
                }
            }
        }




        val onMapLongClickHandler: (LatLng) -> Unit = {
            hapticProcessing()
            currentLocation = it
        }

        val addPolyline: (MotionEvent) -> Unit = { event ->
            lifecycleOwner.lifecycleScope.launch {
                cameraPositionState.projection?.let {

                    val latLng =
                        it.fromScreenLocation(Point(Math.round(event.x), Math.round(event.y)))

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


        val deleteSnapshotHandle: ((page: Int) -> Unit) = { page ->
            snapShotList.removeAt(page)
        }


        val isVisibleMenu = rememberSaveable {
            mutableStateOf(false)
        }

        val checkEnableLocationService:()-> Unit = {
            fusedLocationProviderClient.lastLocation.addOnCompleteListener(context.mainExecutor) { task ->
                if (!task.isSuccessful || task.result == null) {
                    channel.trySend(snackbarChannelList.first {
                        it.channelType == SnackBarChannelType.LOCATION_SERVICE_DISABLE
                    }.channel)
                }
            }
        }

        val saveHandler: (title: String) -> Unit = { title ->
            val desc = String.format( context.resources.getString(R.string.memo_desc),
                viewModel.snapShotList.value.size,
                viewModel.audioTextList.value.size,
                viewModel.phothoList.value.size,
                viewModel.videoList.value.size
            )

            var snippets = ""
            selectedTagArray.value.forEach {
                 snippets = "${snippets} #${  context.resources.getString( tagInfoDataList[it].name)   }"
            }


            val id = System.currentTimeMillis()

                viewModel.onEvent(
                    WriteMemoViewModel.Event.UploadMemo(
                        id = id,
                        isLock = isLock,
                        isMark = isMark,
                        selectedTagArrayList = selectedTagArray.value,
                        title = title,
                        desc = desc,
                        snippets = snippets,
                        location = CURRENTLOCATION_TBL(dt=id, latitude = currentLocation.latitude.toFloat(), longitude = currentLocation.longitude.toFloat(), 0f)
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


        BottomSheetScaffold(
            modifier = Modifier.statusBarsPadding(),
            scaffoldState = scaffoldState,
            sheetContent = {
                MemoDataContainer(
                    onEvent = viewModel::onEvent,
                    deleteHandle = deleteSnapshotHandle,
                    channel = channel
                )
            },
            sheetShape = ShapeDefaults.Small,
            sheetPeekHeight = 110.dp,
            sheetContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
            sheetContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { padding ->


            Box(Modifier.fillMaxSize()) {


                GoogleMap(
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = uiSettings,
                    onMapLongClick = onMapLongClickHandler,
                    onMyLocationButtonClick = {
                        checkEnableLocationService.invoke()
                        return@GoogleMap false

                    }
                ) {


                    MapEffect(key1 = isSnapShot, key2 = isMapClear, key3 = isGoCurrentLocation,) { map ->
                        if (isSnapShot) {
                            map.snapshot { bitmap ->
                                val filePath = FileManager.getFilePath(
                                    context,
                                    FileManager.Companion.OUTPUTFILE.IMAGE
                                )
                                bitmap?.compress(
                                    Bitmap.CompressFormat.JPEG,
                                    100,
                                    FileOutputStream(filePath)
                                )
                                snapShotList.add(filePath.toUri())
                                viewModel.onEvent(WriteMemoViewModel.Event.SetSnapShot(snapShotList.toList()))
                                isSnapShot = false

                                if(!isDefaultSnapShot) {
                                    channel.trySend(snackbarChannelList.first {
                                        it.channelType == SnackBarChannelType.SNAPSHOT_RESULT
                                    }.channel)
                                }
                            }
                        }

                        if (isMapClear) {
                            map.clear()
                            isMapClear = false
                        }

                        if(isGoCurrentLocation) {
                            map.animateCamera( CameraUpdateFactory.newLatLngZoom( currentLocation, 16F) )
                            isGoCurrentLocation = false
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
                                hapticProcessing()
                                polylineList.remove(polyline.points)
                                polylineListR.remove(polyline.points)
                            }

                        )
                    }

                    if (currentPolyline.isNotEmpty()) {
                        Polyline(
                            points = currentPolyline.toList(),
                            geodesic = true,
                            color = Color.Red,
                            width = 5F,
                        )
                    }

                    Marker(
                        state = markerState,
                        title = "lat/lng:(${
                            String.format(
                                "%.5f",
                                markerState.position.latitude
                            )
                        },${String.format("%.5f", markerState.position.longitude)})",
                    )

                }




                if (isDrawing) {
                    Canvas(modifier = Modifier
                        .fillMaxSize()
                        .pointerInteropFilter { event ->
                            addPolyline(event)
                            true
                        }
                    ) {}
                }


                ScaleBar(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 50.dp, end = 10.dp),
                    cameraPositionState = cameraPositionState
                )


                Row(
                    modifier = Modifier
                        .align(alignmentSaveMenuList)
                        .padding(2.dp)
                        .background(
                            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                            shape = ShapeDefaults.ExtraSmall
                        )
                ) {

                    SaveMenuList.forEach {
                        AnimatedVisibility(
                            visible = isVisibleMenu.value,
                        ) {

                            IconButton(
                                onClick = {
                                    hapticProcessing()
                                    when (it) {
                                        SaveMenu.CLEAR -> {
                                            channel.trySend(snackbarChannelList.first {
                                                it.channelType == SnackBarChannelType.MEMO_CLEAR_REQUEST
                                            }.channel)
                                        }
                                        SaveMenu.SAVE -> {

                                            if (snapShotList.isEmpty()) {
                                                isSnapShot = true
                                                isDefaultSnapShot = true
                                            }

                                                viewModel.onEvent(
                                                    WriteMemoViewModel.Event.SearchWeather(currentLocation)
                                                )



                                            isAlertDialog.value = true
                                        }
                                    }
                                }
                            ) {
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
                        .padding(2.dp)
                        .background(
                            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                            shape = ShapeDefaults.ExtraSmall
                        )
                ) {

                    AnimatedVisibility(
                        visible = isVisibleMenu.value,
                    ) {

                        IconButton(
                            onClick = {
                                hapticProcessing()
                                isGoCurrentLocation = true
                            }
                        ) {
                            Icon(
                                modifier = Modifier.scale(1f),
                                imageVector = Icons.Outlined.ModeOfTravel,
                                contentDescription = "ModeOfTravel",
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = isVisibleMenu.value,
                    ) {

                        IconButton(
                            enabled = if(mapTypeIndex == 0) true else false,
                            onClick = {
                                hapticProcessing()
                                isDarkMode = !isDarkMode

                                if (isDarkMode) {
                                    mapProperties = mapProperties.copy(
                                        mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                                            context,
                                            R.raw.mapstyle_night
                                        )
                                    )
                                } else {
                                    mapProperties = mapProperties.copy(mapStyleOptions = null)
                                }
                            }
                        ) {
                            Icon(
                                modifier = Modifier.scale(1f),
                                imageVector = if (isDarkMode) Icons.Outlined.BedtimeOff else Icons.Outlined.DarkMode,
                                contentDescription = "DarkMode",
                            )
                        }
                    }

                }


                Column(
                    modifier = Modifier
                        .align(alignmentCreateMenuList)
                        .padding(2.dp)
                        .background(
                            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                            shape = ShapeDefaults.ExtraSmall
                        )
                ) {
                    CreateMenuList.forEach {
                        AnimatedVisibility(
                            visible = isVisibleMenu.value,
                        ) {
                            IconButton(
                                onClick = {
                                    hapticProcessing()
                                    when (it) {
                                        CreateMenu.SNAPSHOT -> {
                                            isSnapShot = true
                                        }
                                        CreateMenu.RECORD -> {
                                            viewModel.onEvent(
                                                WriteMemoViewModel.Event.ToRoute(
                                                    navController, it.getDesc().second ?: ""
                                                )
                                            )
                                        }
                                        CreateMenu.CAMERA -> {
                                            viewModel.onEvent(
                                                WriteMemoViewModel.Event.ToRoute(
                                                    navController, it.getDesc().second ?: ""
                                                )
                                            )
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
                        .padding(2.dp)
                        .background(
                            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                            shape = ShapeDefaults.ExtraSmall
                        )
                ) {


                    DrawingMenuList.forEach {
                        AnimatedVisibility(
                            visible = isVisibleMenu.value,
                        ) {

                            IconButton(
                                onClick = {
                                    hapticProcessing()
                                    when (it) {

                                        DrawingMenu.Draw -> {
                                            isDrawing = true
                                            viewModel.onEvent(
                                                WriteMemoViewModel.Event.UpdateIsDrawing(
                                                    isDrawing
                                                )
                                            )

                                        }

                                        DrawingMenu.Swipe -> {
                                            isDrawing = false
                                            viewModel.onEvent(
                                                WriteMemoViewModel.Event.UpdateIsEraser(
                                                    !isDrawing
                                                )
                                            )
                                        }

                                        DrawingMenu.Eraser -> {
                                            polylineList.clear()
                                            polylineListR.clear()
                                            isMapClear = true
                                        }


                                    }
                                }) {


                                val iconColor:Color?  = when (it) {
                                    DrawingMenu.Draw -> {
                                        if(isDrawing) it.getDesc().second else null
                                    }
                                    DrawingMenu.Swipe -> {
                                        if(!isDrawing) it.getDesc().second else null
                                    }
                                    else -> {
                                        null
                                    }

                                }


                                Icon(
                                    imageVector =  it.getDesc().first,
                                    contentDescription = it.name,
                                    tint = iconColor?: androidx.compose.material3.MaterialTheme.colorScheme.secondary
                                )
                            }

                        }
                    }

                }


                Row(
                    modifier = Modifier
                        .align(alignmentSettingMenuList)
                        .padding(2.dp)
                        .background(
                            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                            shape = ShapeDefaults.ExtraSmall
                        )
                ) {

                    IconButton(
                        onClick = {
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
                        AnimatedVisibility(
                            visible = isVisibleMenu.value,
                        ) {
                            IconButton(

                                onClick = {
                                    hapticProcessing()
                                    when (it) {
                                        SettingMenu.SECRET -> {
                                            isLock = !isLock
                                            viewModel.onEvent(
                                                WriteMemoViewModel.Event.UpdateIsLock(
                                                    isLock
                                                )
                                            )
                                        }
                                        SettingMenu.MARKER -> {
                                            isMark = !isMark
                                            viewModel.onEvent(
                                                WriteMemoViewModel.Event.UpdateIsMarker(
                                                    isMark
                                                )
                                            )
                                        }
                                        SettingMenu.TAG -> {
                                            isTagDialog = !isTagDialog

                                        }
                                    }
                                }) {
                                val icon = when (it) {
                                    SettingMenu.SECRET -> {
                                        if (isLock) it.getDesc().first else it.getDesc().second
                                            ?: it.getDesc().first
                                    }
                                    SettingMenu.MARKER -> {
                                        if (isMark) it.getDesc().first else it.getDesc().second
                                            ?: it.getDesc().first
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

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                if (scaffoldState.bottomSheetState.currentValue == SheetValue.Hidden
                                    || scaffoldState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded
                                ) {
                                    scaffoldState.bottomSheetState.expand()
                                } else {
                                    scaffoldState.bottomSheetState.hide()
                                }
                            }
                        },

                        ) {
                        Icon(
                            modifier = Modifier,
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = "Data Container"
                        )
                    }


                }



                Column(
                    modifier = Modifier
                        .align(alignmentMapTypeMenuList)
                        .padding(2.dp)
                        .background(
                            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                            shape = ShapeDefaults.ExtraSmall
                        )
                ) {
                    //MapTypeMenuList.forEach {
                    MapTypeMenuList.forEachIndexed { index, it ->
                        AnimatedVisibility(
                            visible = isVisibleMenu.value,
                        ) {
                            IconButton(
                                onClick = {
                                    hapticProcessing()


                                        val mapType = MapType.values().first { mapType ->
                                            mapType.name == it.name
                                        }
                                        mapProperties = mapProperties.copy(mapType = mapType)
                                        mapTypeIndex = index


                                }) {

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
                            .padding(10.dp)
                            .background(
                                color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                                shape = ShapeDefaults.ExtraSmall)
                            .align(Alignment.Center)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {

                        AnimatedVisibility(visible = isTagDialog) {

                            AssistChipGroupView(
                                isVisible = isTagDialog,
                                setState = selectedTagArray,
                            ) {

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.Center)
                                ) {

                                    Divider()

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {

                                        IconButton(
                                            modifier = Modifier,
                                            onClick = {
                                                isTagDialog = false
                                                hapticProcessing()
                                                selectedTagArray.value.clear()
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
                                                hapticProcessing()
                                                isTagDialog = false
                                                selectedTagArray.value.clear()
                                                tagInfoDataList.forEachIndexed { index, tagInfoData ->
                                                    if (tagInfoData.isSet.value) {
                                                        selectedTagArray.value.add(index)
                                                    }
                                                }


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

                    }








                if (isAlertDialog.value) {
                    ConfirmDialog(
                        isAlertDialog = isAlertDialog,
                        cancelSnapShot = {
                            if(isDefaultSnapShot) {
                                viewModel.onEvent(
                                    WriteMemoViewModel.Event.DeleteMemoItem( WriteMemoDataType.SNAPSHOT, 0)
                                )
                                snapShotList.clear()
                                isDefaultSnapShot = false
                            }
                        },
                        onEvent = saveHandler
                    )
                }

            }// Box

        }// BottomSheetScaffold

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDialog(
    isAlertDialog: MutableState<Boolean> ,
    cancelSnapShot:()->Unit,
    onEvent: (title:String) -> Unit,
) {

    val context = LocalContext.current
    val isUsableHaptic = LocalUsableHaptic.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    fun hapticProcessing() {
        if (isUsableHaptic) {
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


    val recognizerIntent = remember { recognizerIntent }

    val startLauncherRecognizerIntent = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {

        if (it.resultCode == Activity.RESULT_OK) {

            val result =
                it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            titleText.value = titleText.value + result?.get(0).toString() + " "

        }
    }

    // .background( color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp))

    AlertDialog(
        onDismissRequest = {
            isAlertDialog.value = false
            cancelSnapShot.invoke()
        }
    ) {
/*
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            color = androidx.compose.material3.MaterialTheme.colorScheme.surface,
            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
            shape = ShapeDefaults.Large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {

 */



            Column(
                modifier = Modifier
                    .background(
                        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                    shape = ShapeDefaults.ExtraSmall)
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Text(
                    modifier = Modifier.padding(top = 20.dp),
                    text = context.resources.getString(R.string.writeMemo_AlertDialog_Title),
                    textAlign = TextAlign.Center,
                    style =  androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth(),
                    singleLine = false,
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            IconButton(
                                modifier = Modifier,
                                onClick = {
                                    hapticProcessing()
                                    titleText.value = ""
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
                                    hapticProcessing()
                                    titleText.value = ""
                                }) {
                                Icon(
                                    imageVector = Icons.Rounded.Replay,
                                    contentDescription = "Clear"
                                )
                            }

                        }
                    },
                    value = titleText.value,
                    onValueChange = { titleText.value = it },
                    label = { androidx.compose.material3.Text(context.resources.getString(R.string.writeMemo_AlertDialog_title_label)) },
                    shape = OutlinedTextFieldDefaults.shape,
                    keyboardActions = KeyboardActions.Default
                )

                Text(
                    text = context.resources.getString(R.string.writeMemo_AlertDialog_desc),
                    style =  androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {


                    TextButton(

                        onClick = {
                            hapticProcessing()
                            isAlertDialog.value = false
                            cancelSnapShot.invoke()
                        }
                    ) {
                        Text(
                            context.resources.getString(R.string.writeMemo_AlertDialog_Cancel),
                            textAlign = TextAlign.Center,
                            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                    }


                    TextButton(

                        onClick = {
                            hapticProcessing()
                            isAlertDialog.value = false
                            onEvent(titleText.value)
                        }
                    ) {
                        Text(
                            context.resources.getString(R.string.writeMemo_AlertDialog_Confirm),
                            textAlign = TextAlign.Center,
                            style  = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                    }


                }
            }

    //    }

    }



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



