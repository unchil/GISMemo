package com.example.gismemo.map

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.gismemo.ui.theme.GISMemoTheme
import com.example.gismemo.view.GoogleMapView

internal const val KEY_ARG_MAP_SCREEN_TYPE = "KEY_ARG_MAP_SCREEN_TYPE"


fun launchGoogleMapActivity(context: Context, mapScreenType:String) {
    val intent = Intent(context, GoogleMapActivity::class.java)
    intent.putExtra(KEY_ARG_MAP_SCREEN_TYPE, mapScreenType)
    context.startActivity(intent)
}



//@AndroidEntryPoint
class GoogleMapActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            GISMemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GoogleMapView()
                }
            }

        }
    }
}


