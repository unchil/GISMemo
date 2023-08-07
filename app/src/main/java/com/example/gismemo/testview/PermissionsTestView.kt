package com.example.gismemo.view

import android.Manifest
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.compose.rememberNavController
import coil.size.Size
import com.example.gismemo.ChkNetWork
import com.example.gismemo.R
import com.example.gismemo.shared.composables.CheckPermission
import com.example.gismemo.shared.composables.LocalPermissionsManager
import com.example.gismemo.shared.composables.PermissionRequiredCompose
import com.example.gismemo.shared.composables.PermissionsManager
import com.example.gismemo.ui.theme.GISMemoTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionTestView() {

    val navController = rememberNavController()
    val permissions = listOf(Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE)
    val multiplePermissionsState = rememberMultiplePermissionsState(permissions)
    CheckPermission(multiplePermissionsState = multiplePermissionsState)

    var isGranted by mutableStateOf(true)

    permissions.forEach { chkPermission ->
        isGranted = isGranted
                && (multiplePermissionsState.permissions.find { it.permission == chkPermission }?.status?.isGranted
            ?: false)
    }

    val context = LocalContext.current

    fun checkInternetConnected() :Boolean  {
        ( context.applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
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



    var isConnect by remember{ mutableStateOf( checkInternetConnected() )}

    Box(modifier = Modifier.fillMaxSize()) {

        if(!isConnect) {
            ChkNetWork(
                onCheckState ={  isConnect = checkInternetConnected() }
            )
        } else {
            IntroView(navController = navController)
        }



    }

}



@RequiresApi(Build.VERSION_CODES.P)
@Preview
@Composable
fun PrevPermissionTestView() {

    val permissionsManager = PermissionsManager()

    GISMemoTheme {
        Surface(  modifier = Modifier.fillMaxSize(),  color = MaterialTheme.colorScheme.background) {
            CompositionLocalProvider( LocalPermissionsManager provides permissionsManager) {

                PermissionTestView()

            }
        }
    }

}