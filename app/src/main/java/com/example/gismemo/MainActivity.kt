package com.example.gismemo

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import coil.size.Size
import com.example.gismemo.db.LocalLuckMemoDB
import com.example.gismemo.db.LuckMemoDB
import com.example.gismemo.navigation.GisMemoDestinations
import com.example.gismemo.navigation.mainScreens
import com.example.gismemo.navigation.navigateTo
import com.example.gismemo.shared.composables.*
import com.example.gismemo.ui.theme.GISMemoTheme
import com.example.gismemo.view.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Flow




//@AndroidEntryPoint


class MainActivity : ComponentActivity() {

    private val permissionsManager = PermissionsManager()


    fun checkInternetConnected() :Boolean  {
        ( applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
            activeNetwork?.let {network ->
                getNetworkCapabilities(network)?.let {networkCapabilities ->
                    return when {
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> { false }
                    }
                }
            }
            return false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



    //    KakaoSdk.init(this, "dfb918015eb4cc70b5db5d2e875dc58a")

        setContent {

            val context = LocalContext.current
            val luckMemoDB = LuckMemoDB.getInstance(context.applicationContext)

            val coroutineScope = rememberCoroutineScope()

            val navController = rememberNavController()
            val currentBackStack by navController.currentBackStackEntryAsState()

            val configuration = LocalConfiguration.current

            val selectedItem = rememberSaveable { mutableStateOf(0) }
            var isPortrait by remember { mutableStateOf(false) }

            var gridWidth by remember { mutableStateOf(1f) }

            when (configuration.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> {
                    isPortrait = true
                    gridWidth = 1f
                }
                else ->{
                    isPortrait = false
                    gridWidth = 0.87f
                }
            }

            LaunchedEffect(key1 = currentBackStack){
                val currentScreen = mainScreens.find {
                    it.route ==  currentBackStack?.destination?.route
                }
                selectedItem.value =  mainScreens.indexOf(currentScreen)
            }

            val isConnect  = mutableStateOf(checkInternetConnected())

            LaunchedEffect(key1 = isConnect ){
                while(!isConnect.value) {
                    delay(500)
                    isConnect.value = checkInternetConnected()
                }
            }


            GISMemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CompositionLocalProvider( LocalLuckMemoDB provides luckMemoDB) {
                        CompositionLocalProvider( LocalPermissionsManager provides permissionsManager) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (!isConnect.value) {
                                    ChkNetWork(
                                        onCheckState = {
                                            coroutineScope.launch {
                                                isConnect.value = checkInternetConnected()
                                            }
                                        }
                                    )
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Top
                                    ) {

                                        if (isPortrait) {

/*
                                BottomNavigation(
                                    modifier = Modifier.height(100.dp),
                                    backgroundColor = Color.Transparent,
                                    contentColor = Color.Black,
                                )
                                {
                                    mainScreens.forEachIndexed { index, item ->
                                        NavigationRailItem(
                                            modifier = Modifier
                                                .align(Alignment.CenterVertically)
                                                .padding(vertical = 2.dp),
                                            icon = {
                                                item.icon?.let {
                                                    Icon(
                                                        it,
                                                        contentDescription = item.name
                                                    )
                                                }
                                            },
                                            label = { androidx.compose.material.Text(item.name ?: "") },
                                            selected = selectedItem.value == index,
                                            onClick = {
                                               selectedItem.value = index
                                                navController.navigateTo(mainScreens[index].route)
                                            }
                                        )
                                    }
                                }


 */

                                            Row(
                                                modifier = Modifier
                                                    .align(Alignment.CenterHorizontally)
                                                    .fillMaxWidth()
                                                    .height(90.dp)
                                                    .shadow(6.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {

                                                mainScreens.forEachIndexed { index, item ->


                                                    Column(
                                                        modifier = Modifier.padding(horizontal = 10.dp),
                                                        verticalArrangement = Arrangement.Center,
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {

                                                        Box(modifier = Modifier) {

                                                            IconButton(
                                                                modifier = Modifier,
                                                                onClick = {
                                                                    selectedItem.value = index
                                                                    navController.navigateTo(
                                                                        mainScreens[index].route
                                                                    )
                                                                },
                                                                content = {
                                                                    item.icon?.let {
                                                                        Icon(
                                                                            modifier = Modifier,
                                                                            imageVector = it,
                                                                            contentDescription = item.name
                                                                        )
                                                                    }
                                                                }
                                                            )

                                                            if (selectedItem.value == index) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .align(Alignment.Center)
                                                                        .width(40.dp)
                                                                        .height(40.dp)
                                                                        .border(
                                                                            width = 25.dp,
                                                                            color = Color.Blue.copy(
                                                                                alpha = 0.1f
                                                                            ),
                                                                            shape = ShapeDefaults.Large
                                                                        )
                                                                )

                                                            }

                                                        }

                                                        item.name?.let { Text(text = it) }

                                                    }


                                                }

                                            }

                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(0.dp),
                                            verticalAlignment = Alignment.CenterVertically

                                        ) {

                                            Box(modifier = Modifier.fillMaxWidth(gridWidth)) {
                                                GisMemoNavHost(navController)
                                            }


                                            if (!isPortrait) {
/*
                                    Box(modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)   ) {
                                        NavigationRail(
                                            modifier = Modifier
                                                .width(100.dp)
                                                .background(color = Color.White)
                                                .shadow(6.dp),
                                            containerColor = Color.Transparent,
                                            contentColor = Color.Black,
                                            header = {
                                           //     Spacer(Modifier.height(20.dp))
                                            }
                                        ) {
                                            mainScreens.forEachIndexed { index, item ->
                                                NavigationRailItem(
                                                    modifier = Modifier.padding(vertical = 2.dp),
                                                    icon = {
                                                        item.icon?.let {
                                                            Icon(
                                                                it,
                                                                contentDescription = item.name
                                                            )
                                                        }
                                                    },
                                                    label = {
                                                        androidx.compose.material.Text(
                                                            item.name ?: ""
                                                        )
                                                    },
                                                    selected = selectedItem.value == index,
                                                    onClick = {
                                                        selectedItem.value = index
                                                        navController.navigateTo(mainScreens[index].route)
                                                    }
                                                )

                                            }
                                        }
                                    }

 */

                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 2.dp)
                                                ) {
                                                    Column(
                                                        modifier = Modifier
                                                            .align(Alignment.Center)
                                                            .fillMaxHeight()
                                                            .width(90.dp)
                                                            .shadow(6.dp),
                                                        verticalArrangement = Arrangement.Center,
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {

                                                        mainScreens.forEachIndexed { index, item ->

                                                            Column(
                                                                modifier = Modifier.padding(vertical = 0.dp),
                                                                verticalArrangement = Arrangement.Center,
                                                                horizontalAlignment = Alignment.CenterHorizontally
                                                            ) {

                                                                Box(modifier = Modifier) {
                                                                    IconButton(
                                                                        modifier = Modifier,
                                                                        onClick = {
                                                                            selectedItem.value =
                                                                                index
                                                                            navController.navigateTo(
                                                                                mainScreens[index].route
                                                                            )
                                                                        },
                                                                        content = {
                                                                            item.icon?.let {
                                                                                Icon(
                                                                                    modifier = Modifier,
                                                                                    imageVector = it,
                                                                                    contentDescription = item.name
                                                                                )
                                                                            }
                                                                        }
                                                                    )

                                                                    if (selectedItem.value == index) {
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .align(Alignment.Center)
                                                                                .width(40.dp)
                                                                                .height(40.dp)
                                                                                .border(
                                                                                    width = 25.dp,
                                                                                    color = Color.Blue.copy(
                                                                                        alpha = 0.1f
                                                                                    ),
                                                                                    shape = ShapeDefaults.Large
                                                                                )
                                                                        )
                                                                    }
                                                                }

                                                                item.name?.let { Text(text = it) }

                                                            }

                                                        }
                                                    }
                                                }

                                            }

                                        }

                                    }
                                }
                            }
                        }
                    }

                }
            }




        }
    }

}


