package com.example.gismemo.view

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.example.gismemo.R
import com.example.gismemo.shared.composables.LocalPermissionsManager
import com.example.gismemo.shared.composables.PermissionsManager
import com.example.gismemo.ui.theme.GISMemoTheme


@SuppressLint("SuspiciousIndentation")
@Composable
fun ImageViewer(data:Any, size: Size, isZoomable:Boolean = false){

    val scale = remember { mutableStateOf(1f) }
    val rotationState = remember { mutableStateOf(0f) }

    val boxModifier:Modifier = when(isZoomable) {
            true -> {
                Modifier.fillMaxSize().pointerInput(Unit){
                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        scale.value *= zoom
                        rotationState.value += rotation }}
            }
            false -> Modifier.fillMaxSize()
        }

    val imageModifier:Modifier  = when(isZoomable) {
        true -> {
            Modifier
                .fillMaxSize()
                .graphicsLayer(
                    // adding some zoom limits (min 50%, max 200%)
                    scaleX = maxOf(.5f, minOf(3f, scale.value)),
                    scaleY = maxOf(.5f, minOf(3f, scale.value)),
                    rotationZ = rotationState.value
                )
        }
        false -> Modifier.fillMaxSize()
    }

        Box(
            contentAlignment = Alignment.Center,
            modifier = boxModifier

        ){
            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(data)
                    .size(size)
                    .crossfade(true)
                    .build()
            )

            when(painter.state){

                is AsyncImagePainter.State.Loading -> {

                    CircularProgressIndicator(
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )


                }
                is AsyncImagePainter.State.Success  -> {

                    Image(

                        painter = painter,
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = imageModifier

                    )
                }
                is AsyncImagePainter.State.Empty -> {

                }
                is AsyncImagePainter.State.Error -> {

                }
            }
        }

}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoPreview(
    modifier: Modifier = Modifier,
    data:Any,
    onPhotoPreviewTapped: (Any) -> Unit
) {


    Box(
        modifier = Modifier
            .then(modifier)
            .height(100.dp)
            .width(100.dp)
            .border(width = 1.dp, color = Color.Black, shape = ShapeDefaults.Small)
            .clip(shape = ShapeDefaults.Small)
            .combinedClickable { onPhotoPreviewTapped(data) }
    ,
        contentAlignment = Alignment.Center

    ) {

        ImageViewer(data = data, size = Size.ORIGINAL, isZoomable = false)
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PrevViewMemoData(
    modifier: Modifier = Modifier,
){

    val permissionsManager = PermissionsManager()


    CompositionLocalProvider(LocalPermissionsManager provides permissionsManager) {

            val uriTest= "content://com.google.android.tts.AudioRecordingProvider/my_recordings/recording.amr"
            val url1 =  "file://data/data/com.example.gismemo/files/photos/2023-05-17-13-02-46-802.jpeg"
            val url2 = "https://images.unsplash.com/photo-1544735716-392fe2489ffa?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop"
            val  url4 = Uri.parse("android.resource://com.example.gismemo/" + R.drawable.outline_perm_media_black_48).toString().toUri()
            val url3 = url1.toUri()


            GISMemoTheme {
                Surface(
                    modifier = Modifier.background(color = Color.White)
                ) {

                ImageViewer(data = url4 , size = Size.ORIGINAL, isZoomable = false)
                 //   AsyncImage(model = url2, modifier = Modifier.fillMaxSize(), contentDescription = "")

                }
            }


    }

}