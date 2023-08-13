package com.example.gismemo.view

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.example.gismemo.LocalUsableHaptic
import com.example.gismemo.data.RepositoryProvider
import com.example.gismemo.db.LocalLuckMemoDB
import com.example.gismemo.db.entity.MEMO_TBL
import com.example.gismemo.db.entity.toCURRENTWEATHER_TBL
import com.example.gismemo.db.entity.toLatLng
import com.example.gismemo.shared.utils.FileManager
import com.example.gismemo.shared.utils.SnackBarChannelType
import com.example.gismemo.shared.utils.snackbarChannelList
import com.example.gismemo.viewmodel.CameraViewModel
import com.example.gismemo.viewmodel.DetailMemoViewModel
import com.example.gismemo.viewmodel.WriteMemoViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.maps.android.compose.widgets.ScaleBar
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailMemoView(navController: NavController, id:Long){

    val db = LocalLuckMemoDB.current
    val viewModel = remember {
        DetailMemoViewModel (repository = RepositoryProvider.getRepository().apply { database = db }  )
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





    //--------------
    LaunchedEffect(key1 = viewModel, ){
        viewModel.onEvent(DetailMemoViewModel.Event.SetMemo(id = id))
        viewModel.onEvent(DetailMemoViewModel.Event.SetWeather(id = id))
        viewModel.onEvent(DetailMemoViewModel.Event.SetTags(id = id))
        viewModel.onEvent(DetailMemoViewModel.Event.SetFiles(id = id))
    }
    //--------------



    val memo =  viewModel._memo.collectAsState().value

    val memoPosition:LatLng
       =  if(memo == null)  {
            LatLng(0.0, 0.0)
            }else {
                LatLng(memo.latitude.toDouble(),memo.longitude.toDouble())
            }

    val markerState = MarkerState( position = memoPosition   )

    val defaultCameraPosition = CameraPosition.fromLatLngZoom(memoPosition, 16f)

    var cameraPositionState = CameraPositionState(position = defaultCameraPosition)

    var mapProperties by remember {  mutableStateOf(
        MapProperties(mapType = MapType.NORMAL,   isMyLocationEnabled = false) )  }

    val uiSettings by remember {  mutableStateOf(
        MapUiSettings(zoomControlsEnabled = false ) ) }

    var isExpanded by remember { mutableStateOf(false) }

    val sheetState = SheetState(
        skipPartiallyExpanded = false,
        initialValue = SheetValue.Hidden,
        skipHiddenState = false)


    val scaffoldState =  rememberBottomSheetScaffoldState( bottomSheetState = sheetState )

    var isTagDialog by  rememberSaveable { mutableStateOf(false) }

    var isLock by rememberSaveable { mutableStateOf(false) }
    var isMark by rememberSaveable { mutableStateOf(false) }


    LaunchedEffect(key1 = memo){
        memo?.let {
            isLock = it.isSecret
            isMark = it.isPin
        }

    }
    val isVisibleMenu = rememberSaveable {
        mutableStateOf(false)
    }

    val selectedTagArray =  mutableStateOf(viewModel._tagArrayList.collectAsState().value)

    val weatherData = viewModel._weather.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val channel = remember { Channel<Int>(Channel.CONFLATED) }
    LaunchedEffect(channel) {
        channel.receiveAsFlow().collect { index ->
            val channelData = snackbarChannelList.first {
                it.channel == index
            }

            //----------
            val message = when(channelData.channelType){
                SnackBarChannelType.LOCK_CHANGE -> {
                    channelData.message +  if (isLock) " [ 설정 ] "  else " [ 해지 ] "
                }
                SnackBarChannelType.MARKER_CHANGE -> {
                    channelData.message + if (isMark) " [ 설정 ] "   else " [ 해지 ] "
                }
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
                        else -> {}
                    }
                }
                SnackbarResult.Dismissed -> {
                }
            }
        }
    }


    BottomSheetScaffold(
        modifier = Modifier.statusBarsPadding(),
        scaffoldState = scaffoldState,
        sheetContent = {
            MemoDataContainer()
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
                onMapLongClick = {}  ) {


                Marker(
                    state = markerState,
                    title = "lat/lng:(${String.format("%.5f", markerState.position.latitude)},${String.format("%.5f", markerState.position.longitude)})",
                )

            }

            weatherData.value?.let {
                    WeatherView(
                        modifier = Modifier
                            .width(400.dp)
                            .align(Alignment.TopCenter)
                            .padding(20.dp)
                            .background(
                                color = Color.Yellow.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(6.dp)
                            ) ,
                        item =  it.toCURRENTWEATHER_TBL()
                    )
            }

            memo?.let {
                Box(
                    modifier = Modifier
                        .width(400.dp)
                        .padding(top = 200.dp)
                        .padding(horizontal = 20.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            color = Color.Yellow.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clip(RoundedCornerShape(8.dp)),

                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                    ) {

                        Text(it.title)
                        Text(it.desc)
                        if(it.snippets.isNotEmpty()) {
                            Text(it.snippets)
                        }
                    }
                }

            }




            ScaleBar( modifier = Modifier.align(Alignment.TopStart),
                cameraPositionState = cameraPositionState )



            Column(modifier = Modifier
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(6.dp))
                .background(color = Color.LightGray.copy(alpha = 0.7f)),
            ) {
                MapTypeMenuList.forEach {
                    AnimatedVisibility(visible = isVisibleMenu.value,
                    ) {
                        androidx.compose.material3.IconButton(
                            onClick = {
                               // isPressed.value = true
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


            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color = Color.LightGray.copy(alpha = 0.7f))) {

                androidx.compose.material3.IconButton(
                    onClick = {
                        // isPressed.value = true
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
                                // isPressed.value = true
                                hapticProcessing()
                            when(it){
                                SettingMenu.SECRET -> {
                                    isLock = !isLock
                                    memo?.let {
                                        viewModel.onEvent(DetailMemoViewModel.Event.UpdateIsSecret(id = it.id , isSecret = isLock))
                                    }
                                    channel.trySend(snackbarChannelList.first {
                                        it.channelType == SnackBarChannelType.LOCK_CHANGE
                                    }.channel)
                                }
                                SettingMenu.MARKER -> {
                                    isMark = !isMark
                                    memo?.let {
                                        viewModel.onEvent(DetailMemoViewModel.Event.UpdateIsMark(id = it.id , isMark = isMark))
                                    }
                                    channel.trySend(snackbarChannelList.first {
                                        it.channelType == SnackBarChannelType.MARKER_CHANGE
                                    }.channel)
                                }
                                SettingMenu.TAG -> {
                                    isTagDialog = !isTagDialog
                                }

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
                    }
                ) {
                    Icon(
                        modifier = Modifier,
                        imageVector = Icons.Outlined.FolderOpen,
                        contentDescription = "Data Container",

                        )
                }


            }


            Box(
                modifier = Modifier
                    .clip(ShapeDefaults.ExtraSmall)
                    .background(color = Color.White)
                    .align(Alignment.Center)
                    .fillMaxWidth(0.9f),
                contentAlignment = Alignment.Center
            ){
                AssistChipGroupView(
                    isVisible = isTagDialog,
                    setState = selectedTagArray,
                    getState = { selectedTagList ->
                        memo?.let {
                            viewModel.onEvent(DetailMemoViewModel.Event.UpdateTagList(id, selectedTagList))
                        }
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
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    // isPressed.value = true
                                    hapticProcessing()
                                    tagInfoDataList.clear()
                                    selectedTagArray.value = arrayListOf()
                                    viewModel.onEvent(DetailMemoViewModel.Event.UpdateTagList(id,  arrayListOf()))
                                }
                            ) {
                                androidx.compose.material.Text(text = "Clear")
                            }


                            androidx.compose.material3.TextButton(
                                onClick = {
                                    // isPressed.value = true
                                    hapticProcessing()
                                    isTagDialog = false
                                }
                            ) {
                                androidx.compose.material.Text(text = "Confirm")
                            }
                        }


                    }


                }

            }






        }
    }



}