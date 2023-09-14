package com.unchil.gismemo.shared

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.biometric.BiometricManager

import androidx.core.content.FileProvider
import com.unchil.gismemo.data.RepositoryProvider
import com.unchil.gismemo.db.LuckMemoDB
import com.unchil.gismemo.db.entity.MEMO_TBL

import java.io.File


fun launchIntent_Biometric_Enroll(context: Context){
    val intent =   Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
        putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
    }
    context.startActivity(intent)
}

fun launchIntent_ShareMemo(context: Context, db:LuckMemoDB, memo: MEMO_TBL){

     val FILEPROVIDER_AUTHORITY = "com.example.gismemo.fileprovider"

    val repository = RepositoryProvider.getRepository().apply { database = db }

   // val repository = RepositoryProvider.getRepository(context.applicationContext)
    repository.getShareMemoData(id = memo.id) { attachment, comments ->

        val attachmentUri = arrayListOf<Uri>()

        attachment.forEach {
            attachmentUri.add(
                FileProvider.getUriForFile(  context,
                    FILEPROVIDER_AUTHORITY,  File( it.encodedPath?: "")  )
            )
        }

        val subject =  memo.title
        var text = "${memo.desc} \n${memo.snippets} \n\n"

        comments.forEachIndexed { index, comment ->
            text = text + "[${index}]: ${comment}" + "\n"
        }

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"

            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, subject)

            putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachmentUri)
        }
        context.startActivity(intent)

    }

}
/*
fun launchIntent_ShareKakao(context: Context, memo: MEMO_TBL) {


    if( ShareClient.instance.isKakaoTalkSharingAvailable(context)){

        var text =  "제목: ${memo.title}\n\n ${memo.desc} \n  태그: ${memo.snippets} \n\n"

        val repository = RepositoryProvider.getRepository(context.applicationContext)

        repository.getShareMemoData(id = memo.id) { attachment, comments ->

            comments.forEachIndexed { index, comment ->
                text = text + "comment(${index+1}): ${comment}" + "\n"
            }

            val defaultFeed = TextTemplate(
                text = text,
                link = Link()
            )

            ShareClient.instance.shareDefault(context, defaultFeed) { sharingResult, error ->
                if (error == null && sharingResult != null) {
                    context.startActivity(sharingResult.intent)
                }
            }

        }

    }
}

 */

/*
@RequiresApi(Build.VERSION_CODES.R)
fun launchIntent_BiometricEnRoll(context: Context){

    val intent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
        putExtra(  Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL  )
    }

    /*
    val reqPermissionResultCode:Int = -1

    ActivityCompat.startActivityForResult(
        context as FragmentActivity,
        intent,
        reqPermissionResultCode,
        null
    )

     */

    context.startActivity(intent)
}

 */