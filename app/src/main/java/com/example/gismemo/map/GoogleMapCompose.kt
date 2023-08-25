package com.example.gismemo.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.res.Configuration
import android.location.Location
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.size.Size
import com.example.gismemo.LocalUsableHaptic
import com.example.gismemo.data.RepositoryProvider
import com.example.gismemo.db.LocalLuckMemoDB
import com.example.gismemo.db.entity.MEMO_TBL
import com.example.gismemo.model.BiometricCheckType
import com.example.gismemo.navigation.GisMemoDestinations
import com.example.gismemo.shared.composables.*
import com.example.gismemo.shared.utils.SnackBarChannelType
import com.example.gismemo.shared.utils.snackbarChannelList
import com.example.gismemo.ui.theme.GISMemoTheme
import com.example.gismemo.viewmodel.MemoMapViewModel
import com.example.gismemo.viewmodel.WriteMemoViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.maps.android.compose.widgets.ScaleBar
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


private const val TAG = "GoogleMap"

fun Location.toLatLng():LatLng{
    return LatLng(this.latitude, this.longitude)
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleMapView() {

    val context = LocalContext.current

    val fusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var currentLocation by remember {
        mutableStateOf(LatLng(0.0,0.0))
    }

    val isDeviceLocation = remember {
        mutableStateOf(false)
    }


    LaunchedEffect(key1 = isDeviceLocation.value, key2 =  currentLocation){
        if(isDeviceLocation.value || currentLocation == LatLng(0.0,0.0)) {

            fusedLocationProviderClient.lastLocation.addOnCompleteListener( context.mainExecutor) { task ->
                if (task.isSuccessful && task.result != null ) {
                    currentLocation = task.result.toLatLng()
                }
            }
            isDeviceLocation.value = false
        }
    }


    // No ~~~~ remember
val markerState =  MarkerState( position = currentLocation )
val defaultCameraPosition =  CameraPosition.fromLatLngZoom( currentLocation, 16f)
var cameraPositionState =  CameraPositionState(position = defaultCameraPosition)



var mapProperties by remember {
    mutableStateOf(
       MapProperties(
           mapType = MapType.NORMAL,
           isMyLocationEnabled = true,
       )
    )
}

val uiSettings by remember {
    mutableStateOf(
        MapUiSettings(
            compassEnabled = true,
            myLocationButtonEnabled = true,
            mapToolbarEnabled = true,
            zoomControlsEnabled = false

        )
    )
}



val sheetState = SheetState(skipPartiallyExpanded = false, initialValue = Hidden)

val scaffoldState =  rememberBottomSheetScaffoldState( bottomSheetState = sheetState )

val onMapLongClickHandler: (LatLng) -> Unit = {
    markerState.position = it
    cameraPositionState = CameraPositionState( position =  CameraPosition.fromLatLngZoom(it, 16f))
}


    BottomSheetScaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            TopMenuBar()
        },
        scaffoldState = scaffoldState,
        sheetContent = {
            MapTypeControls( onMapTypeClick = {
                mapProperties = mapProperties.copy(mapType = it)
            })
        },
        sheetContainerColor = Color.LightGray.copy(alpha = 0.5f),
        sheetPeekHeight = 0.dp

    ) { padding ->

        Box(Modifier.fillMaxSize()) {

                GoogleMap(
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = uiSettings,
                    onMapLongClick = onMapLongClickHandler,

                ) {

                   Marker(
                       state = markerState,
                       title = "lat/lng:(${String.format("%.5f", markerState.position.latitude)},${String.format("%.5f", markerState.position.longitude)})",
                   )

                }

               ScaleBar(
                   modifier = Modifier
                       .padding(bottom = 30.dp)
                       .align(Alignment.BottomStart),
                   cameraPositionState = cameraPositionState
               )

               Column(modifier = Modifier) {


                   FloatingActionButton(
                       onClick = {
                           isDeviceLocation.value = true
                       },
                       containerColor = Color.White.copy(alpha = 0.7f),
                   ) {
                       Icon(
                           modifier = Modifier,
                           imageVector = Icons.Outlined.CompassCalibration,
                           contentDescription = "CompassCalibration",
                       )
                   }


               }

        }
    }





}

