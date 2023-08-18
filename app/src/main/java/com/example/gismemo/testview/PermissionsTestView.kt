package com.example.gismemo.view

import android.Manifest
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.gismemo.shared.composables.*
import com.example.gismemo.ui.theme.GISMemoTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionTestView() {

    val navController = rememberNavController()
    val permissions = listOf(        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION)
    val multiplePermissionsState = rememberMultiplePermissionsState(permissions)
    CheckPermission(multiplePermissionsState = multiplePermissionsState)

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


    }


}



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