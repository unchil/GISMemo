package com.example.gismemo.view


import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.text.format.DateUtils
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.gismemo.shared.composables.*
import com.example.gismemo.shared.utils.FileManager
import com.example.gismemo.ui.theme.GISMemoTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.linc.audiowaveform.AudioWaveform
import com.linc.audiowaveform.model.AmplitudeType
import com.linc.audiowaveform.model.WaveformAlignment
import kotlinx.coroutines.*
import java.io.File
import java.util.*


@RequiresApi(Build.VERSION_CODES.S)
fun Context.getMediaRecorder():MediaRecorder {
    return MediaRecorder(this).apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.OGG)
        setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
    }
}


@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceRecorderCompose(){

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionsManager = LocalPermissionsManager.current


  //  val viewModel = JetpackcomposeCameraViewModel( fileManager = FileManager(context) )

    val permissions =
        remember { listOf(Manifest.permission.RECORD_AUDIO) }

    val multiplePermissionsState = rememberMultiplePermissionsState(permissions)

    CheckPermission(multiplePermissionsState = multiplePermissionsState)

    var recordingUri:Uri?  by remember { mutableStateOf(null) }

    val recordingStarted: MutableState<Boolean> = remember { mutableStateOf(false) }

    val recordingStatus:MutableState<RecordingStatus> = remember {
         mutableStateOf(RecordingStatus.Idle)
    }

    var voiceRecorder:MediaRecorder? by remember { mutableStateOf(null) }


    val  startRecording = {
        context.getMediaRecorder().apply{
            voiceRecorder = this


            FileManager.getFilePath(context = context, outputfile = FileManager.Companion.OUTPUTFILE.AUDIO).let {
                setOutputFile(File(it))
                recordingUri = it.toUri()
            }


/*
            viewModel.getFilePathNew(outputfile = JetpackcomposeCameraViewModel.OUTPUTFILE.AUDIO).let {
                setOutputFile(File(it))
                recordingUri = it.toUri()
            }

 */
            prepare()
            start()
        }.also {
            recordingStatus.value = RecordingStatus.InProgress
            recordingStarted.value = true
        }
    }

    val  pauseRecording = {
        voiceRecorder?.let {
            it.pause()
            recordingStatus.value = RecordingStatus.Paused
        }
    }

    val  resumeRecording = {
        voiceRecorder?.let {
            it.resume()
            recordingStatus.value = RecordingStatus.InProgress
        }
    }

    val  stopRecording = {
        voiceRecorder?.let { recorder ->
            if ( recordingStatus.value == RecordingStatus.Paused) {
                recorder.resume()
            }
            recorder.stop()
            recorder.reset()
            recorder.release()
            voiceRecorder = null
            recordingStarted.value = false
            recordingStatus.value = RecordingStatus.Idle
        }
    }

    var isGranted by mutableStateOf(true)
    permissions.forEach { chkPermission ->
        isGranted = isGranted && multiplePermissionsState.permissions.find { it.permission == chkPermission }?.status?.isGranted
            ?: false
    }


    PermissionRequiredCompose(
        isGranted = isGranted,
        multiplePermissions = permissions,
        viewType = PermissionRequiredComposeFuncName.SpeechToText
    ) {

        Column (modifier = Modifier.fillMaxWidth()){


            TimerNew(
                status =   recordingStatus.value,
                isStarted = recordingStarted.value)


            recordingUri?.let {
               AudioWaveformNew(status = recordingStatus.value, isStarted = recordingStarted.value, fileUri = it)
            }


            Row(
                modifier = Modifier
                    .padding(vertical = 20.dp)
                    .align(alignment = Alignment.CenterHorizontally)
            ) {

                if(recordingStarted.value){

                    if(recordingStatus.value == RecordingStatus.InProgress){
                        IconButton(
                            modifier = Modifier.scale(1.5f),
                            onClick = {
                                pauseRecording()
                            },
                            content = {
                                Icon(
                                    modifier = Modifier,
                                    imageVector = Icons.Outlined.PauseCircle,
                                    contentDescription = "recording pause"
                                )
                            }
                        )

                    } else {

                        IconButton(
                            modifier = Modifier.scale(1.5f),
                            onClick = {
                                resumeRecording()
                            },
                            content = {
                                Icon(
                                    modifier = Modifier,
                                    imageVector = Icons.Outlined.PlayCircle,
                                    contentDescription = "recording resume"
                                )
                            }
                        )
                    }



                }


                if(!recordingStarted.value) {

                    IconButton(
                        modifier = Modifier.scale(1.5f),
                        onClick = {
                            startRecording()
                        },
                        content = {
                            Icon(
                                modifier = Modifier,
                                imageVector = Icons.Outlined.Mic,
                                contentDescription = "recording start"
                            )
                        }
                    )

                }else {


                    IconButton(
                        modifier = Modifier.scale(1.5f),
                        onClick = {
                           stopRecording()
                       },
                        content = {
                            Icon(
                                modifier = Modifier,
                                imageVector = Icons.Outlined.MicOff,
                                contentDescription = "recording stop"
                            )
                        }
                    )
                }



            }


            recordingUri?.let {
                if(!recordingStarted.value)   ExoplayerCompose(uriList = listOf(it)  , isVisibleAmplitudes = true)
            }

        }


    }
}