@Composable
fun GisMemoNavHost(
    navController: NavHostController
){

    NavHost(
        navController = navController,
        startDestination = GisMemoDestinations.IntroView.route
    ) {

        composable(
            route = GisMemoDestinations.IntroView.route
        ){
            IntroView(navController = navController)
        }

        composable(
            route = GisMemoDestinations.WriteMemoView.route
        ){
            WriteMemoView(navController = navController)
        }

        composable(
            route = GisMemoDestinations.MapView.route
        ){
            MemoMapView(navController = navController)
        }

        composable(
            route = GisMemoDestinations.SettingView.route
        ){
            SettingsView(navController = navController)
        }

        composable(
            route = GisMemoDestinations.DetailMemoView.route ,
            arguments = listOf(
                navArgument(GisMemoDestinations.ARG_NAME_ID){
                    nullable = false
                    type = NavType.StringType } )

        ){
            DetailMemoView(
                navController = navController,
                id = GisMemoDestinations.DetailMemoView.getIDFromArgs(it.arguments).toLong())
        }

        composable(
            route = GisMemoDestinations.CameraCompose.route
        ){
            CameraCompose( navController = navController)
        }

        composable(
            route = GisMemoDestinations.SpeechToText.route
        ){
            SpeechRecognizerCompose( navController = navController)
        }

        composable(
            route =  GisMemoDestinations.PhotoPreview.route,
            arguments = listOf(
                navArgument(GisMemoDestinations.ARG_NAME_FILE_PATH) {
                    nullable = false
                    type = NavType.StringType})
        ){
            ImageViewer(
                data = GisMemoDestinations.PhotoPreview.getUriFromArgs(it.arguments).toUri(),
                size = Size.ORIGINAL,
                isZoomable = true )
        }

        composable(
            route = GisMemoDestinations.ExoPlayerView.route,
            arguments = listOf(
                navArgument(GisMemoDestinations.ARG_NAME_FILE_PATH) {
                    nullable = false
                    type = NavType.StringType},
                navArgument(GisMemoDestinations.ARG_NAME_ISVISIBLE_AMPLITUDES) {
                    nullable = false
                    type = NavType.BoolType})
        ){
            ExoplayerCompose(
                uri = GisMemoDestinations.ExoPlayerView.getUriFromArgs(it.arguments).toUri(),
                isVisibleAmplitudes = GisMemoDestinations.ExoPlayerView.getIsVisibleAmplitudesFromArgs(it.arguments)
            )
        }

    }


    BackHandler {

        navController.popBackStack()
    }

}



