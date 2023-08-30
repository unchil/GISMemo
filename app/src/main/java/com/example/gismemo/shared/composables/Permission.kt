package com.example.gismemo.shared.composables

import android.content.Context
import android.content.Intent
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.hardware.biometrics.BiometricPrompt
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.ShapeDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import coil.size.Size
import com.example.gismemo.R
import com.example.gismemo.model.BiometricCheckType
import com.example.gismemo.model.getTitle
import com.example.gismemo.view.ImageViewer
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState


fun biometricPrompt(
    context: Context,
    bioMetricCheckType: BiometricCheckType,
    onResult: (isSucceeded:Boolean, bioMetricCheckType: BiometricCheckType, errorMsg:String?  ) ->Unit
){

    val biometricPrompt = BiometricPrompt.Builder(context)
        .apply {
            setTitle(bioMetricCheckType.getTitle(context.resources::getString).first)
            setSubtitle(bioMetricCheckType.getTitle(context.resources::getString).second)
            setDescription(context.resources.getString(R.string.biometric_desc))
            //BiometricPrompt.PromptInfo.Builder 인스턴스에서는 setNegativeButtonText()와 setAllowedAuthenticators(... or DEVICE_CREDENTIAL)를 동시에 호출할 수 없습니다.
            setAllowedAuthenticators( BIOMETRIC_STRONG  or DEVICE_CREDENTIAL )
            //   setNegativeButton("취소", context.mainExecutor, { _ , _ ->   })

        }.build()

    biometricPrompt.authenticate(android.os.CancellationSignal(), context.mainExecutor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onResult(false, bioMetricCheckType,  errString.toString())
            }
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onResult(false, bioMetricCheckType, context.resources.getString(R.string.biometric_err_msg))
            }
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onResult(true, bioMetricCheckType, null)
            }
        }
    )


}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CheckPermission( multiplePermissionsState: MultiplePermissionsState){

    val context = LocalContext.current
    val permissionsManager = LocalPermissionsManager.current
    val coroutineScope = rememberCoroutineScope()
    val permissionAction = permissionsManager.action.collectAsState()

    when( permissionAction.value) {

        PermissionsManager.Action.NO_ACTION -> {  }

        PermissionsManager.Action.SHOW_RATIONALE -> {
            PermissionRationaleDialog(
                message = context.resources.getString(R.string.permission_rationale_msg),
                title =  context.resources.getString(R.string.permission_rationale_title),
                primaryButtonText = "REQUEST",
                onOkTapped = {
                    permissionsManager.getPermissionActionNew(multiplePermissionsState, coroutineScope)
                }
            )
        }

        PermissionsManager.Action.SHOW_SETTING -> {
            ShowGotoSettingsDialog(
                title = context.resources.getString(R.string.permission_setting_title),
                message = context.resources.getString(R.string.permission_setting_msg),
                onSettingsTapped = {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:" + context.packageName)
                        context.startActivity(this)
                    }
                }
            )
        }

    }


}


@Composable
fun PermissionRationaleDialog(
    message: String,
    title: String,
    primaryButtonText: String,
    onOkTapped: () -> Unit
) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.body1,
                    color = Color.Black
                )
            },
            buttons = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Text(
                        text = primaryButtonText.uppercase(),
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .clickable { onOkTapped() },
                        style = MaterialTheme.typography.button,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        )

}


@Composable
fun ShowGotoSettingsDialog(
    title: String,
    message: String,
    onSettingsTapped: () -> Unit,
) {

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = {

        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle2,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Bold
            )
        },
        buttons = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    text = context.resources.getString(R.string.showGotoSettingsDialog_title),
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .clickable { onSettingsTapped() },
                    style = MaterialTheme.typography.button,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}