@Composable
 fun TopMenuBar(){
    Row(modifier = Modifier) {

        }
}

@Composable
 fun MapTypeControls( onMapTypeClick: (MapType) -> Unit ) {
Row (
   modifier = Modifier
       .fillMaxWidth()
       .horizontalScroll(state = ScrollState(0)),
   horizontalArrangement = Arrangement.Center
) {
   MapType.values().filter {
       it.name == "NORMAL" ||
               it.name == "TERRAIN" ||
               it.name == "HYBRID"
   }.forEach {
       MapTypeButton(type = it) { onMapTypeClick(it) }
   }
}
}

@Composable
private fun MapTypeButton(type: MapType, onClick: () -> Unit) =
MapButton(text = type.toString(), onClick = onClick)

@Composable
private fun MapButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
Button(
   modifier = modifier.padding(4.dp),
   colors = ButtonDefaults.buttonColors(
       containerColor = MaterialTheme.colorScheme.outline,
       contentColor = MaterialTheme.colorScheme.onPrimary
   ),
   onClick = onClick
) {
   Text(text = text, style = MaterialTheme.typography.labelSmall)
}
}

@OptIn(ExperimentalPermissionsApi::class)
@Preview
@Composable
fun PrevViewMap(){

     val permissionsManager = PermissionsManager()

    CompositionLocalProvider(LocalPermissionsManager provides permissionsManager) {


        val permissions = listOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val multiplePermissionsState = rememberMultiplePermissionsState(permissions)
        CheckPermission(multiplePermissionsState = multiplePermissionsState)


        var isGranted by mutableStateOf(true)
        permissions.forEach { chkPermission ->
            isGranted =  isGranted && multiplePermissionsState.permissions.find { it.permission == chkPermission }?.status?.isGranted
                ?: false
        }

        val navController = rememberNavController()

        PermissionRequiredCompose(
            isGranted = isGranted,
            multiplePermissions = permissions,
            viewType = PermissionRequiredComposeFuncName.Weather
        ) {

            GISMemoTheme {
                Box {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        //      WriteMemoView(navController = navController)
                        //   DrawingOnMap()
                        GoogleMapView()
                        //      MemoMapView(navController)
                    }
                }
            }
        }

    }

}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class,
    MapsComposeExperimentalApi::class
)
@Composable
fun MemoMapView(navController: NavController){

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

    val context = LocalContext.current
    val db = LocalLuckMemoDB.current

    val viewModel = remember {
        MemoMapViewModel(repository = RepositoryProvider.getRepository().apply { database = db }  )
    }

    val fusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var currentLocation by remember {
        mutableStateOf(LatLng(0.0,0.0))
    }

    var location:Location? by remember {
        mutableStateOf(null)
    }

        var isGoCurrentLocation by remember { mutableStateOf(false) }

    LaunchedEffect( key1 =  currentLocation){
        if( currentLocation == LatLng(0.0,0.0)) {
            fusedLocationProviderClient.lastLocation.addOnCompleteListener( context.mainExecutor) { task ->
                if (task.isSuccessful && task.result != null ) {
                    location = task.result
                    currentLocation = task.result.toLatLng()
                }
            }
            viewModel.onEvent(MemoMapViewModel.Event.SetMarkers)
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
                    message = channelData.message,
                    actionLabel = channelData.actionLabel,
                    withDismissAction = channelData.withDismissAction,
                    duration = channelData.duration
                )
                when (result) {
                    SnackbarResult.ActionPerformed -> {
                        hapticProcessing()
                        when (channelData.channelType) {
                            else -> {  }
                        }
                    }
                    SnackbarResult.Dismissed -> {
                        hapticProcessing()
                    }
                }
            }
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



    val markerMemoList = viewModel.markerMemoList.collectAsState()


    // No ~~~~ remember
    val markerState =  MarkerState( position = currentLocation )
    val defaultCameraPosition =  CameraPosition.fromLatLngZoom( currentLocation, 16f)
    val cameraPositionState =  CameraPositionState(position = defaultCameraPosition)


    var mapProperties by remember {
        mutableStateOf(
            MapProperties(
                mapType = MapType.NORMAL,
                isMyLocationEnabled = true,
            )
        )
    }

    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = false
                )
        )
    }

    val isVisibleMenu = rememberSaveable {
        mutableStateOf(false)
    }


    val sheetState = SheetState(skipPartiallyExpanded = false, initialValue = Hidden)

    val scaffoldState =  rememberBottomSheetScaffoldState( bottomSheetState = sheetState )

    val onMapLongClickHandler: (LatLng) -> Unit = {
        hapticProcessing()
        currentLocation = it
    }


    val configuration = LocalConfiguration.current
    val cardViewRate:Float =when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> {
              1f
        }
        else -> {
            0.6f
        }
    }



    val isMemoCardView = remember{ mutableStateOf(false) }
    val isCurrentMemo = remember{ mutableStateOf(0L) }





            BottomSheetScaffold(
                modifier = Modifier.statusBarsPadding(),
                topBar = {
                    TopMenuBar()
                },
                scaffoldState = scaffoldState,
                sheetContent = {
                    MapTypeControls(onMapTypeClick = {
                        mapProperties = mapProperties.copy(mapType = it)
                    })
                },
                sheetContainerColor = Color.LightGray.copy(alpha = 0.5f),
                sheetPeekHeight = 0.dp,
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState)
                }

            ) { padding ->

                Box(Modifier.fillMaxSize()) {

                    GoogleMap(
                        //  modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = mapProperties,
                        uiSettings = uiSettings,
                        onMapLongClick = onMapLongClickHandler,
                        onMyLocationButtonClick = {
                            checkEnableLocationService.invoke()
                            return@GoogleMap false

                        }
                        ) {


                        MapEffect(key1 = isGoCurrentLocation){
                            if(isGoCurrentLocation) {
                                it.animateCamera( CameraUpdateFactory.newLatLngZoom( currentLocation, 16F) )
                                isGoCurrentLocation = false
                            }
                        }


                        Marker(
                            state = markerState,
                            title = "lat/lng:(${String.format("%.5f", markerState.position.latitude)},${String.format("%.5f", markerState.position.longitude)})",
                        )

                        markerMemoList.value.forEach {

                            val state =
                                MarkerState(
                                    position = LatLng(it.latitude.toDouble(), it.longitude.toDouble())
                                )


                            MarkerInfoWindowContent(
                                state = state,
                                title = it.title,
                                //   snippet = "${it.snippets}\n${it.desc}",
                                //    onClick = markerClick,
                                //  icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                                draggable = true,
                                onInfoWindowClick = {
                                    hapticProcessing()
                                    isMemoCardView.value = false
                                },
                                onInfoWindowClose = {},
                                onInfoWindowLongClick = { marker ->
                                    hapticProcessing()
                                    isMemoCardView.value = true
                                    isCurrentMemo.value = it.id
                                },
                            ) { marker ->

                                Column {
                                    Text(marker.title ?: "", color = Color.Red)
                                }
                            }

                        }

                    }

                    if (isMemoCardView.value) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(cardViewRate)
                                .align(Alignment.BottomCenter)
                                .padding(10.dp)
                                .shadow(AppBarDefaults.TopAppBarElevation)
                        ) {

                            MemoView(
                                item = markerMemoList.value.first {
                                    it.id == isCurrentMemo.value
                                },
                                viewModel::onEvent,
                                navController = navController
                            )
                        }
                    }


                    IconButton(
                        modifier = Modifier.align(Alignment.TopStart).padding(2.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color =MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)),
                        onClick = {
                            hapticProcessing()
                            isGoCurrentLocation = true
                        //        cameraPositionState.position = defaultCameraPosition

                        }
                    ) {
                        Icon(
                            modifier = Modifier.scale(1f),
                            imageVector = Icons.Outlined.ModeOfTravel,
                            contentDescription = "ModeOfTravel",
                        )
                    }



                    ScaleBar(
                        modifier = Modifier
                            .align(Alignment.BottomEnd).padding(bottom = 10.dp, end = 10.dp),
                        cameraPositionState = cameraPositionState
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clip(RoundedCornerShape(2.dp)).padding(2.dp)
                            .background(color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp))

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
                                contentDescription = "OpenWith",
                            )
                        }


                        MapTypeMenuList.forEach {
                            AnimatedVisibility(
                                visible = isVisibleMenu.value,
                            ) {
                               IconButton(onClick = {
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

                }
            }


    }


}





