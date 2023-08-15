package com.example.gismemo.shared.utils

import androidx.compose.material3.SnackbarDuration

enum class SnackBarChannelType {
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
    var message:String,
    val duration: SnackbarDuration,
    val actionLabel:String?,
    val withDismissAction:Boolean,
)

val snackbarChannelList = listOf(

    SnackBarChannelData(
        channelType = SnackBarChannelType.AUTHENTICATION_FAILED,
        channel = 10,
        message = "인증에 실패 하였습니다. ",
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),


    SnackBarChannelData(
        channelType = SnackBarChannelType.ITEM_DELETE,
        channel = 9,
        message = "삭제 하였습니다. ",
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),


    SnackBarChannelData(
        channelType = SnackBarChannelType.MEMO_DELETE,
        channel = 8,
        message = "메모를 삭제 하였습니다. ",
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),


    SnackBarChannelData(
        channelType = SnackBarChannelType.MARKER_CHANGE_SET,
        channel = 70,
        message = "메모를 마커로 설정 하였습니다. ",
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),



    SnackBarChannelData(
        channelType = SnackBarChannelType.MARKER_CHANGE_FREE,
        channel = 7,
        message = "메모의 마커 설정을 해지 하였습니다.",
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),


    SnackBarChannelData(
        channelType = SnackBarChannelType.LOCK_CHANGE_SET,
        channel = 60,
        message = "메모를 비밀로 설정 하였습니다.",
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),


    SnackBarChannelData(
        channelType = SnackBarChannelType.LOCK_CHANGE_FREE,
        channel = 6,
        message = "메모의 보안 설정을 해지 하였습니다.",
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),



    SnackBarChannelData(
        channelType = SnackBarChannelType.SNAPSHOT_RESULT,
        channel = 5,
        message = "캡쳐 하였습니다.",
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),


    SnackBarChannelData(
        channelType = SnackBarChannelType.MEMO_SAVE,
        channel = 4,
        message = "작성중된 메모를 저장 하였습니다.",
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),


    SnackBarChannelData(
        channelType = SnackBarChannelType.SEARCH_RESULT,
        channel = 3,
        message = "검색된 데이터 ",
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),


    SnackBarChannelData(
        channelType = SnackBarChannelType.SEARCH_CLEAR,
        channel = 2,
        message = "검색 조건을 클리어 하였습니다.",
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),

    SnackBarChannelData(
        channelType = SnackBarChannelType.MEMO_CLEAR_RESULT,
        channel = 1,
        message = "작성중인 메모를 클리어 하였습니다.",
        duration = SnackbarDuration.Short,
        actionLabel = null,
        withDismissAction = true,
    ),

    SnackBarChannelData(
        channelType = SnackBarChannelType.MEMO_CLEAR_REQUEST,
        channel = 0,
        message = "작성중인 메모를 클리어 하시겠습니까?",
        duration = SnackbarDuration.Indefinite,
        actionLabel = "Ok",
        withDismissAction = true,
    ),

)