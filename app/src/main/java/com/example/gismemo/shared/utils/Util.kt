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
