package com.example.gismemo.view


import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

import androidx.media3.ui.PlayerView

import com.example.gismemo.ui.theme.GISMemoTheme



import com.linc.audiowaveform.AudioWaveform
import com.linc.audiowaveform.infiniteLinearGradient
import com.linc.audiowaveform.model.AmplitudeType
import com.linc.audiowaveform.model.WaveformAlignment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import linc.com.amplituda.Amplituda
import linc.com.amplituda.callback.AmplitudaErrorListener

typealias Amplitudes = List<Int>


 fun Context.setAmplitudes( uri:Uri) :Amplitudes {
    return  Amplituda(this).processAudio(uri.encodedPath)
        .get(AmplitudaErrorListener {
            it.printStackTrace()
        })
        .amplitudesAsList()
}

fun Context.getExoPlayer(exoPlayerListener: Player.Listener): ExoPlayer {
    return ExoPlayer.Builder(this).build().apply {
        addListener(exoPlayerListener)
        prepare()
    }
}


@SuppressLint("UnsafeOptInUsageError")
@Composable
fun  ExoplayerCompose( uri:Uri? = null,   uriList: List<Uri> = emptyList(), isVisibleAmplitudes:Boolean = false){

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var waveformProgress by remember { mutableStateOf(0F) }

    var mediaItemDuration by remember { mutableStateOf(0L) }

    var mediaItemAmplitudes:Amplitudes by  remember { mutableStateOf( emptyList() ) }

    var mediaItemTitle  by  remember { mutableStateOf( "" ) }

    val mediaItems :MutableList<MediaItem> = mutableListOf()

    val coroutineScope = rememberCoroutineScope()

    lateinit var coroutineJob: Job


    val exoPlayerListener = object : Player.Listener {

        override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int ) {
            super.onPositionDiscontinuity(oldPosition, newPosition, reason)
            if(isVisibleAmplitudes) {
                waveformProgress = newPosition.positionMs.toFloat() / mediaItemDuration
            }
        }


        override fun onEvents(player: Player, events: Player.Events) {

            if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)){
                if(isVisibleAmplitudes) {
                    if (player.isPlaying) {
                       coroutineJob = lifecycleOwner.lifecycleScope.launch {
                            while (true) {
                                waveformProgress =
                                    player.currentPosition.toFloat() / player.duration
                                delay(100L)
                            }
                        }
                    } else {
                        if (coroutineJob.isActive) {
                            coroutineJob.cancel()
                        }
                    }
                }
            }


            if ( events.contains(Player.EVENT_TRACKS_CHANGED) ) {
                super.onEvents(player, events)
                if(isVisibleAmplitudes) {
                    player.playWhenReady = false
                    player.seekTo(0L)
                    mediaItemDuration = player.duration
                    waveformProgress = 0F
                    player.currentMediaItem?.localConfiguration?.uri?.let { uri ->
                        mediaItemAmplitudes = context.setAmplitudes(uri = uri)
                        mediaItemTitle =
                            "${player.currentMediaItemIndex + 1}번 트렉[${uri.lastPathSegment.toString()}]"
                    }
                }
            }


        }


    }

    val exoPlayer =   remember { context.getExoPlayer(exoPlayerListener) }

    LaunchedEffect(key1 = uri, key2 = uriList){

        uri?.let {
            if (exoPlayer.mediaItemCount > 0) {
                exoPlayer.addMediaItem(exoPlayer.mediaItemCount , MediaItem.fromUri(it))
                exoPlayer.seekTo(exoPlayer.mediaItemCount - 1, 0)
            } else {
                exoPlayer.setMediaItem(MediaItem.fromUri(it))
            }
        }

        if(uriList.isNotEmpty()){
            if (exoPlayer.mediaItemCount > 0) {
                uriList.forEach {
                    mediaItems.add(MediaItem.fromUri(it))
                }
                exoPlayer.addMediaItems(exoPlayer.mediaItemCount , mediaItems)
            } else {
                uriList.forEach {
                    mediaItems.add(MediaItem.fromUri(it))
                }
                exoPlayer.setMediaItems(mediaItems)
            }
        }
    }


/*
    val colorBrush = SolidColor(Color.Magenta)
    val staticGradientBrush = Brush.linearGradient(colors = listOf(Color(0xff22c1c3), Color(0xfffdbb2d)))
    val animatedGradientBrush = Brush.infiniteLinearGradient(
        colors = listOf(Color(0xff22c1c3), Color(0xfffdbb2d)),
        animation = tween(durationMillis = 6000, easing = LinearEasing),
        width = 128F
    )
*/


    Column {


        Text(
            modifier = Modifier.fillMaxWidth(),
            text = mediaItemTitle,
            textAlign = TextAlign.Center
        )




        if (isVisibleAmplitudes && mediaItemAmplitudes.isNotEmpty()) {

            AudioWaveform(
                modifier = Modifier.fillMaxWidth(),
                // Spike DrawStyle: Fill or Stroke
                style = Fill,
                waveformAlignment = WaveformAlignment.Center,
                amplitudeType = AmplitudeType.Avg,
                // Colors could be updated with Brush API
            //  progressBrush = SolidColor(Color.Gray),
              progressBrush = Brush.infiniteLinearGradient(
                  colors = listOf(Color(0xff22c1c3), Color(0xfffdbb2d)),
                  animation = tween(durationMillis = 6000, easing = LinearEasing),
                  width = 128F
              ),
                waveformBrush = SolidColor(Color.LightGray),

                spikeWidth = 1.dp,
                spikePadding = 1.dp,
                spikeRadius = 1.dp,
                spikeAnimationSpec = tween(0),
                progress = waveformProgress,
                amplitudes = mediaItemAmplitudes,

                onProgressChange = {

                    exoPlayer.seekTo(mediaItemDuration.times(it).toLong())
                },
                onProgressChangeFinished = {

                }
            )
        }


            DisposableEffect(
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AndroidView(
                        factory = { context ->
                            /*
                            StyledPlayerView(context).apply {
                                player = exoPlayer
                                controllerShowTimeoutMs = 0
                            }
                             */

/*
                            PlayerControlView(context).apply {
                                player = exoPlayer
                               showTimeoutMs = 0

                                val params = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                layoutParams = params
                            }

 */

                            PlayerView(context).apply {
                                player = exoPlayer
                                this.controllerShowTimeoutMs = 0

                                val params = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                layoutParams = params
                            }



                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            ) {
                onDispose {
                    exoPlayer.release()
                }
            }





    }

}


@Preview
@Composable
private fun PrevExoplayerCompose(){


    GISMemoTheme {

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
    //       ExoplayerCompose( uri = "/data/data/com.example.gismemo/files/audios/20230601-092114739_record.ogg".toUri())

            ExoplayerCompose( uri = "/data/data/com.example.gismemo/files/videos/2023-06-08-18-24-16-156.mp4".toUri())
        }

    }

}

