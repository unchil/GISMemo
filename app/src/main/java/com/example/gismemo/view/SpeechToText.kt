package com.example.gismemo.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.rounded.HighlightOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.gismemo.LocalUsableHaptic
import com.example.gismemo.data.RepositoryProvider
import com.example.gismemo.db.LocalLuckMemoDB
import com.example.gismemo.shared.composables.*
import com.example.gismemo.shared.utils.FileManager
import com.example.gismemo.ui.theme.GISMemoTheme
import com.example.gismemo.viewmodel.ListViewModel
import com.example.gismemo.viewmodel.SpeechToTextViewModel
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import java.io.FileOutputStream



val recognizerIntent =  {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

    intent.putExtra(
        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
        RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
    )

    intent.putExtra(
        RecognizerIntent.EXTRA_LANGUAGE,
        java.util.Locale.getDefault()
    )
    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Talk")

    /*
    intent.putExtra("android.speech.extra.GET_AUDIO", true)
    intent.putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR")

     */
}


@Composable
fun AudioTextView(data: Pair<String, List<Uri>>){

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




    var speechInput =  rememberSaveable { data.first }
    val recordingUri: List<Uri>  = rememberSaveable { data.second }





    Column(modifier = Modifier.fillMaxSize().padding( 20.dp),
        horizontalAlignment= Alignment.CenterHorizontally
    ) {


        OutlinedTextField(
            modifier = Modifier
                .height(220.dp)
                .fillMaxWidth(),

            singleLine = false,
            trailingIcon = {
                IconButton(
                    onClick = {
                        hapticProcessing()
                    speechInput = ""
                }) {
                    Icon(
                        imageVector = Icons.Rounded.HighlightOff,
                        contentDescription = "Clear"
                    )
                }
            },
            value = speechInput,
            onValueChange = { speechInput = it },
            label = { Text("Speech To Text") },
            shape = OutlinedTextFieldDefaults.shape,
            keyboardActions = KeyboardActions.Default
        )


        Box( modifier = Modifier
            .height(280.dp)
            .fillMaxWidth()

        ) {
            ExoplayerCompose(uriList = recordingUri, isVisibleAmplitudes = true)
        }






    }

}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SpeechRecognizerCompose(navController: NavController   ) {

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


    val permissions =  remember { listOf(Manifest.permission.RECORD_AUDIO) }
    val multiplePermissionsState = rememberMultiplePermissionsState(permissions)
    CheckPermission(multiplePermissionsState = multiplePermissionsState)

    val context = LocalContext.current

    val db = LocalLuckMemoDB.current
    val viewModel = remember {
        SpeechToTextViewModel (repository = RepositoryProvider.getRepository().apply { database = db }  )
    }

    val currentBackStack by navController.currentBackStackEntryAsState()

    var recordingUri: Uri?  by rememberSaveable { mutableStateOf(null) }


    val currentAudioTextList = viewModel._currentAudioText.value.toMutableList()

    val audioTextList:MutableList<Pair<String, List<Uri>>>
            =  rememberSaveable { currentAudioTextList }


    val audioTextData:Pair<MutableState<String>, MutableList<Uri>>
            =  rememberSaveable { Pair(mutableStateOf(""), mutableListOf()) }


    val startLauncherRecognizerIntent = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()  ) {

        if(it.resultCode == Activity.RESULT_OK){

            val result = it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            audioTextData.first.value = audioTextData.first.value + result?.get(0).toString() + " "

            it.data?.data?.let {uri ->


                FileManager.getFilePath(context, FileManager.Companion.OUTPUTFILE.AUDIO).let{outputFilePath ->
                    context.contentResolver.openInputStream(uri)?.copyTo(FileOutputStream(outputFilePath))

                    audioTextData.second.add(outputFilePath.toUri())

                    recordingUri = outputFilePath.toUri()


                }


            }
        }
    }


    val  recognizerIntent =  remember {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
        )

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Talk")

        intent.putExtra("android.speech.extra.GET_AUDIO", true)
        intent.putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR")

    }

    val scrollState = rememberScrollState()

    val backStack = {
        if (audioTextData.first.value.length > 0) {
            audioTextList.add(
                Pair(
                    audioTextData.first.value,
                    audioTextData.second.toList()
                )
            )

            viewModel.onEvent(
                SpeechToTextViewModel.Event.SetAudioText(data = audioTextList)
            )
        }
        navController.popBackStack()
    }


    var isGranted by mutableStateOf(true)
    permissions.forEach { chkPermission ->
        isGranted =  isGranted && multiplePermissionsState.permissions.find { it.permission == chkPermission }?.status?.isGranted
            ?: false
    }



    PermissionRequiredCompose(
            isGranted = isGranted,
            multiplePermissions = permissions,
            viewType = PermissionRequiredComposeFuncName.SpeechToText
    ) {

            Column(modifier = Modifier
                .verticalScroll(state = scrollState)
                .fillMaxWidth().padding(  20.dp),
                horizontalAlignment= Alignment.CenterHorizontally
            ) {


                OutlinedTextField(
                    modifier = Modifier
                        .height(220.dp)
                        .fillMaxWidth(),
                    singleLine = false,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                hapticProcessing()
                                audioTextData.first.value  = ""
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.HighlightOff,
                                contentDescription = "Clear"
                            )
                        }
                    },
                    value = audioTextData.first.value ,
                    onValueChange = { audioTextData.first.value  = it },
                    label = { Text("Speech To Text") },
                    shape = OutlinedTextFieldDefaults.shape,
                    keyboardActions = KeyboardActions.Default
                )


                Row {

                    IconButton(
                        modifier = Modifier.scale(1.5f),
                        onClick = {
                            hapticProcessing()
                            startLauncherRecognizerIntent.launch(recognizerIntent)
                                  },
                        content = {
                            Icon(
                                modifier = Modifier,
                                imageVector = Icons.Outlined.Mic,
                                contentDescription = "Voice Recording"
                            )
                        }
                    )
                }






                recordingUri?.let {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                      ) {
                        ExoplayerCompose(uri  = it, isVisibleAmplitudes = true)
                    }
                }

            }
        }

    BackHandler {
        backStack()
    }


}



@OptIn(ExperimentalAnimationApi::class)
@Preview
@Composable
fun PrevSpeechRecognizerCompose(){

    val navController = rememberAnimatedNavController()
    val permissionsManager = PermissionsManager()


    CompositionLocalProvider(LocalPermissionsManager provides permissionsManager) {

            GISMemoTheme {
                Surface(
                    modifier = Modifier,
                    color = MaterialTheme.colors.onPrimary,
                    contentColor = MaterialTheme.colors.primary
                ) {
                    Box(
                        modifier = Modifier
                    ) {
                        SpeechRecognizerCompose(navController = navController)
                    }
                }
            }


    }
}
