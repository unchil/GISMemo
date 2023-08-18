package com.example.gismemo.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.hardware.biometrics.BiometricManager
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.navigation.compose.rememberNavController
import com.example.gismemo.shared.composables.*
import com.example.gismemo.shared.launchIntent_Biometric_Enroll
import com.example.gismemo.ui.theme.GISMemoTheme
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

    var isGranted by mutableStateOf(false)
    permissions.forEach { chkPermission ->
        isGranted = multiplePermissionsState.permissions.find { it.permission == chkPermission }?.status?.isGranted
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