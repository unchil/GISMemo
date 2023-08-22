@file:OptIn(ExperimentalPermissionsApi::class)

package com.example.gismemo.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.util.Consumer
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.example.gismemo.LocalUsableHaptic
import com.example.gismemo.R
import com.example.gismemo.data.RepositoryProvider
import com.example.gismemo.db.LocalLuckMemoDB
import com.example.gismemo.model.WriteMemoDataType
import com.example.gismemo.navigation.GisMemoDestinations

import com.example.gismemo.shared.composables.*
import com.example.gismemo.shared.utils.FileManager
import com.example.gismemo.ui.theme.GISMemoTheme
import com.example.gismemo.viewmodel.CameraViewModel
import com.example.gismemo.viewmodel.SpeechToTextViewModel
import com.example.gismemo.viewmodel.WriteMemoViewModel
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun Long.fromNanoToSeconds() = (this / (1000 * 1000 * 1000)).toInt()


suspend fun Context.createVideoCaptureUseCase(
    lifecycleOwner: LifecycleOwner,
    cameraSelector: CameraSelector,
    previewView: PreviewView,
    @ImageCapture.FlashMode  flashMode: Int = ImageCapture.FLASH_MODE_AUTO,
    @TorchState.State  torchState: Int = TorchState.OFF,
): Pair<VideoCapture<Recorder>, ImageCapture> {


    val preview = androidx.camera.core.Preview.Builder().build()

    val qualitySelector = QualitySelector.from( Quality.UHD, FallbackStrategy.lowerQualityOrHigherThan(
        Quality.UHD)
    )

    val recorder = Recorder.Builder().setExecutor(mainExecutor)
        .setQualitySelector(qualitySelector)
        .build()

    val videoCapture = VideoCapture.withOutput(recorder)

    val imageCapture = ImageCapture.Builder()
        .setFlashMode(flashMode)
        .build()


    getCameraProvider().let { cameraProvider ->
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            videoCapture,
            imageCapture
        ).apply {
            cameraControl.enableTorch(torchState == TorchState.ON)

            preview.setSurfaceProvider(previewView.surfaceProvider)

        }

        return Pair(videoCapture, imageCapture)
    }

}

suspend fun Context.getCameraProvider() : ProcessCameraProvider = suspendCoroutine{ continuation ->
    ProcessCameraProvider.getInstance(this).also { listenableFuture ->
        listenableFuture.addListener(
            {continuation.resume(listenableFuture.get())},
            mainExecutor
        )
    }
}

sealed class RecordingStatus {
    object Idle : RecordingStatus()
    object InProgress : RecordingStatus()
    object Paused : RecordingStatus()
}



@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("RestrictedApi", "MissingPermission")
@Composable

fun CameraCompose( navController: NavController? = null   ) {



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



    val permissions =
        remember { listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO) }
    val multiplePermissionsState = rememberMultiplePermissionsState(permissions)
    CheckPermission(multiplePermissionsState = multiplePermissionsState)


    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val db = LocalLuckMemoDB.current
    val viewModel =   remember { CameraViewModel  (repository = RepositoryProvider.getRepository().apply { database = db }  ) }
 //   val viewModel =   remember { CameraViewModel(repository = RepositoryProvider.getRepository(context.applicationContext)) }


    val previewView: PreviewView = remember { PreviewView(context) }
    val videoCapture: MutableState<VideoCapture<Recorder>?> =   mutableStateOf(null)
    val imageCapture: MutableState<ImageCapture?> =  mutableStateOf(null)

    var videoRecording: Recording? = remember { null }

    val recordingStarted: MutableState<Boolean> = remember { mutableStateOf(false) }
    val audiioEnabled: MutableState<Boolean> = remember { mutableStateOf(true) }
    val cameraSelector: MutableState<CameraSelector> =  remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    val currentCameraInfo: MutableState<CameraInfo?> = remember { mutableStateOf(null) }
    val torchState: MutableState<Int> = remember { mutableStateOf(TorchState.OFF) }
    val flashMode: MutableState<Int> = remember { mutableStateOf(ImageCapture.FLASH_MODE_OFF) }
    val recordingStatus: MutableState<RecordingStatus> = remember {  mutableStateOf(RecordingStatus.Idle) }
    var recordingLength: Int by mutableStateOf(0)
    var photoPreviewData: Any by rememberSaveable { mutableStateOf(R.drawable.outline_perm_media_black_48) }

    val isDualCamera: MutableState<Boolean> = remember { mutableStateOf(true) }

    val isVideoRecording = remember{ mutableStateOf(false)}

    var videoUri : String?  by remember{ mutableStateOf(null)}


    lifecycleOwner.lifecycleScope.launch {

        context.createVideoCaptureUseCase(
            lifecycleOwner,
            cameraSelector.value,
            previewView,
            flashMode.value,
            torchState.value
        ).let {
            videoCapture.value = it.first
            imageCapture.value = it.second
        }

        val cameraInfos = context.getCameraProvider().availableCameraInfos
        isDualCamera.value = cameraInfos.size > 1
        currentCameraInfo.value = cameraInfos.find { cameraInfo ->
            cameraInfo.lensFacing == cameraSelector.value.lensFacing
        }

    }