typealias DrawingPolyline = List<LatLng>


@OptIn(ExperimentalMaterial3Api::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun MemoView(
    item: MEMO_TBL,
    event: ((MemoMapViewModel.Event)->Unit)? = null,
    navController: NavController? = null){



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


    val context = LocalContext.current

    val permissions = listOf(
        Manifest.permission.USE_BIOMETRIC,
    )
    val multiplePermissionsState = rememberMultiplePermissionsState( permissions)


    var isGranted by mutableStateOf(true)
    permissions.forEach { chkPermission ->
        isGranted = isGranted && multiplePermissionsState.permissions.find { it.permission == chkPermission }?.status?.isGranted
            ?: false
    }


    fun checkBiometricSupport(): Boolean {
        val isDeviceSecure = ContextCompat.getSystemService(context, KeyguardManager::class.java)?.isDeviceSecure ?: false
        return isGranted || isDeviceSecure
    }



    val onResult: (isSucceeded:Boolean, bioMetricCheckType: BiometricCheckType, errorMsg:String? ) -> Unit =
        { result, bioMetricCheckType, msg ->
            if(result){
                when(bioMetricCheckType){
                    BiometricCheckType.DETAILVIEW -> {

                        navController?.let {
                            if (event != null) {
                                event(
                                    MemoMapViewModel.Event.ToRoute(
                                        navController = it,
                                        route = GisMemoDestinations.DetailMemoView.createRoute( id= item.id.toString() )
                                    )
                                )
                            }
                        }
                    }

                    else -> {}
                }
            }else {

            }
        }




    val shape = RoundedCornerShape(6.dp)

    Card(
        modifier = Modifier
            .height(260.dp)
            .padding(top = 2.dp)
        ,
        onClick = {


            hapticProcessing()
            if(item.isSecret && checkBiometricSupport()) {
                biometricPrompt(context, BiometricCheckType.DETAILVIEW, onResult)
            }else {
                navController?.let {
                    if (event != null) {
                        event(
                            MemoMapViewModel.Event.ToRoute(
                                navController = it,
                                route = GisMemoDestinations.DetailMemoView.createRoute( id= item.id.toString() )
                            )
                        )
                    }
                }
            }


        }

    ) {

                Box(

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SwipeBoxHeight)
                        .background(Color.LightGray)
                        .clip(shape),

                    contentAlignment = Alignment.Center
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Icon(
                            modifier = Modifier
                                .width(30.dp)
                                .scale(1f),
                            imageVector = if (item.isSecret) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                            contentDescription = "Lock",
                        )

                        Column(
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(item.title)
                            Text(item.desc)
                            Text(item.snippets)
                        }

                        Icon(
                            modifier = Modifier
                                .width(30.dp)
                                .scale(1f),
                            imageVector = if (item.isPin) Icons.Outlined.LocationOn else Icons.Outlined.LocationOff,
                            contentDescription = "Mark",
                        )


                    }
                }

                ImageViewer(data = item.snapshot.toUri() , size = Size.ORIGINAL, isZoomable = false)

            }





}