@Composable
fun AudioWaveformNew(status:RecordingStatus, isStarted:Boolean, fileUri:Uri) {

    val context = LocalContext.current
    var mediaItemAmplitudes:Amplitudes  by  remember{ mutableStateOf( emptyList() ) }
    val INTERVAL = 100L

    LaunchedEffect(key1 = status, key2 = isStarted){
        while(isStarted){
            delay(INTERVAL)
            when(status){
                RecordingStatus.Idle -> {}
                RecordingStatus.InProgress -> {
                    mediaItemAmplitudes  = context.setAmplitudes(uri = fileUri)
                }
                RecordingStatus.Paused -> { }
            }

        }
    }

    AudioWaveform(
        modifier = Modifier,
        style = Fill,
        waveformAlignment = WaveformAlignment.Center,
        amplitudeType = AmplitudeType.Avg,
        //progressBrush = SolidColor(Color.Gray),
        progressBrush = Brush.linearGradient(colors = listOf(Color(0xff22c1c3), Color(0xfffdbb2d))),
        waveformBrush = SolidColor(Color.LightGray),
        spikeWidth = 1.dp,
        spikePadding = 1.dp,
        spikeRadius = 1.dp,
        spikeAnimationSpec = tween(0),
        progress = 1F,
        amplitudes = mediaItemAmplitudes,
        onProgressChange = {  },
        onProgressChangeFinished = { }
    )

}


@Composable
fun TimerNew( status:RecordingStatus, isStarted:Boolean) {

    var recordingSec:Int  by  remember {  mutableStateOf(0)  }
    val INTERVAL = 1000L

    LaunchedEffect(key1 = status, key2 = isStarted){
       while(isStarted){
           when(status){
               RecordingStatus.Idle -> {}
               RecordingStatus.InProgress -> {
                   recordingSec = recordingSec + 1
               }
               RecordingStatus.Paused -> { }
           }
           delay(INTERVAL)
       }
        recordingSec = 0
    }

    Box(modifier = Modifier
        .fillMaxWidth()
        .height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = DateUtils.formatElapsedTime(recordingSec.toLong()),
                style  = TextStyle(color = Color.Black, fontWeight = FontWeight.Normal, fontSize = 30.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

}




@RequiresApi(Build.VERSION_CODES.S)
@Preview
@Composable
fun VoiceRecorderComposePrev(){


    val permissionsManager = PermissionsManager()
    CompositionLocalProvider(LocalPermissionsManager provides permissionsManager) {


        GISMemoTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {

                VoiceRecorderCompose()

            }
        }
    }
}

