package com.example.gismemo.view

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.gismemo.*
import com.example.gismemo.R
import com.example.gismemo.data.RepositoryProvider
import com.example.gismemo.db.LocalLuckMemoDB
import com.example.gismemo.shared.composables.LocalPermissionsManager
import com.example.gismemo.shared.composables.PermissionsManager
import com.example.gismemo.ui.theme.GISMemoTheme
import com.example.gismemo.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import java.util.*

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}


val localeList =  listOf(
    Locale("ko"),
    Locale("en"),
    Locale("fr"),
    Locale("pt")
)

@Composable
fun SettingsView(navController: NavHostController){
    val LocalChangeLocaleCurrent = compositionLocalOf{ false }
    var context = LocalContext.current
    val db = LocalLuckMemoDB.current
    val viewModel = remember {
        SettingsViewModel(repository = RepositoryProvider.getRepository().apply { database = db }  )
    }

    val isUsableHaptic = LocalUsableHaptic.current
    val isUsableDarkMode = LocalUsableDarkMode.current
    var isLocaleChange by rememberSaveable { mutableStateOf(false) }
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
    } else {  null  }

    val iconIsDarkMode: (@Composable () -> Unit)? = if (checkedIsDarkMode) {
        {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(SwitchDefaults.IconSize),
            )
        }
    } else {    null }

    var isAlertDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val localeOption =  listOf(
        context.resources.getString(R.string.setting_Locale_ko),
        context.resources.getString(R.string.setting_Locale_en),
        context.resources.getString(R.string.setting_Locale_fr),
        context.resources.getString(R.string.setting_Locale_pt)
    )



    val localeRadioGroupState = rememberSaveable {
        mutableStateOf(0 )
    }

    LaunchedEffect(key1 = localeRadioGroupState.value ){
        isLocaleChange = !isLocaleChange

        val locale = localeList[localeRadioGroupState.value]
        Locale.setDefault(locale)

        context.resources.configuration.setLocale(locale)
        context.resources.configuration.setLayoutDirection(locale)
    //    context.createConfigurationContext(context.resources.configuration)
       context.resources.updateConfiguration( context.resources.configuration, context.resources.displayMetrics)
        viewModel.onEvent(SettingsViewModel.Event.UpdateOnChangeLocale(isLocaleChange))
        viewModel.onEvent(SettingsViewModel.Event.UpdateIsChangeLocale(localeRadioGroupState.value))

/*
        if(isLocaleChange) {
            context.findActivity().recreate()
        }
 */

    }

    CompositionLocalProvider(LocalChangeLocaleCurrent provides isLocaleChange) {

        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(500.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                Divider(modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        text = context.resources.getString(R.string.setting_UsableHaptic),
                    )

                    Switch(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .semantics { contentDescription = "IS Usable Haptic " },
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
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        text = context.resources.getString(R.string.setting_UsableDarkMode)
                    )

                    Switch(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .semantics {
                                contentDescription = "IS Usable DarkMode "
                            },
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
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        text = context.resources.getString(R.string.setting_ClearAllMemo)
                    )

                    IconButton(
                        modifier = Modifier
                            .scale(1.2f)
                            .fillMaxWidth(0.5f),
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



                Divider(modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        modifier = Modifier.padding(end = 10.dp),
                        imageVector = Icons.Outlined.Language,
                        contentDescription = "locale"
                    )
                    Text(context.resources.getString(R.string.setting_Locale))
                }


                RadioButtonGroupView(
                    state = localeRadioGroupState,
                    items = localeOption
                )


                Divider(modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp))


            }

        }

    }

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


