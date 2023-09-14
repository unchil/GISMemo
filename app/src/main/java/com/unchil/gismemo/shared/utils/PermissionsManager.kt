@file:OptIn(ExperimentalPermissionsApi::class)


package com.unchil.gismemo.shared.composables

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


val LocalPermissionsManager = compositionLocalOf<PermissionsManager> { error("No permissions handler found!") }

class PermissionsManager {

    val _action = MutableStateFlow(Action.NO_ACTION)
    val action: StateFlow<Action> = _action
    var _shouldShowRationale = false
    var isRequestPermission = false

    fun getPermissionActionNew(multiplePermissionsState: MultiplePermissionsState, coroutineScope: CoroutineScope){

        coroutineScope.launch {
            val permissionAction =

                if (multiplePermissionsState.allPermissionsGranted) {
                    Action.NO_ACTION
                } else {

                    if (multiplePermissionsState.shouldShowRationale){
                        if(isRequestPermission){
                            multiplePermissionsState.launchMultiplePermissionRequest()
                            isRequestPermission = false
                            _shouldShowRationale = true
                            Action.NO_ACTION
                        }else{
                            isRequestPermission = true
                            Action.SHOW_RATIONALE
                        }

                    } else {

                        if (_shouldShowRationale){
                            _shouldShowRationale = false
                            Action.SHOW_SETTING
                        }else {
                            multiplePermissionsState.launchMultiplePermissionRequest()
                            Action.NO_ACTION
                        }
                    }
                }

            _action.emit(permissionAction)

        }
    }

    enum class Action {
        SHOW_RATIONALE, NO_ACTION, SHOW_SETTING
    }


}

