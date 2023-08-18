package com.example.gismemo.view

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.animation.*
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gismemo.LocalUsableHaptic
import com.example.gismemo.data.RepositoryProvider
import com.example.gismemo.db.LocalLuckMemoDB
import com.example.gismemo.db.entity.toCURRENTWEATHER_TBL
import com.example.gismemo.shared.composables.CheckPermission
import com.example.gismemo.shared.composables.PermissionRequiredCompose
import com.example.gismemo.shared.composables.PermissionRequiredComposeFuncName
import com.example.gismemo.shared.utils.SnackBarChannelType
import com.example.gismemo.shared.utils.snackbarChannelList
import com.example.gismemo.viewmodel.DetailMemoViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.maps.android.compose.widgets.ScaleBar
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class, MapsComposeExperimentalApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun DetailMemoView(navController: NavController, id:Long) {


    val permissions = listOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val multiplePermissionsState = rememberMultiplePermissionsState( permissions)
    CheckPermission(multiplePermissionsState = multiplePermissionsState)

    var isGranted by mutableStateOf(true)
    permissions.forEach { chkPermission ->
        isGranted = isGranted
                &&  ( multiplePermissionsState.permissions.find { it.permission == chkPermission  }?.status?.isGranted ?: false )
    }

    PermissionRequiredCompose(
        isGranted = isGranted,
        multiplePermissions = permissions ,
        viewType = PermissionRequiredComposeFuncName.MemoMap
    ) {

    val db = LocalLuckMemoDB.current
    val context = LocalContext.current
    val viewModel = remember {
        DetailMemoViewModel(repository = RepositoryProvider.getRepository().apply { database = db })
    }
    val memoID by rememberSaveable { mutableStateOf(id) }
    //--------------
    LaunchedEffect(key1 = memoID) {
        viewModel.onEvent(DetailMemoViewModel.Event.SetMemo(id = id))
        viewModel.onEvent(DetailMemoViewModel.Event.SetWeather(id = id))
        viewModel.onEvent(DetailMemoViewModel.Event.SetTags(id = id))
        viewModel.onEvent(DetailMemoViewModel.Event.SetFiles(id = id))
    }
    //--------------

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


    var mapProperties by remember {
        mutableStateOf(
            MapProperties(mapType = MapType.NORMAL, isMyLocationEnabled = true)
        )
    }

    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(zoomControlsEnabled = false)
        )
    }

    val sheetState = SheetState(
        skipPartiallyExpanded = false,
        initialValue = SheetValue.Hidden,
        skipHiddenState = false
    )

    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    var isTagDialog by rememberSaveable { mutableStateOf(false) }
    val isVisibleMenu = rememberSaveable { mutableStateOf(false) }

    val memo = viewModel.memo.collectAsState()
    val selectedTagArray = viewModel.tagArrayList.collectAsState()
    val weatherData = viewModel.weather.collectAsState()

    val isLock = mutableStateOf(memo.value?.isSecret ?: false)
    val isMark = mutableStateOf(memo.value?.isPin ?: false)
    val snippets = mutableStateOf(memo.value?.snippets ?: "")
    val selectedTags = mutableStateOf(selectedTagArray.value)
    var isTitleBox by  rememberSaveable{  mutableStateOf(true)}

    val fusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }


    val currentLocation =  mutableStateOf(
            LatLng(
                memo.value?.latitude?.toDouble() ?: 0.0,
                memo.value?.longitude?.toDouble() ?: 0.0
            )
        )


    var isGoCurrentLocation by remember { mutableStateOf(false) }

    val markerState = MarkerState(position = currentLocation.value)

    val defaultCameraPosition = CameraPosition.fromLatLngZoom(currentLocation.value, 16f)

    val cameraPositionState = CameraPositionState(position = defaultCameraPosition)


    val snackbarHostState = remember { SnackbarHostState() }
    val channel = remember { Channel<Int>(Channel.CONFLATED) }

    LaunchedEffect(channel) {

        channel.receiveAsFlow().collect { index ->
            val channelData = snackbarChannelList.first {
                it.channel == index
            }

            val result = snackbarHostState.showSnackbar(
                message = channelData.message,
                actionLabel = channelData.actionLabel,
                withDismissAction = channelData.withDismissAction,
                duration = channelData.duration
            )
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    hapticProcessing()
                    when (channelData.channelType) {
                        else -> {}
                    }
                }
                SnackbarResult.Dismissed -> {
                    hapticProcessing()
                }
            }
        }
    }


    val checkEnableLocationService: () -> Unit = {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener(context.mainExecutor) { task ->
            if (!task.isSuccessful || task.result == null) {
                channel.trySend(snackbarChannelList.first {
                    it.channelType == SnackBarChannelType.LOCATION_SERVICE_DISABLE
                }.channel)
            }
        }
    }

        val density = LocalDensity.current

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
                onMapLongClick = {},
                onMyLocationButtonClick = {
                    checkEnableLocationService.invoke()
                    return@GoogleMap false

                }
            ) {


                MapEffect(key1 = isGoCurrentLocation) {
                    if (isGoCurrentLocation) {
                        it.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation.value, 16F))
                        isGoCurrentLocation = false
                    }
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

            androidx.compose.material.IconButton(
                modifier = Modifier.align(Alignment.TopCenter).clip(RoundedCornerShape(6.dp))
                    .background(color = Color.Yellow.copy(alpha = 0.7f)).padding(horizontal = 10.dp),
                onClick = {
                    hapticProcessing()
                    isTitleBox = !isTitleBox
                },
                content = {
                    Row(
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = "titleBox"
                        )
                        Text("Memo Description")
                        Icon(
                            modifier = Modifier,
                            imageVector = if (isTitleBox) Icons.Outlined.UnfoldLess else Icons.Outlined.UnfoldMore,
                            contentDescription = "titleBox "
                        )
                    }
                })

            AnimatedVisibility(visible = isTitleBox,
                enter = slideInVertically {
                    // Slide in from 40 dp from the top.
                    with(density) { -40.dp.roundToPx() }
                } + expandVertically(
                    // Expand from the top.
                    expandFrom = Alignment.Top
                ) + fadeIn(
                    // Fade in with the initial alpha of 0.3f.
                    initialAlpha = 0.3f
                ),
                exit = slideOutVertically() + shrinkVertically() + fadeOut()
                ) {



                memo.value?.let {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 55.dp)
                                .padding(horizontal = 20.dp)
                                .background(
                                    color = Color.Yellow,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clip(RoundedCornerShape(8.dp)),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            weatherData.value?.let {
                                WeatherView(
                                    modifier = Modifier
                                        .width(400.dp)
                                        .background(
                                            color = Color.Yellow,
                                            shape = RoundedCornerShape(6.dp)
                                        ),
                                    item = it.toCURRENTWEATHER_TBL()
                                )
                            }



                            Text(it.title)
                            Text(it.desc)

                            Text(text = snippets.value)


                        }
                    }

                }

            }


            ScaleBar(
                modifier = Modifier.align(Alignment.TopStart),
                cameraPositionState = cameraPositionState
            )


            IconButton(
                modifier = Modifier.align(Alignment.BottomEnd),
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




            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color = Color.LightGray.copy(alpha = 0.7f)),
            ) {
                MapTypeMenuList.forEach {
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
                    .align(Alignment.BottomStart)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color = Color.LightGray.copy(alpha = 0.7f))
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
                                        isLock.value = !isLock.value
                                        memo.value?.let {
                                            viewModel.onEvent(
                                                DetailMemoViewModel.Event.UpdateIsSecret(
                                                    id = it.id,
                                                    isSecret = isLock.value
                                                )
                                            )
                                        }

                                        val snackBarChannelType =
                                            if (isLock.value) SnackBarChannelType.LOCK_CHANGE_SET else SnackBarChannelType.LOCK_CHANGE_FREE

                                        channel.trySend(snackbarChannelList.first {
                                            it.channelType == snackBarChannelType
                                        }.channel)
                                    }

                                    SettingMenu.MARKER -> {
                                        isMark.value = !isMark.value
                                        memo.value?.let {
                                            viewModel.onEvent(
                                                DetailMemoViewModel.Event.UpdateIsMark(
                                                    id = it.id,
                                                    isMark = isMark.value
                                                )
                                            )
                                        }

                                        val snackBarChannelType =
                                            if (isMark.value) SnackBarChannelType.MARKER_CHANGE_SET else SnackBarChannelType.MARKER_CHANGE_FREE

                                        channel.trySend(snackbarChannelList.first {
                                            it.channelType == snackBarChannelType
                                        }.channel)

                                    }
                                    SettingMenu.TAG -> {
                                        isTagDialog = !isTagDialog
                                    }

                                }
                            }) {
                            val icon = when (it) {
                                SettingMenu.SECRET -> {
                                    if (isLock.value) it.getDesc().first else it.getDesc().second
                                        ?: it.getDesc().first
                                }
                                SettingMenu.MARKER -> {
                                    if (isMark.value) it.getDesc().first else it.getDesc().second
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
            ) {


                if (isTagDialog) {

                    AssistChipGroupView(
                        isVisible = isTagDialog,
                        setState = selectedTags,
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
                                        isTagDialog = false
                                        hapticProcessing()
                                        snippets.value = ""
                                        selectedTags.value.clear()
                                        viewModel.onEvent(
                                            DetailMemoViewModel.Event.UpdateTagList(
                                                id,
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
                                        isTagDialog = false
                                        hapticProcessing()
                                        snippets.value = ""
                                        selectedTags.value.clear()
                                        tagInfoDataList.forEachIndexed { index, tagInfoData ->
                                            if (tagInfoData.isSet.value) {
                                                snippets.value =
                                                    "${snippets.value} #${tagInfoData.name}"
                                                selectedTags.value.add(index)
                                            }
                                        }

                                        viewModel.onEvent(
                                            DetailMemoViewModel.Event.UpdateTagList(
                                                id,
                                                selectedTags.value
                                            )
                                        )
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


        }
    }

}

}