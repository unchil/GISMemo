package com.example.gismemo.shared.composables

import android.graphics.drawable.shapes.Shape
import android.text.format.DateUtils
import androidx.camera.core.ImageCapture
import androidx.camera.core.TorchState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun CameraFlashIcon(
    buttonModifier: Modifier = Modifier,
    iconModifier:Modifier = Modifier,
    @ImageCapture.FlashMode flashMode: Int,
    onTapped: () -> Unit) {

    IconButton(
        modifier = Modifier.then(buttonModifier),
        onClick = { onTapped() },
        content = {
            // ImageVector
            val imageVector = when(flashMode) {
                ImageCapture.FLASH_MODE_AUTO -> Icons.Outlined.FlashAuto
                ImageCapture.FLASH_MODE_OFF -> Icons.Outlined.FlashOff
                ImageCapture.FLASH_MODE_ON -> Icons.Outlined.FlashOn
                else -> Icons.Outlined.FlashOff
            }
            Icon(
                modifier = Modifier.then(iconModifier),
                imageVector = imageVector,
                contentDescription = "flash_mode"
            )
        }
    )
}

@Composable
fun CameraRecordIcon(
    buttonModifier: Modifier = Modifier,
    iconModifier:Modifier = Modifier,
    onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier.then(buttonModifier),
        onClick = { onTapped() },
        content = {

            Icon(
                modifier = Modifier.then(iconModifier),
                imageVector = Icons.Outlined.Videocam,
                contentDescription = "videocam"
            )

        })
}

@Composable
fun CameraPauseIcon(
    buttonModifier: Modifier = Modifier,
    iconModifier:Modifier = Modifier,
    onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier.then(buttonModifier),
        onClick = { onTapped() },
        content = {
            Icon(
                modifier = Modifier.then(iconModifier),
                imageVector = Icons.Outlined.PauseCircle,
                contentDescription = "videocam"
            )
        }
    )
}


@Composable
fun CameraPlayIcon(
    buttonModifier: Modifier = Modifier,
    iconModifier:Modifier = Modifier,
    onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier.then(buttonModifier),
        onClick = { onTapped() },
        content = {
            Icon(
                modifier = Modifier.then(iconModifier),
                imageVector = Icons.Outlined.PlayCircle,
                contentDescription = "videocam"
            )
        }
    )
}



@Composable
fun CameraStopIcon(
    buttonModifier: Modifier = Modifier,
    iconModifier:Modifier = Modifier,
    onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier.then(buttonModifier),
        onClick = { onTapped() },
        content = {

            Icon(
                modifier = Modifier.then(iconModifier),
                imageVector = Icons.Outlined.StopCircle,
                contentDescription = "videocam"
            )

        }
    )
}


@Composable
fun CameraTorchIcon(
    modifier: Modifier = Modifier,
    @TorchState.State torchState: Int,
    onTapped: () -> Unit) {
    IconButton(
        modifier = Modifier.then(modifier),
        onClick = { onTapped() },
        content = {

            val imageVector = when(torchState) {
                TorchState.ON -> { Icons.Outlined.FlashOn }
                else -> { Icons.Outlined.FlashOff }
            }
            Icon(
                imageVector = imageVector,
                contentDescription = "flash_mode"
            )
        }
    )
}


@Composable
fun CameraCaptureIcon(
    buttonModifier: Modifier = Modifier,
    iconModifier:Modifier = Modifier,
    onTapped: () -> Unit) {

    IconButton(
        modifier = Modifier.then(buttonModifier),
        onClick = { onTapped() },
        content = {

            Icon(
                modifier = Modifier.then(iconModifier),
                imageVector = Icons.Outlined.Camera
                , contentDescription = "capture"
            )

        }
    )
}


@Composable
fun CameraFlipIcon(
    buttonModifier: Modifier = Modifier,
    iconModifier:Modifier = Modifier,
    onTapped: () -> Unit) {

    IconButton(
        modifier = Modifier.then(buttonModifier),
        onClick = { onTapped() },
        content = {

            Icon(
                modifier = Modifier.then(iconModifier),
                imageVector = Icons.Outlined.FlipCameraIos,
                contentDescription = "camera_flip"
            )

        }
    )
}

@Composable
fun Timer(modifier: Modifier = Modifier, seconds: Int) {
    if (seconds > 0) {
        Box(modifier = Modifier.padding(vertical = 30.dp).then(modifier).clip(ShapeDefaults.Small)) {
            Text(
                text = DateUtils.formatElapsedTime(seconds.toLong()),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                modifier = Modifier
                    .background(color = Color.Red.copy(alpha = 0.3f))
                    .padding( horizontal = 6.dp)
                    .then(modifier)
            )
        }

    }
}



