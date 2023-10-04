package com.unchil.gismemo.model

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Screenshot
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Share
import androidx.compose.ui.graphics.vector.ImageVector
import com.unchil.gismemo.R


sealed class MemoData {
    data class Photo(val dataList: MutableList<Uri>) : MemoData()
    data class SnapShot(val dataList: MutableList<Uri>) : MemoData()
    data class AudioText(var dataList: MutableList<Pair<String,List<Uri>>>) : MemoData()
    data class Video(val dataList: MutableList<Uri>) : MemoData()

}


enum class ListItemBackgroundAction {
    SHARE,DELETE
}

fun ListItemBackgroundAction.getDesc(): Pair<String, ImageVector>{
    return when(this){
        ListItemBackgroundAction.SHARE -> {
            Pair(this.name,  Icons.Rounded.Share)
        }
        ListItemBackgroundAction.DELETE -> {
            Pair(this.name,   Icons.Rounded.Delete)
        }
    }
}

enum class BiometricCheckType {
    DETAILVIEW, SHARE, DELETE
}

fun BiometricCheckType.getTitle(getString: (Int)->String):Pair<String,String> {
    return when(this){
        BiometricCheckType.DETAILVIEW  -> {
            Pair(getString(R.string.biometric_prompt_detailview_title), getString(R.string.biometric_prompt_detailview_msg))
        }
        BiometricCheckType.SHARE -> {
            Pair(getString(R.string.biometric_prompt_share_title), getString(R.string.biometric_prompt_share_msg))
        }
        BiometricCheckType.DELETE -> {
            Pair(getString(R.string.biometric_prompt_delete_title), getString(R.string.biometric_prompt_delete_msg))
        }
    }

}

enum class MemoDataContainerUser {
    DetailMemoView, WriteMemoView
}
enum class WriteMemoDataType {
    PHOTO,AUDIOTEXT,VIDEO,SNAPSHOT
}

fun WriteMemoDataType.getDesc(): Pair<Int, ImageVector>{
      return  when(this){
           WriteMemoDataType.PHOTO -> {
               Pair(R.string.dataContainer_Photo,  Icons.Outlined.Photo)
           }
           WriteMemoDataType.AUDIOTEXT -> {
               Pair(R.string.dataContainer_AudioText,  Icons.Outlined.Mic)
           }
           WriteMemoDataType.VIDEO -> {
               Pair(R.string.dataContainer_Video,  Icons.Outlined.Videocam)
           }
           WriteMemoDataType.SNAPSHOT -> {
               Pair(R.string.dataContainer_Screenshot,  Icons.Outlined.Screenshot)
           }
       }
}

val WriteMemoDataTypeList = listOf(
    WriteMemoDataType.SNAPSHOT,
    WriteMemoDataType.AUDIOTEXT,
    WriteMemoDataType.PHOTO,
    WriteMemoDataType.VIDEO
)