package com.example.gismemo.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController

@Composable
fun SettingsView(navController: NavHostController){
    Box(modifier = Modifier.fillMaxSize().background(color = Color.DarkGray))
}