/*
    LaunchedEffect(key1 = previewView ){
        context.createVideoCaptureUseCase(
            lifecycleOwner,
            cameraSelector.value,
            previewView,
            flashMode.value,
            torchState.value
        ).let {
            videoCapture.value = it.first
            imageCapture.value = it.second
        }

        val cameraInfos = context.getCameraProvider().availableCameraInfos
        isDualCamera.value = cameraInfos.size > 1
        currentCameraInfo.value = cameraInfos.find { cameraInfo ->
            cameraInfo.lensFacing == cameraSelector.value.lensFacing
        }
    }


 */





    val currentPhotoList = viewModel._currentPhoto.collectAsState().value.toMutableList()

    val photoList:MutableList<Uri>
            =  rememberSaveable { currentPhotoList }

    val currentVideoList = viewModel._currentVideo.collectAsState().value.toMutableList()

    val videoList:MutableList<Uri>
            =  rememberSaveable { currentVideoList }


    val imageCaptureListener =  object : ImageCapture.OnImageSavedCallback {
        override fun onError(error: ImageCaptureException) { }
        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            lifecycleOwner.lifecycleScope.launch {
                outputFileResults.savedUri?.let {uri ->
                    photoPreviewData = uri
                    photoList.add(uri)
                }
            }
        }
    }

    val  takePicture = {
        imageCapture.value?.let {

            FileManager.getFilePath(context = context, outputfile = FileManager.Companion.OUTPUTFILE.IMAGE).let {filePath->
                val outputFileOptions = ImageCapture.OutputFileOptions.Builder(File(filePath)).build()
                it.takePicture( outputFileOptions, context.mainExecutor, imageCaptureListener)
            }
        }
    }

     val videoRecordingListener = Consumer<VideoRecordEvent> { event ->
        when (event) {
            is VideoRecordEvent.Finalize -> {
                if (!event.hasError()) {
                    videoUri = event.outputResults.outputUri.encodedPath
                    videoList.add( event.outputResults.outputUri)
                    recordingLength = 0
                    videoRecording = null
                    takePicture()
                    isVideoRecording.value = true
                }
            }
            is VideoRecordEvent.Status -> {
                recordingLength = event.recordingStats.recordedDurationNanos.fromNanoToSeconds()
            }
        }
    }


    val takeVideo = {
        if( !recordingStarted.value){
            videoCapture.value?.let { videoCapture ->

                FileManager.getFilePath(context = context, outputfile = FileManager.Companion.OUTPUTFILE.VIDEO).let { filePath ->
                    recordingStatus.value = RecordingStatus.InProgress
                    recordingStarted.value = true
                    val outputFileOptions = FileOutputOptions.Builder(File(filePath)).build()
                    videoRecording = videoCapture.output.prepareRecording(context, outputFileOptions).apply {
                        if (audiioEnabled.value) withAudioEnabled()
                    } .start(context.mainExecutor, videoRecordingListener)
                }

            }
        }
    }

    val backStack = {

        viewModel.onEvent(CameraViewModel.Event.SetPhotoVideo(photoList, videoList))

        navController?.popBackStack()
    }

    var isGranted by mutableStateOf(true)
    permissions.forEach { chkPermission ->
        isGranted =  isGranted && multiplePermissionsState.permissions.find { it.permission == chkPermission }?.status?.isGranted
            ?: false
    }


    PermissionRequiredCompose(
        isGranted = isGranted,
        multiplePermissions = permissions,
        viewType = PermissionRequiredComposeFuncName.Camera,
    ) {



        Box(modifier = Modifier.fillMaxSize()) {



                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize(),
                    update = {

                    }
                )




            if (videoRecording == null) {
                VideoCameraHeader(
                    modifier = Modifier.align(Alignment.TopCenter),
                    showFlashIcon = currentCameraInfo.value?.hasFlashUnit() ?: false,
                    torchState = torchState.value,
                    onFlashTapped = {
                        hapticProcessing()
                        torchState.value = when (torchState.value) {
                            TorchState.OFF -> TorchState.ON
                            else -> TorchState.OFF
                        }
                        flashMode.value = when (flashMode.value) {
                            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_OFF
                            else -> ImageCapture.FLASH_MODE_OFF
                        }

                    },
                )
            } else {
                if (recordingStarted.value) {
                    Timer(
                        modifier = Modifier.align(Alignment.TopCenter),
                        seconds = recordingLength
                    )
                }
            }



            VideoCameraFooter(
                photoPrevData = photoPreviewData,
                modifier = Modifier.align(Alignment.BottomStart),
                recordingStatus = recordingStatus.value,
                showFlipIcon = isDualCamera.value,
                onRecordTapped = {
                    hapticProcessing()
                    takeVideo()
                },
                onPauseTapped = {
                    hapticProcessing()
                    videoRecording?.pause()
                    recordingStatus.value = RecordingStatus.Paused
                },
                onResumeTapped = {
                    hapticProcessing()
                    videoRecording?.resume()
                    recordingStatus.value = RecordingStatus.InProgress
                },
                onStopTapped = {
                    hapticProcessing()
                    videoRecording?.stop()
                    recordingStarted.value = false
                    recordingStatus.value = RecordingStatus.Idle
                },
                onFlipTapped = {
                    hapticProcessing()
                    if(videoRecording == null ) {
                        cameraSelector.value = when (cameraSelector.value) {
                            CameraSelector.DEFAULT_BACK_CAMERA -> CameraSelector.DEFAULT_FRONT_CAMERA
                            else -> CameraSelector.DEFAULT_BACK_CAMERA
                        }
                    }
                },
                onCaptureTapped = {
                    hapticProcessing()
                    isVideoRecording.value = false
                    takePicture()
                }  ,
                onPhotoPreviewTapped = { it ->
                    hapticProcessing()
                    when(it){
                        is Int -> { }
                        else -> {

                            when(isVideoRecording.value){
                                true -> {
                                    videoUri?.let {
                                        navController?.navigate(GisMemoDestinations.ExoPlayerView.createRoute( it))  }
                                }
                                false -> {
                                    navController?.navigate(GisMemoDestinations.PhotoPreview.createRoute(it))
                                }
                            }

                        }
                    }
                }
            )


        }

    }

    BackHandler {
        backStack()
    }

}


