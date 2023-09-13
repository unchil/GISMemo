package com.example.gismemo.shared.utils

import androidx.compose.material3.SnackbarDuration
import com.example.gismemo.R

enum class SnackBarChannelType {
    ALL_DATA_DELETE,
    BIOMETRIC_NO_SUCCESS,
    LOCATION_SERVICE_DISABLE,
    AUTHENTICATION_FAILED,
    ITEM_DELETE,
    MARKER_CHANGE_SET,
    MARKER_CHANGE_FREE,
    LOCK_CHANGE_SET,
    LOCK_CHANGE_FREE,
    SNAPSHOT_RESULT,
    SEARCH_CLEAR,
    SEARCH_RESULT,
    MEMO_SAVE,
    MEMO_CLEAR_REQUEST,
    MEMO_CLEAR_RESULT,
    MEMO_DELETE
}

data class SnackBarChannelData(
    val channelType: SnackBarChannelType,
    val channel:Int,
    var message:Int,
    val duration: SnackbarDuration,
    val actionLabel:String?,
    val withDismissAction:Boolean,
)

val snackbarChannelList = listOf(

    SnackBarChannelData(
        channelType = SnackBarChannelType.ALL_DATA_DELETE,
        channel = 13,
        message = R.string.snackbar_13,
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),


    SnackBarChannelData(
        channelType = SnackBarChannelType.BIOMETRIC_NO_SUCCESS,
        channel = 12,
        message = R.string.snackbar_12,
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),


    SnackBarChannelData(
        channelType = SnackBarChannelType.LOCATION_SERVICE_DISABLE,
        channel = 11,
        message = R.string.snackbar_11,
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),


    SnackBarChannelData(
        channelType = SnackBarChannelType.AUTHENTICATION_FAILED,
        channel = 10,
        message = R.string.snackbar_10,
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),


    SnackBarChannelData(
        channelType = SnackBarChannelType.ITEM_DELETE,
        channel = 9,
        message = R.string.snackbar_9,
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),


    SnackBarChannelData(
        channelType = SnackBarChannelType.MEMO_DELETE,
        channel = 8,
        message = R.string.snackbar_8,
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),


    SnackBarChannelData(
        channelType = SnackBarChannelType.MARKER_CHANGE_SET,
        channel = 700,
        message = R.string.snackbar_700,
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),



    SnackBarChannelData(
        channelType = SnackBarChannelType.MARKER_CHANGE_FREE,
        channel = 7,
        message = R.string.snackbar_7,
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),


    SnackBarChannelData(
        channelType = SnackBarChannelType.LOCK_CHANGE_SET,
        channel = 600,
        message = R.string.snackbar_600,
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),


    SnackBarChannelData(
        channelType = SnackBarChannelType.LOCK_CHANGE_FREE,
        channel = 6,
        message = R.string.snackbar_6,
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),



    SnackBarChannelData(
        channelType = SnackBarChannelType.SNAPSHOT_RESULT,
        channel = 5,
        message = R.string.snackbar_5,
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),


    SnackBarChannelData(
        channelType = SnackBarChannelType.MEMO_SAVE,
        channel = 4,
        message = R.string.snackbar_4,
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),


    SnackBarChannelData(
        channelType = SnackBarChannelType.SEARCH_RESULT,
        channel = 3,
        message = R.string.snackbar_3,
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),


    SnackBarChannelData(
        channelType = SnackBarChannelType.SEARCH_CLEAR,
        channel = 2,
        message = R.string.snackbar_2,
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),

    SnackBarChannelData(
        channelType = SnackBarChannelType.MEMO_CLEAR_RESULT,
        channel = 1,
        message = R.string.snackbar_1,
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),

    SnackBarChannelData(
        channelType = SnackBarChannelType.MEMO_CLEAR_REQUEST,
        channel = 0,
        message = R.string.snackbar_0,
        duration = SnackbarDuration.Indefinite,
        actionLabel = "Ok",
        withDismissAction = true,
    ),

)