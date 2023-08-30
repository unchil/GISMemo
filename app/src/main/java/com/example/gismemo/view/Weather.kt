package com.example.gismemo.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gismemo.LocalUsableDarkMode
import com.example.gismemo.R
import com.example.gismemo.data.RepositoryProvider
import com.example.gismemo.db.*
import com.example.gismemo.shared.composables.CheckPermission
import com.example.gismemo.shared.composables.PermissionRequiredCompose
import com.example.gismemo.shared.composables.PermissionRequiredComposeFuncName
import com.example.gismemo.ui.theme.GISMemoTheme
import com.example.gismemo.viewmodel.WeatherViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices


private fun getWeatherIcon(type:String):Int {
    return when(type){
        "01d" -> R.drawable.ic_openweather_01d
        "01n" -> R.drawable.ic_openweather_01n
        "02d" -> R.drawable.ic_openweather_02d
        "02n" -> R.drawable.ic_openweather_02n
        "03d" -> R.drawable.ic_openweather_03d
        "03n" -> R.drawable.ic_openweather_03n
        "04d" -> R.drawable.ic_openweather_04d
        "04n" -> R.drawable.ic_openweather_04n
        "09d" -> R.drawable.ic_openweather_09d
        "09n" -> R.drawable.ic_openweather_09n
        "10d" -> R.drawable.ic_openweather_10d
        "10n" -> R.drawable.ic_openweather_10n
        "11d" -> R.drawable.ic_openweather_11d
        "11n" -> R.drawable.ic_openweather_11n
        "13d" -> R.drawable.ic_openweather_13d
        "13n" -> R.drawable.ic_openweather_13n
        "50d" -> R.drawable.ic_openweather_50d
        "50n" -> R.drawable.ic_openweather_50n
        else -> R.drawable.ic_openweather_unknown
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission", "SuspiciousIndentation")
@Composable
fun WeatherContent(isSticky:Boolean = false , onCheckLocationService:((Boolean)->Unit)? = null){

    val permissions = listOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val multiplePermissionsState = rememberMultiplePermissionsState( permissions)
    CheckPermission(multiplePermissionsState = multiplePermissionsState)

    var isGranted by mutableStateOf(true)
    permissions.forEach { chkPermission ->
        isGranted =   isGranted && multiplePermissionsState.permissions.find { it.permission == chkPermission  }?.status?.isGranted ?: false
    }

    PermissionRequiredCompose(
        isGranted = isGranted,
        multiplePermissions = permissions ,
        viewType = PermissionRequiredComposeFuncName.Weather
    ) {

    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val db = LocalLuckMemoDB.current
    val viewModel = remember {
        WeatherViewModel(repository = RepositoryProvider.getRepository().apply { database = db }  )
    }

        val fusedLocationProviderClient = remember {
            LocationServices.getFusedLocationProviderClient(context)
        }


        var isSuccessfulTask by remember { mutableStateOf( false ) }
        var checkCurrentLocation by remember { mutableStateOf(true) }


        LaunchedEffect(key1 =  checkCurrentLocation){
            if(checkCurrentLocation) {
                checkCurrentLocation = false
                fusedLocationProviderClient.lastLocation.addOnCompleteListener( context.mainExecutor) { task ->
                    if (task.isSuccessful && task.result != null ) {
                        isSuccessfulTask = true
                        viewModel.searchWeather(task.result.toLatLng())
                    } else {
                        onCheckLocationService?.let {
                            it(false)

                        }
                    }
                }
            }
        }


        val weatherData = viewModel._currentWeatheStaterFlow.collectAsState()

        Column(
            modifier = Modifier
                .clickable(false, null, null) {}
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.background)
            ,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            androidx.compose.animation.AnimatedVisibility  (!isSuccessfulTask  ) {

                androidx.compose.material.IconButton(

                    onClick = { checkCurrentLocation = true },
                    content = {
                        Row(
                            modifier = Modifier,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                modifier = Modifier.padding(end = 10.dp),
                                imageVector = Icons.Outlined.LocationSearching,
                                contentDescription = "LocationSearching"
                            )
                            Text(text = context.resources.getString(R.string.weather_location_searching))
                        }
                    })


            }




            androidx.compose.animation.AnimatedVisibility (weatherData.value != null){
                weatherData.value?.let {
                    when (configuration.orientation) {
                        Configuration.ORIENTATION_PORTRAIT -> {
                            WeatherView(it)
                        }
                        else -> {
                            if (isSticky) {
                                WeatherView(it)
                            } else {
                                WeatherViewLandScape(it)
                            }
                        }
                    }
                }
            }



        }

    }


}

