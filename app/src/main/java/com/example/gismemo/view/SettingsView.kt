package com.example.gismemo.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.gismemo.LocalUsableDarkMode
import com.example.gismemo.LocalUsableHaptic
import com.example.gismemo.R
import com.example.gismemo.data.RepositoryProvider
import com.example.gismemo.db.LocalLuckMemoDB
import com.example.gismemo.shared.composables.LocalPermissionsManager
import com.example.gismemo.shared.composables.PermissionsManager
import com.example.gismemo.ui.theme.GISMemoTheme
import com.example.gismemo.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(navController: NavHostController){

    val context = LocalContext.current
    val db = LocalLuckMemoDB.current
    val viewModel = remember {
        SettingsViewModel(repository = RepositoryProvider.getRepository().apply { database = db }  )
    }

    val isUsableHaptic = LocalUsableHaptic.current
    val isUsableDarkMode = LocalUsableDarkMode.current
    var checkedIsUsableHaptic by remember { mutableStateOf(isUsableHaptic) }
    var checkedIsDarkMode by remember { mutableStateOf(isUsableDarkMode) }

    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    fun hapticProcessing(){
        if(isUsableHaptic){
            coroutineScope.launch {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }



    val iconIsUsableHaptic: (@Composable () -> Unit)? = if (checkedIsUsableHaptic) {
        {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(SwitchDefaults.IconSize),
            )
        }
    } else {
        null
    }

    val iconIsDarkMode: (@Composable () -> Unit)? = if (checkedIsDarkMode) {
        {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(SwitchDefaults.IconSize),
            )
        }
    } else {
        null
    }

    var isAlertDialog by rememberSaveable {
        mutableStateOf(false)
    }


    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(26.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(context.resources.getString(R.string.setting_UsableHaptic))
            Text(context.resources.getString(R.string.setting_UsableDarkMode))
            Text(context.resources.getString(R.string.setting_ClearAllMemo))
        }

        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.Start
        ) {
            Switch(
                modifier = Modifier.semantics { contentDescription = "IS Usable Haptic " },
                checked = checkedIsUsableHaptic,
                onCheckedChange = {
                    hapticProcessing()
                    checkedIsUsableHaptic = it
                    viewModel.onEvent(SettingsViewModel.Event.UpdateIsUsableHaptic(it))
                },
                thumbContent = iconIsUsableHaptic
            )

            Switch(
                modifier = Modifier.semantics { contentDescription = "IS Usable DarkMode " },
                checked = checkedIsDarkMode,
                onCheckedChange = {
                    hapticProcessing()
                    checkedIsDarkMode = it
                    viewModel.onEvent(SettingsViewModel.Event.UpdateIsUsableDarkMode(it))
                },
                thumbContent = iconIsDarkMode
            )

            IconButton(
                modifier = Modifier.scale(1.2f),
                onClick = {
                    hapticProcessing()
                    isAlertDialog = true

                },
                content = {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Clear All Memo"
                    )
                }
            )

        }
    }




    /*
    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ){

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text("Usable Haptic ")
            Switch(
                modifier = Modifier.semantics { contentDescription = "IS Usable Haptic " },
                checked = checkedIsUsableHaptic,
                onCheckedChange = {
                    hapticProcessing()
                    checkedIsUsableHaptic = it
                    viewModel.onEvent(SettingsViewModel.Event.UpdateIsUsableHaptic(it))
                },
                thumbContent = iconIsUsableHaptic
            )

        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text("Usable DarkMode ")
            Switch(
                modifier = Modifier.semantics { contentDescription = "IS Usable DarkMode " },
                checked = checkedIsDarkMode,
                onCheckedChange = {
                    hapticProcessing()
                    checkedIsDarkMode = it
                    viewModel.onEvent(SettingsViewModel.Event.UpdateIsUsableDarkMode(it))
                },
                thumbContent = iconIsDarkMode
            )

        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text("Clear All Memo")

            IconButton(
                modifier = Modifier,
                onClick = {
                    hapticProcessing()
                    isAlertDialog = true

                },
                content = {

                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Clear All Memo"
                        )


                }
            )

        }

        if(isAlertDialog) {
            AlertDialog(
                onDismissRequest = {
                    isAlertDialog = false
                }
            ) {
                Surface(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight(),
                    shape = ShapeDefaults.Large,
                    tonalElevation = AlertDialogDefaults.TonalElevation
                ) {

                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(top = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {

                        androidx.compose.material.Text(
                            modifier = Modifier.padding(bottom = 10.dp),
                            text = "Clear All Memo",
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )


                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {

                            androidx.compose.material3.TextButton(

                                onClick = {
                                    hapticProcessing()
                                    isAlertDialog = false
                                }
                            ) {
                                androidx.compose.material.Text(
                                    "Cancel",
                                    textAlign = TextAlign.Center,
                                    style = TextStyle(
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                            }


                            androidx.compose.material3.TextButton(

                                onClick = {
                                    hapticProcessing()
                                    isAlertDialog = false
                                    viewModel.onEvent(SettingsViewModel.Event.clearAllMemo)
                                }
                            ) {
                                androidx.compose.material.Text(
                                    "Confirm",
                                    textAlign = TextAlign.Center,
                                    style = TextStyle(
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                            }


                        }
                    }

                }

            }

        }

    }

     */

}

@Preview
@Composable
fun PrevSettingsView(){
    val permissionsManager = PermissionsManager()
    val navController = rememberNavController()

    CompositionLocalProvider(LocalPermissionsManager provides permissionsManager) {

        GISMemoTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.onPrimary,
                contentColor = MaterialTheme.colors.primary
            ) {
                SettingsView(navController = navController)
            }
        }

    }


}


