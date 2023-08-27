package com.example.gismemo.model

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Screenshot
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Share
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.gismemo.R


/*
sealed class GisMemoData {
    data class Photo(val imageUrl: String) : GisMemoData()
    data class SnapShot(val imageUrl: String) : GisMemoData()
    data class Record(val imageUrl: String) : GisMemoData()
    data class MemoData(val dataType:GisMemoDataType,  val imageUrl: String) : GisMemoData()
}

enum class GisMemoDataType {
    IMAGE,AUDIO,VIDEO,TEXT
}

 */


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

fun BiometricCheckType.getTitle():Pair<String,String> {
    return when(this){
        BiometricCheckType.DETAILVIEW  -> {
            Pair("메모 보기", "메모를 확인 하려면 보안 인증이 필요 합니다.")
        }
        BiometricCheckType.SHARE -> {
            Pair("메모 공유", "메모를 공유 하려면 보안 인증이 필요 합니다.")
        }
        BiometricCheckType.DELETE -> {
            Pair("메모 삭제", "메모를 삭제 하려면 보안 인증이 필요 합니다.")
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