@Composable
fun WeatherView(
    item: CURRENTWEATHER_TBL,
    modifier:Modifier = Modifier
){

    val context = LocalContext.current
    val isUsableDarkMode = LocalUsableDarkMode.current
    val colorFilter:ColorFilter? = if(isUsableDarkMode){
        tint(Color.LightGray.copy(alpha = 0.3f), blendMode = BlendMode.Darken)
    }else {
        null
    }



    Column(
        modifier = modifier
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Text( item.toTextHeadLine()
            , modifier = Modifier.fillMaxWidth()
            , textAlign = TextAlign.Center
            , style  = MaterialTheme.typography.titleMedium
        )

        Text(item.toTextWeatherDesc()
            , modifier = Modifier.fillMaxWidth()
            , textAlign = TextAlign.Center
            , style  = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.align(Alignment.Start)

        ) {


/*
            Icon (
            //    imageVector = Icons.Outlined.LightMode
                painter = painterResource(id = getWeatherIcon(item.icon))
                ,contentDescription = "weather"
                , modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth(0.3f)
                    .aspectRatio(1.5f)
                , tint = Color(255, 165, 0)
            )


 */



            Image(
                painter =  painterResource(id = getWeatherIcon(item.icon)),
                modifier = Modifier .clip(ShapeDefaults.Small)
                    .fillMaxWidth(0.2f),
                contentDescription = "weather",
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                colorFilter = colorFilter
            )




            Column (modifier = Modifier.padding(start= 10.dp)){
                WeatherItem(id =  Icons.Outlined.WbTwilight, desc = item.toTextSun(context.resources::getString))
                WeatherItem(id = Icons.Outlined.DeviceThermostat, desc = item.toTextTemp(context.resources::getString))
                WeatherItem(id = Icons.Outlined.WindPower, desc = item.toTextWind(context.resources::getString))
                WeatherItem(id = Icons.Outlined.Storm, desc = item.toTextWeather(context.resources::getString))
            }


        }

    }

}

@Composable
fun WeatherViewLandScape(
    item: CURRENTWEATHER_TBL,
    modifier:Modifier = Modifier
) {
    val context = LocalContext.current

    val isUsableDarkMode = LocalUsableDarkMode.current
    val colorFilter:ColorFilter? = if(isUsableDarkMode){
        tint(Color.LightGray.copy(alpha = 0.3f), blendMode = BlendMode.Darken)
    }else {
        null
    }

    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            item.toTextHeadLine(),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            item.toTextWeatherDesc(),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )

/*
        Icon (
        //    imageVector = Icons.Outlined.LightMode
            painter = painterResource(id = getWeatherIcon(item.icon))
            ,contentDescription = "weather"
            , modifier = Modifier
                .fillMaxWidth(0.5f)
                .aspectRatio(1f)
            , tint = Color(255, 165, 0)
        )

 */

        Image(
            painter =  painterResource(id = getWeatherIcon(item.icon)),
            modifier = Modifier .clip(ShapeDefaults.Small)
                .fillMaxWidth(0.5f),
            contentDescription = "weather",
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            colorFilter = colorFilter
            )



        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp),
            horizontalAlignment = Alignment.Start
        ){
            WeatherItem(id =  Icons.Outlined.WbTwilight, desc = item.toTextSun(context.resources::getString))
            WeatherItem(id = Icons.Outlined.DeviceThermostat, desc = item.toTextTemp(context.resources::getString))
            WeatherItem(id = Icons.Outlined.WindPower, desc = item.toTextWind(context.resources::getString))
            WeatherItem(id = Icons.Outlined.Storm, desc = item.toTextWeather(context.resources::getString))
        }


    }

}

@Composable
fun WeatherItem(id: ImageVector, desc: String){

    Row( modifier = Modifier) {

        Icon(  imageVector = id
            , contentDescription = "desc"
            , modifier = Modifier
                .height(20.dp)
                .padding(end = 10.dp)

        )

        Text( desc
            , modifier = Modifier
            , textAlign = TextAlign.Start
            , style  = TextStyle( fontWeight = FontWeight.Light)
        )
    }


}

@Preview
@Composable
fun PrevWeatherContent(){

    GISMemoTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            WeatherContent(isSticky = false)
        }
    }
}
