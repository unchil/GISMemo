package com.example.gismemo.view

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
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
import com.example.gismemo.shared.utils.SnackBarChannelType
import com.example.gismemo.shared.utils.snackbarChannelList
import com.example.gismemo.ui.theme.GISMemoTheme
import com.example.gismemo.viewmodel.ListViewModel
import com.example.gismemo.viewmodel.SettingsViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
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


fun Context.getLanguageArray():Array<String>{
    return resources.getStringArray(R.array.Language_Array)
}


@Composable
fun SettingsView(navController: NavHostController){



    val localeChange = LocalChangeLocale.current
    var context = LocalContext.current

    val languageArray = context.getLanguageArray()

    val db = LocalLuckMemoDB.current
    val viewModel = remember {
        SettingsViewModel(repository = RepositoryProvider.getRepository().apply { database = db }  )
    }

    val isUsableHaptic = LocalUsableHaptic.current
    val isUsableDarkMode = LocalUsableDarkMode.current
    val isUsableDynamicColor= LocalUsableDynamicColor.current

    var isLocaleChange by rememberSaveable { mutableStateOf(false) }
    var checkedIsUsableHaptic by remember { mutableStateOf(isUsableHaptic) }
    var checkedIsDarkMode by remember { mutableStateOf(isUsableDarkMode) }
    var checkedIsDynamicColor by remember { mutableStateOf(isUsableDynamicColor) }
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val isAlertDialog = rememberSaveable { mutableStateOf(false) }

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

    val iconIsDynamicColor: (@Composable () -> Unit)? = if (checkedIsDynamicColor) {
        {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(SwitchDefaults.IconSize),
            )
        }
    } else {    null }


    val localeOption =  listOf(
        context.resources.getString(R.string.setting_Locale_ko),
        context.resources.getString(R.string.setting_Locale_en),
        context.resources.getString(R.string.setting_Locale_fr),
        context.resources.getString(R.string.setting_Locale_zh),
        context.resources.getString(R.string.setting_Locale_pt),
        context.resources.getString(R.string.setting_Locale_es)
    )



    val localeRadioGroupState = remember {
        mutableStateOf( viewModel.repository.isChangeLocale.value )
    }

    LaunchedEffect(key1 = localeRadioGroupState.value ){
        isLocaleChange = !isLocaleChange

        val locale = Locale( languageArray[localeRadioGroupState.value] )

        Locale.setDefault(locale)

        context.resources.configuration.setLocale(locale)
        context.resources.configuration.setLayoutDirection(locale)
       context.resources.updateConfiguration( context.resources.configuration, context.resources.displayMetrics)
        viewModel.onEvent(SettingsViewModel.Event.UpdateOnChangeLocale(isLocaleChange))
        viewModel.onEvent(SettingsViewModel.Event.UpdateIsChangeLocale(localeRadioGroupState.value))
    }


    val snackBarHostState = remember { SnackbarHostState() }
    val channel = remember { Channel<Int>(Channel.CONFLATED) }

    LaunchedEffect(channel) {
        channel.receiveAsFlow().collect { index ->
            val channelData = snackbarChannelList.first {
                it.channel == index
            }

            val result = snackBarHostState.showSnackbar(
                message =   context.resources.getString( channelData.message),
                actionLabel = channelData.actionLabel,
                withDismissAction = channelData.withDismissAction,
                duration = channelData.duration
            )
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    hapticProcessing()
                    //----------
                    when (channelData.channelType) {

                        else -> {}
                    }
                    //----------
                }
                SnackbarResult.Dismissed -> {
                    hapticProcessing()
                }
            }
        }
    }



    Scaffold(
        snackbarHost ={
            SnackbarHost(hostState = snackBarHostState)
        },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
    ) {
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
                        style = androidx.compose.material3.MaterialTheme.typography.titleSmall
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
                        text = context.resources.getString(R.string.setting_UsableDarkMode),
                        style = androidx.compose.material3.MaterialTheme.typography.titleSmall
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
                        text = context.resources.getString(R.string.setting_UsableDynamicColor),
                        style = androidx.compose.material3.MaterialTheme.typography.titleSmall
                    )

                    Switch(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .semantics {
                                contentDescription = "IS Usable DynamicColor "
                            },
                        checked = checkedIsDynamicColor,
                        onCheckedChange = {
                            hapticProcessing()
                            checkedIsDynamicColor = it
                            viewModel.onEvent(SettingsViewModel.Event.UpdateIsDynamicColor(it))
                        },
                        thumbContent = iconIsDynamicColor
                    )
                }



                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        text = context.resources.getString(R.string.setting_ClearAllMemo),
                        style = androidx.compose.material3.MaterialTheme.typography.titleSmall
                    )

                    IconButton(
                        modifier = Modifier
                            .scale(1.2f)
                            .fillMaxWidth(0.5f),
                        onClick = {
                            hapticProcessing()
                            isAlertDialog.value = true

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
                        modifier = Modifier,
                        imageVector = Icons.Outlined.Language,
                        contentDescription = "locale"
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                    Text(text = context.resources.getString(R.string.setting_Locale),
                        style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
                }


                RadioButtonGroupView(
                    state = localeRadioGroupState,
                    data = localeOption,
                    layoutScopeType = "Column"
                )


                Divider(modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp))


            }


            if( isAlertDialog.value) {
                DeleteConfirmDialog(isAlertDialog){
                    viewModel.onEvent(SettingsViewModel.Event.clearAllMemo)

                    channel.trySend(snackbarChannelList.first {
                        it.channelType == SnackBarChannelType.ALL_DATA_DELETE
                    }.channel)
                }

            }

        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteConfirmDialog(
    isAlertDialog: MutableState<Boolean>,
    event: (()-> Unit)? = null
){

    val context = LocalContext.current
    val isUsableHaptic = LocalUsableHaptic.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    fun hapticProcessing() {
        if (isUsableHaptic) {
            coroutineScope.launch {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            isAlertDialog.value = false
        }
    ) {


        Column(
            modifier = Modifier
                .background(
                    color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceColorAtElevation(
                        6.dp
                    ),
                    shape = ShapeDefaults.ExtraSmall
                )
                .wrapContentWidth()
                .wrapContentHeight()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {


            androidx.compose.material.Text(
                modifier = Modifier,
                text = context.resources.getString(R.string.setting_DeleteAlertDialog_Title),
                textAlign = TextAlign.Center,
                style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                color = Color.Red
            )


            TextButton(

                onClick = {
                    hapticProcessing()
                    isAlertDialog.value = false
                    //event(SettingsViewModel.Event.clearAllMemo)
                    event?.invoke()
                }
            ) {
                androidx.compose.material.Text(
                    context.resources.getString(R.string.setting_DeleteAlertDialog_Confirm),
                    textAlign = TextAlign.Center,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    color = Color.Red
                )
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


