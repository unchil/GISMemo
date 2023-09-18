package com.unchil.gismemo

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import coil.size.Size
import com.unchil.gismemo.data.RepositoryProvider
import com.unchil.gismemo.db.LocalLuckMemoDB
import com.unchil.gismemo.db.LuckMemoDB
import com.unchil.gismemo.navigation.GisMemoDestinations
import com.unchil.gismemo.navigation.mainScreens
import com.unchil.gismemo.navigation.navigateTo
import com.unchil.gismemo.shared.composables.*
import com.unchil.gismemo.ui.theme.GISMemoTheme
import com.unchil.gismemo.view.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.unchil.gismemo.shared.checkInternetConnected
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


val LocalChangeLocale = compositionLocalOf{ false }
val LocalUsableHaptic = compositionLocalOf{ true }
val LocalUsableDarkMode = compositionLocalOf{ false }
val LocalUsableDynamicColor = compositionLocalOf{ false }

class MainActivity : ComponentActivity() {

    private val permissionsManager = PermissionsManager()

/*
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

 */


    override fun attachBaseContext(context: Context?) {

        if(context != null ){
            context.let {
                val luckMemoDB = LuckMemoDB.getInstance(context.applicationContext)
                val repository = RepositoryProvider.getRepository().apply { database = luckMemoDB }

                if(repository.isFirstSetup.value){
                    repository.isFirstSetup.value = false
                //    val index = languageList.indexOf( Locale.getDefault().language)
                    val index = context.getLanguageArray().indexOf(Locale.getDefault().language)
                    repository.isChangeLocale.value = if (index == -1 ) 0 else index
                    super.attachBaseContext(context)
                } else {
                 //   val locale = Locale( languageList[    repository.isChangeLocale.value    ] )
                    val locale = Locale( context.getLanguageArray()[    repository.isChangeLocale.value    ] )
                    Locale.setDefault(locale)
                    context.resources.configuration.setLayoutDirection(locale)
                    it.resources.configuration.setLocale(locale)
                    it.createConfigurationContext(it.resources.configuration)
                    super.attachBaseContext(it.createConfigurationContext(it.resources.configuration))
                }
            }
        }else {
            super.attachBaseContext(context)
        }
    }



    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



    //    KakaoSdk.init(this, BuildConfig.KAKAO_KEY_KEY)

