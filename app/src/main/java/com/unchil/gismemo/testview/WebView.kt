package com.unchil.gismemo.view

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.unchil.gismemo.ui.theme.GISMemoTheme
import com.google.accompanist.web.*
import com.unchil.gismemo.R
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ImageWebViewer( url:String ) {
    val webViewNavigator = rememberWebViewNavigator()
    val webViewState = rememberWebViewState( url = url,  additionalHttpHeaders = emptyMap())
    val webViewClient = AccompanistWebViewClient()
    val webChromeClient = AccompanistWebChromeClient()

    WebView(
        modifier= Modifier.fillMaxSize(),
        state = webViewState,
        client = webViewClient,
        chromeClient = webChromeClient,
        navigator = webViewNavigator,
        onCreated = { webView ->
            with(webView) {
                settings.run {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    javaScriptCanOpenWindowsAutomatically = false
                }
            }
        }
    )

}

@Preview
@Composable
fun PrevImageWebViewer() {

    val context = LocalContext.current
    GISMemoTheme {
        androidx.compose.material3.Surface(
            modifier = Modifier.background(color = Color.White)
        ) {


         //   ImageWebViewer(url = "https://google.co.kr")
            //  ImageWebViewer(url = "http://naver.com")

            context.resources.getString(R.string.mainmenu_write)
            Text(text = context.resources.getString(R.string.mainmenu_write))
        }

    }
}