@Composable
fun VideoCameraHeader(
    modifier: Modifier = Modifier,
    showFlashIcon: Boolean,
    torchState: Int,
    onFlashTapped: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp)
            .then(modifier)
    ) {
        if (showFlashIcon) {
            CameraTorchIcon(modifier = modifier, torchState = torchState, onTapped = onFlashTapped)
        }

    }
}

@Composable
fun VideoCameraFooter(
    photoPrevData: Any,
    modifier: Modifier = Modifier,
    recordingStatus: RecordingStatus,
    showFlipIcon: Boolean,
    onRecordTapped: () -> Unit,
    onStopTapped: () -> Unit,
    onPauseTapped: () -> Unit,
    onResumeTapped: () -> Unit,
    onFlipTapped: () -> Unit,
    onCaptureTapped: () -> Unit,
    onPhotoPreviewTapped: (Any) -> Unit
) {

    Row(
        modifier = Modifier
            .background(color = Color.White.copy(alpha = 0.2f))
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 24.dp)
            .then(modifier),
        horizontalArrangement =  Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically

    ) {


        when(photoPrevData){
            is Int -> { }
            else -> {
                PhotoPreview(
                    data = photoPrevData,
                    onPhotoPreviewTapped = onPhotoPreviewTapped
                )
                Spacer(modifier = Modifier.padding(horizontal = 10.dp))
            }
        }

        when (recordingStatus) {

            RecordingStatus.Idle -> {
                CameraRecordIcon(
                    iconModifier = Modifier.size(40.dp),
                    onTapped = onRecordTapped
                )
            }
            RecordingStatus.InProgress -> {
                CameraPauseIcon(
                    iconModifier = Modifier.size(40.dp),
                    onTapped = onPauseTapped,
                )

                Spacer(modifier = Modifier.padding(horizontal = 10.dp))

                CameraStopIcon(
                    iconModifier = Modifier.size(40.dp),
                    onTapped = onStopTapped
                )
            }
            RecordingStatus.Paused -> {
                CameraPlayIcon(
                    iconModifier = Modifier.size(40.dp),
                    onTapped = onResumeTapped,
                )

                Spacer(modifier = Modifier.padding(horizontal = 10.dp))

                CameraStopIcon(
                    iconModifier = Modifier.size(40.dp),
                    onTapped = onStopTapped
                )
            }
        }


        Spacer(modifier = Modifier.padding(horizontal = 10.dp))

        CameraCaptureIcon(
            iconModifier = Modifier.size(40.dp),
            onTapped = onCaptureTapped
        )


        Spacer(modifier = Modifier.padding(horizontal = 10.dp))

        if (showFlipIcon && recordingStatus == RecordingStatus.Idle) {

            CameraFlipIcon(
                iconModifier = Modifier.size(40.dp),
                onTapped = onFlipTapped
            )

        }
    }
}



@OptIn(ExperimentalAnimationApi::class)
@Preview
@Composable
fun CameraComposePreview(){
    val permissionsManager = PermissionsManager()
    val navController = rememberAnimatedNavController()

    CompositionLocalProvider(LocalPermissionsManager provides permissionsManager) {
        GISMemoTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {

                CameraCompose(
                    navController = navController
                )

            }
        }
    }
}
