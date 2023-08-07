package com.example.gismemo.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.icu.text.SimpleDateFormat
import android.location.Location
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices


// var currentLocation = LatLng(37.422672271728516, -122.0849838256836)


@SuppressLint("MissingPermission")
fun Context.getDeviceLocation(completeHandler: (Location?) -> Unit){

    val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(this)

    var lastKnownLocation: Location? = null

    fusedLocationProviderClient.lastLocation.addOnCompleteListener(this.mainExecutor) { task ->
        if (task.isSuccessful && task.result != null && task.result != lastKnownLocation) {
            lastKnownLocation = task.result
            completeHandler(task.result)
        }else {
            completeHandler(lastKnownLocation)
        }
    }
}


@SuppressLint("MissingPermission")
@Composable
fun GetDeviceLocation( completeHandler: (Location?) -> Unit){

    val context = LocalContext.current

    val fusedLocationProviderClient = rememberSaveable {
        LocationServices.getFusedLocationProviderClient(context)
    }
    var lastKnownLocation by rememberSaveable {
        mutableStateOf<Location?>(null)
    }

    fusedLocationProviderClient.lastLocation.addOnCompleteListener(context.findActivity()) { task ->

        if (task.isSuccessful && task.result != null && task.result != lastKnownLocation) {
            lastKnownLocation = task.result
            completeHandler(task.result)

        }else {
            completeHandler(lastKnownLocation)
        }


    }
}

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}


const val MILLISEC_CHECK = 9999999999
const val MILLISEC_DIGIT = 1L
const val MILLISEC_CONV_DIGIT = 1000L
const val yyyyMMddHHmmE = "yyyy/MM/dd HH:mm E"
const val yyyyMMddHHmmssE = "yyyy/MM/dd HH:mm:ss E"
const val EEEHHmmss = "EEE HH:mm:ss"


@SuppressLint("SimpleDateFormat")
fun UnixTimeToString(time: Long, format: String): String{
    val UNIXTIMETAG_SECTOMILI
            = if( time > MILLISEC_CHECK) MILLISEC_DIGIT else MILLISEC_CONV_DIGIT

    return SimpleDateFormat(format)
        .format(time * UNIXTIMETAG_SECTOMILI )
        .toString()
}