@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ChkNetWork(
    onCheckState:()->Unit
){

    val permissions = listOf(Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE)
    val multiplePermissionsState = rememberMultiplePermissionsState(permissions)
    CheckPermission(multiplePermissionsState = multiplePermissionsState)

    var isGranted by mutableStateOf(true)

    permissions.forEach { chkPermission ->
        isGranted = isGranted
                && (multiplePermissionsState.permissions.find { it.permission == chkPermission }?.status?.isGranted
            ?: false)
    }

    val  url = Uri.parse("android.resource://com.example.gismemo/" + R.drawable.baseline_wifi_off_black_48).toString().toUri()

    PermissionRequiredCompose(
        isGranted = isGranted,
        multiplePermissions = permissions
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 60.dp),
                textAlign = TextAlign.Center,
                style = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 26.sp),
                text = "Gis Momo"
            )

            ImageViewer(data = url, size = Size.ORIGINAL, isZoomable = false)



            Button(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 60.dp),
                onClick = {
                    onCheckState()
                }
            ) {
                Text("네트웍 연결을 확인해 주세요")
            }


        }


    }

}



/*

@Composable
fun  ORIENTATION_TEST(){
    val configuration = LocalConfiguration.current
    when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            Text("Landscape")
        }
        Configuration.ORIENTATION_PORTRAIT -> {
            Text("Portrait")
        }
        Configuration.ORIENTATION_UNDEFINED -> {
            Text("UnDefined")
        }
        else -> {
            Text("Else")
        }
    }

}

 */