@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequiredCompose(
    isGranted : Boolean,
    multiplePermissions : List<String>,
    viewType:PermissionRequiredComposeFuncName? = null,
    content: @Composable () -> Unit) {


    if(isGranted){
        content()
    }else{

        val (id:Int, message:String ) = if ( viewType == null) {
            Pair(
                PermissionRequiredComposeFuncName.Weather.getDrawable(),
                "PERMISSION REQUEST"
            )
        } else {
            when (viewType) {
                PermissionRequiredComposeFuncName.SpeechToText -> {
                    Pair(
                        PermissionRequiredComposeFuncName.SpeechToText.getDrawable() ,
                        PermissionRequiredComposeFuncName.SpeechToText.getTitle()
                    )
                }
                PermissionRequiredComposeFuncName.Weather -> {
                    Pair(
                        PermissionRequiredComposeFuncName.Weather.getDrawable(),
                        PermissionRequiredComposeFuncName.Weather.getTitle()
                    )
                }
                PermissionRequiredComposeFuncName.Camera ->  {
                    Pair(
                        PermissionRequiredComposeFuncName.Camera.getDrawable(),
                        PermissionRequiredComposeFuncName.Camera.getTitle()
                    )
                }
                PermissionRequiredComposeFuncName.MemoMap ->  {
                    Pair(
                        PermissionRequiredComposeFuncName.MemoMap.getDrawable(),
                        PermissionRequiredComposeFuncName.MemoMap.getTitle()
                    )
                }
            }
        }

        val permissionsManager = LocalPermissionsManager.current
        val multiplePermissionsState = rememberMultiplePermissionsState(
            multiplePermissions
        )
        val coroutineScope = rememberCoroutineScope()

        Box(
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth()
                .padding(10.dp)
                .border(width = 1.dp, color = MaterialTheme.colors.onSurface, shape = ShapeDefaults.Small),
            contentAlignment = Alignment.Center
        ) {

            //    Image(painter = painterResource(id = id), contentDescription = "")
            ImageViewer(data = id, size = Size.ORIGINAL)
            Button(
                onClick = {
                    //onClickEvent()
                    permissionsManager.getPermissionActionNew(multiplePermissionsState, coroutineScope)
                }
            ){
                Text(text = message)
            }
        }



    }
}


@Composable
fun PermissionRequiredComposeNew(
    isGranted : Boolean,
    viewType:PermissionRequiredComposeFuncName,
    onClickEvent: () -> Unit,
    content: @Composable () -> Unit) {


    val (id:Int, message:String ) = when(viewType){

        PermissionRequiredComposeFuncName.SpeechToText -> {
            Pair(
                PermissionRequiredComposeFuncName.SpeechToText.getDrawable() ,
                PermissionRequiredComposeFuncName.SpeechToText.getTitle()
            )
        }
        PermissionRequiredComposeFuncName.Weather -> {
            Pair(
                PermissionRequiredComposeFuncName.Weather.getDrawable(),
                PermissionRequiredComposeFuncName.Weather.getTitle()
            )
        }
        PermissionRequiredComposeFuncName.Camera ->  {
            Pair(
                PermissionRequiredComposeFuncName.Camera.getDrawable(),
                PermissionRequiredComposeFuncName.Camera.getTitle()
            )
        }

        PermissionRequiredComposeFuncName.MemoMap ->  {
            Pair(
                PermissionRequiredComposeFuncName.MemoMap.getDrawable(),
                PermissionRequiredComposeFuncName.MemoMap.getTitle()
            )
        }

    }


    if(isGranted){
        content()
    }else{

        Box(
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth()
                .padding(10.dp)
                .border(width = 1.dp, color = Color.Black, shape = ShapeDefaults.Small),
            contentAlignment = Alignment.Center
        ) {

            //    Image(painter = painterResource(id = id), contentDescription = "")
            ImageViewer(data = id, size = Size.ORIGINAL)
            Button( onClick = {
                onClickEvent()}    ){
                Text(text = message)
            }
        }



    }
}






enum class PermissionRequiredComposeFuncName {
    SpeechToText, Weather, Camera, MemoMap
}

fun PermissionRequiredComposeFuncName.getTitle(): String {
    return when(this.name) {
        PermissionRequiredComposeFuncName.SpeechToText.name -> {"REQUEST: RECORD_AUDIO"}
        PermissionRequiredComposeFuncName.Weather.name -> {"REQUEST: LOCATION"}
        PermissionRequiredComposeFuncName.Camera.name -> {"REQUEST : CAMERA, RECORD_AUDIO"}
        PermissionRequiredComposeFuncName.MemoMap.name -> {"REQUEST : LOCATION"}
        else -> {"PERMISSION REQUEST :"}
    }
}

fun PermissionRequiredComposeFuncName.getDrawable(): Int {
    return when(this.name) {
        PermissionRequiredComposeFuncName.SpeechToText.name -> {
            R.drawable.speechrecognizer}
        PermissionRequiredComposeFuncName.Weather.name -> {
            R.drawable.weathercontent}
        PermissionRequiredComposeFuncName.Camera.name -> {
            R.drawable.camera}
        PermissionRequiredComposeFuncName.MemoMap.name -> {
            R.drawable.memomap}
        else -> {0}
    }
}


