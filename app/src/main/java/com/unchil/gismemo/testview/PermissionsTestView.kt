package com.unchil.gismemo.view

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.unchil.gismemo.shared.composables.*
import com.unchil.gismemo.ui.theme.GISMemoTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@SuppressLint("SwitchIntDef")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionTestView() {

    val navController = rememberNavController()
    val permissions = listOf(        Manifest.permission.USE_BIOMETRIC)
    val multiplePermissionsState = rememberMultiplePermissionsState(permissions)
    CheckPermission(multiplePermissionsState = multiplePermissionsState)

    var isGranted by mutableStateOf(true)
    permissions.forEach { chkPermission ->
        isGranted = isGranted &&  multiplePermissionsState.permissions.find { it.permission == chkPermission }?.status?.isGranted
            ?: false
    }

    val context = LocalContext.current



    PermissionRequiredCompose(
        isGranted = isGranted,
        multiplePermissions = permissions ,
        viewType = PermissionRequiredComposeFuncName.Weather
    ) {

        Text("dlkjfsdlkjfdlskjfldksj")

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