        setContent {


            val context = LocalContext.current
            val luckMemoDB = LuckMemoDB.getInstance(context.applicationContext)
            val repository = RepositoryProvider.getRepository().apply { database = luckMemoDB }
            val onChangeLocale = repository.onChangeLocale.collectAsState()
            val isUsableHaptic = repository.isUsableHaptic.collectAsState()
            val isUsableDarkMode = repository.isUsableDarkMode.collectAsState()
            val isUsableDynamicColor = repository.isUsableDynamicColor.collectAsState()
            val hapticFeedback = LocalHapticFeedback.current
            val isPressed = remember { mutableStateOf(false) }


            LaunchedEffect(key1 = isPressed.value, key2 = isUsableHaptic.value) {
                if (isPressed.value && isUsableHaptic.value) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    isPressed.value = false
                }
            }

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
                    gridWidth = 0.9f
                }
            }


            LaunchedEffect(key1 = currentBackStack){
                val currentScreen = mainScreens.find {
                    it.route ==  currentBackStack?.destination?.route
                }
                selectedItem.value =  mainScreens.indexOf(currentScreen)
            }

            val isConnect  = mutableStateOf(context.checkInternetConnected())

            LaunchedEffect(key1 = isConnect ){
                while(!isConnect.value) {
                    delay(500)
                    isConnect.value = context.checkInternetConnected()
                }
            }


                GISMemoTheme(darkTheme = isUsableDarkMode.value,
                    dynamicColor = isUsableDynamicColor.value
                ) {

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        CompositionLocalProvider(LocalChangeLocale provides onChangeLocale.value) {
                            CompositionLocalProvider(LocalUsableDarkMode provides isUsableDarkMode.value) {
                                CompositionLocalProvider(LocalUsableDynamicColor provides isUsableDynamicColor.value) {
                                    CompositionLocalProvider(LocalUsableHaptic provides isUsableHaptic.value) {
                                        CompositionLocalProvider(LocalLuckMemoDB provides luckMemoDB) {
                                            CompositionLocalProvider(LocalPermissionsManager provides permissionsManager) {


                                                Box(modifier = Modifier.fillMaxSize()) {
                                                    if (!isConnect.value) {
                                                        ChkNetWork(
                                                            onCheckState = {
                                                                coroutineScope.launch {
                                                                    isConnect.value =
                                                                        checkInternetConnected()
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
                                                            Row(
                                                                modifier = Modifier
                                                                    .align(Alignment.CenterHorizontally)
                                                                    .fillMaxWidth()
                                                                    .height(90.dp)
                                                                    .shadow(2.dp),
                                                                horizontalArrangement = Arrangement.Center,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {

                                                                mainScreens.forEachIndexed { index, item ->


                                                                    Column(
                                                                        modifier = Modifier.padding(
                                                                            horizontal = 10.dp
                                                                        ),
                                                                        verticalArrangement = Arrangement.Center,
                                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                                    ) {

                                                                        Box(modifier = Modifier) {

                                                                            IconButton(
                                                                                modifier = Modifier,

                                                                                onClick = {
                                                                                    isPressed.value =
                                                                                        true
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
                                                                                            contentDescription = context.resources.getString(
                                                                                                item.name
                                                                                            )
                                                                                        )
                                                                                    }
                                                                                }
                                                                            )

                                                                            if (selectedItem.value == index) {
                                                                                Box(
                                                                                    modifier = Modifier
                                                                                        .align(
                                                                                            Alignment.Center
                                                                                        )
                                                                                        .width(40.dp)
                                                                                        .height(40.dp)
                                                                                        .border(
                                                                                            width = 25.dp,
                                                                                            color = MaterialTheme.colorScheme.surfaceTint.copy(
                                                                                                alpha = 0.6f
                                                                                            ),
                                                                                            shape = ShapeDefaults.Large
                                                                                        )
                                                                                )

                                                                            }

                                                                        }

                                                                        Text(
                                                                            text = context.resources.getString(
                                                                                item.name
                                                                            )
                                                                        )

                                                                    }


                                                                }

                                                            }


 */
                                                                BottomNavigation(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .height(60.dp)
                                                                        .shadow(elevation = 1.dp),
                                                                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                                                                ) {

                                                                    Spacer(
                                                                        modifier = Modifier.padding(
                                                                            horizontal = 10.dp
                                                                        )
                                                                    )

                                                                    mainScreens.forEachIndexed { index, it ->

                                                                        BottomNavigationItem(
                                                                            icon = {
                                                                                Icon(
                                                                                    imageVector = it.icon
                                                                                        ?: Icons.Outlined.Info,
                                                                                    contentDescription = context.resources.getString(
                                                                                        it.name
                                                                                    ),
                                                                                    tint = if (selectedItem.value == index) Color.Red else MaterialTheme.colorScheme.secondary
                                                                                )
                                                                            },
                                                                            label = {
                                                                                androidx.compose.material.Text(
                                                                                    context.resources.getString(
                                                                                        it.name
                                                                                    )
                                                                                )
                                                                            },
                                                                            selected = selectedItem.value == index,
                                                                            onClick = {
                                                                                isPressed.value =
                                                                                    true
                                                                                selectedItem.value =
                                                                                    index
                                                                                navController.navigateTo(
                                                                                    mainScreens[index].route
                                                                                )
                                                                            },
                                                                            selectedContentColor = Color.Red,
                                                                            unselectedContentColor = MaterialTheme.colorScheme.secondary

                                                                        )

                                                                    }

                                                                    Spacer(
                                                                        modifier = Modifier.padding(
                                                                            horizontal = 10.dp
                                                                        )
                                                                    )
                                                                }
                                                            }

                                                            Row(
                                                                modifier = Modifier,
                                                                horizontalArrangement = Arrangement.SpaceEvenly,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {

                                                                Box(
                                                                    modifier = Modifier.fillMaxWidth(
                                                                        gridWidth
                                                                    )
                                                                ) {
                                                                    GisMemoNavHost(navController)
                                                                }


                                                                if (!isPortrait) {

                                                                    /*
                                                                Box(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .padding(vertical = 2.dp)
                                                                ) {
                                                                    Column(
                                                                        modifier = Modifier
                                                                            .align(Alignment.Center)
                                                                            .fillMaxHeight()
                                                                            .width(100.dp)
                                                                            .shadow(2.dp),
                                                                        verticalArrangement = Arrangement.Center,
                                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                                    ) {

                                                                        mainScreens.forEachIndexed { index, item ->

                                                                            Column(
                                                                                modifier = Modifier.padding(
                                                                                    vertical = 0.dp
                                                                                ),
                                                                                verticalArrangement = Arrangement.Center,
                                                                                horizontalAlignment = Alignment.CenterHorizontally
                                                                            ) {

                                                                                Box(modifier = Modifier) {
                                                                                    IconButton(
                                                                                        modifier = Modifier,
                                                                                        onClick = {
                                                                                            isPressed.value =
                                                                                                true
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
                                                                                                    contentDescription = context.resources.getString(
                                                                                                        item.name
                                                                                                    )
                                                                                                )
                                                                                            }
                                                                                        }
                                                                                    )

                                                                                    if (selectedItem.value == index) {
                                                                                        Box(
                                                                                            modifier = Modifier
                                                                                                .align(
                                                                                                    Alignment.Center
                                                                                                )
                                                                                                .width(
                                                                                                    40.dp
                                                                                                )
                                                                                                .height(
                                                                                                    40.dp
                                                                                                )
                                                                                                .border(
                                                                                                    width = 25.dp,
                                                                                                    color = MaterialTheme.colorScheme.surfaceTint.copy(
                                                                                                        alpha = 0.6f
                                                                                                    ),
                                                                                                    shape = ShapeDefaults.Large
                                                                                                )
                                                                                        )
                                                                                    }
                                                                                }

                                                                                Text(
                                                                                    text = context.resources.getString(
                                                                                        item.name
                                                                                    )
                                                                                )

                                                                            }

                                                                        }
                                                                    }
                                                                }


                                                                 */

                                                                    NavigationRail(
                                                                        modifier = Modifier.shadow(
                                                                            elevation = 1.dp
                                                                        )
                                                                            .width(80.dp),
                                                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                                    ) {

                                                                        Spacer(
                                                                            modifier = Modifier.padding(
                                                                                vertical = 20.dp
                                                                            )
                                                                        )
                                                                        mainScreens.forEachIndexed { index, it ->
                                                                            NavigationRailItem(
                                                                                icon = {
                                                                                    Icon(
                                                                                        imageVector = it.icon
                                                                                            ?: Icons.Outlined.Info,
                                                                                        contentDescription = context.resources.getString(
                                                                                            it.name
                                                                                        ),
                                                                                        tint = if (selectedItem.value == index) Color.Red else MaterialTheme.colorScheme.secondary
                                                                                    )
                                                                                },
                                                                                label = {
                                                                                    Text(
                                                                                        text = context.resources.getString(
                                                                                            it.name
                                                                                        ),
                                                                                        color = if (selectedItem.value == index) Color.Red else MaterialTheme.colorScheme.secondary
                                                                                    )
                                                                                },
                                                                                selected = selectedItem.value == index,
                                                                                onClick = {
                                                                                    isPressed.value =
                                                                                        true
                                                                                    selectedItem.value =
                                                                                        index
                                                                                    navController.navigateTo(
                                                                                        mainScreens[index].route
                                                                                    )
                                                                                }
                                                                            )
                                                                        }
                                                                        Spacer(
                                                                            modifier = Modifier.padding(
                                                                                vertical = 20.dp
                                                                            )
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

    val context = LocalContext.current

    val permissions = listOf(Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE)
    val multiplePermissionsState = rememberMultiplePermissionsState(permissions)
    CheckPermission(multiplePermissionsState = multiplePermissionsState)

    var isGranted by mutableStateOf(true)

    val isUsableDarkMode = LocalUsableDarkMode.current
    val colorFilter: ColorFilter? = if(isUsableDarkMode){
        ColorFilter.tint(Color.LightGray, blendMode = BlendMode.Darken)
    }else {
        null
    }

    permissions.forEach { chkPermission ->
        isGranted = isGranted && multiplePermissionsState.permissions.find { it.permission == chkPermission }?.status?.isGranted
            ?: false
    }

    //val  url = Uri.parse("android.resource://com.example.gismemo/" + R.drawable.baseline_wifi_off_black_48).toString().toUri()

    PermissionRequiredCompose(
        isGranted = isGranted,
        multiplePermissions = permissions
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 60.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                text = "Gis Momo"
            )

        //    ImageViewer(data = url, size = Size.ORIGINAL, isZoomable = false)

            Image(
                painter =  painterResource(R.drawable.baseline_wifi_off_black_48),
                modifier = Modifier
                    .clip(ShapeDefaults.Medium)
                    .width(160.dp)
                    .height(160.dp),
                contentDescription = "not Connected",
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                colorFilter = colorFilter
            )


            Button(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 60.dp),
                onClick = {
                    onCheckState()
                }
            ) {
                Text(context.resources.getString(R.string.chkNetWork_msg))
            }


        }


    }

}

