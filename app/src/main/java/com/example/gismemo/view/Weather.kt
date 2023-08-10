package com.example.gismemo.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.gismemo.data.RepositoryProvider
import com.example.gismemo.db.*
import com.example.gismemo.db.entity.MEMO_FILE_TBL
import com.example.gismemo.shared.composables.CheckPermission
import com.example.gismemo.shared.composables.PermissionRequiredCompose
import com.example.gismemo.shared.composables.PermissionRequiredComposeFuncName
import com.example.gismemo.ui.theme.GISMemoTheme
import com.example.gismemo.utils.getDeviceLocation
import com.example.gismemo.viewmodel.ListViewModel
import com.example.gismemo.viewmodel.SpeechToTextViewModel
import com.example.gismemo.viewmodel.WeatherViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch


@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission", "SuspiciousIndentation")
@Composable
fun WeatherContent(isSticky:Boolean = false ){

    val permissions = listOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val multiplePermissionsState = rememberMultiplePermissionsState( permissions)
    CheckPermission(multiplePermissionsState = multiplePermissionsState)

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current

    val db = LocalLuckMemoDB.current
    val viewModel = remember {
        WeatherViewModel(repository = RepositoryProvider.getRepository().apply { database = db }  )
    }

    val weatherData = viewModel._currentWeatheStaterFlow.collectAsState()

    if( weatherData.value == null){
        context.getDeviceLocation {it?.let {
            lifecycleOwner.lifecycleScope.launch {
                viewModel.searchWeather(it)
            }
        }}

    }

    var isGranted by mutableStateOf(true)
    permissions.forEach { chkPermission ->
        isGranted = isGranted
                &&  ( multiplePermissionsState.permissions.find { it.permission == chkPermission  }?.status?.isGranted ?: false )
    }

    PermissionRequiredCompose(
        isGranted = isGranted,
        multiplePermissions = permissions ,
        viewType = PermissionRequiredComposeFuncName.Weather
    ) {

        Column(
            modifier = Modifier
                .background(color = Color.White)
        ) {

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

@Composable
fun WeatherView(
    item: CURRENTWEATHER_TBL,
    modifier:Modifier = Modifier
){

    Column(
        modifier = modifier
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item.toTextHeadLine()

        Text( item.toTextHeadLine()
            , modifier = Modifier.fillMaxWidth()
            , textAlign = TextAlign.Center
            , style  = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        )

        Text(item.toTextWeatherDesc()
            , modifier = Modifier.fillMaxWidth()
            , textAlign = TextAlign.Center
            , style  = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        )

        Row(
            modifier = Modifier.align(Alignment.Start)

        ) {

            Icon (
                imageVector = Icons.Outlined.LightMode
                ,contentDescription = "clear sky"
                , modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth(0.3f)
                    .aspectRatio(1.5f)
                , tint = Color(255, 165, 0)
            )


            Column (modifier = Modifier.fillMaxWidth()){
                WeatherItem(id =  Icons.Outlined.WbTwilight, desc = item.toTextSun())
                WeatherItem(id = Icons.Outlined.DeviceThermostat, desc = item.toTextTemp())
                WeatherItem(id = Icons.Outlined.WindPower, desc = item.toTextWind())
                WeatherItem(id = Icons.Outlined.Storm, desc = item.toTextWeather())
            }


        }

    }

}

@Composable
fun WeatherViewLandScape(
    item: CURRENTWEATHER_TBL,
    modifier:Modifier = Modifier
) {

    Column(
        modifier = modifier
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item.toTextHeadLine()

        Text(
            item.toTextHeadLine(),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        )

        Text(
            item.toTextWeatherDesc(),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        )

        Spacer(modifier = Modifier.padding(vertical = 10.dp))

        Icon (
            imageVector = Icons.Outlined.LightMode
            ,contentDescription = "clear sky"
            , modifier = Modifier.fillMaxWidth(0.5f).aspectRatio(1f)
            , tint = Color(255, 165, 0)
        )

        Spacer(modifier = Modifier.padding(vertical = 10.dp))

        Column (
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            WeatherItem(id =  Icons.Outlined.WbTwilight, desc = item.toTextSun())
            WeatherItem(id = Icons.Outlined.DeviceThermostat, desc = item.toTextTemp())
            WeatherItem(id = Icons.Outlined.WindPower, desc = item.toTextWind())
            WeatherItem(id = Icons.Outlined.Storm, desc = item.toTextWeather())
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
            , tint = Color.Gray

        )

        Text( desc
            , modifier = Modifier
            , textAlign = TextAlign.Start
            , style  = TextStyle(color = Color.Black, fontWeight = FontWeight.Light)
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
