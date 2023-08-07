package com.example.gismemo.view

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewModelScope
import com.example.gismemo.data.RepositoryProvider
import com.example.gismemo.navigation.mainScreens
import com.example.gismemo.navigation.navigateTo
import com.example.gismemo.ui.theme.GISMemoTheme
import com.example.gismemo.viewmodel.WriteMemoViewModel
import com.google.maps.android.compose.Circle

/*
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MemoTagSelectView(
    isDialog: MutableState<Boolean>   ,
 //   tagList: List<GisMemoDataLocalSource.Companion.IconStringPair>,
    tagList:List<TagInfoData>,
    tagUpdateEvent:(()->Unit)? = null){

    Dialog(
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true),
        onDismissRequest = {
        isDialog.value = !isDialog.value
    }) {


        Surface(
            modifier = Modifier,
            shape = ShapeDefaults.Large,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            color = Color.LightGray

        ) {

            Column( modifier = Modifier.padding(vertical = 20.dp, horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,) {

                LazyVerticalGrid(
                    modifier  = Modifier
                        .padding(6.dp)
                        .height(200.dp),
                    columns = GridCells.Fixed(2),
                    userScrollEnabled = true,
                ) {

                    items(tagList) {

                        AssistChip(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            onClick = {
                              //  it.isChecked.value = !it.isChecked.value
                                it.isSet.value = !it.isSet.value
                            },
                            label = {
                                Row {
                                    Icon(
                                        it.icon,
                                        contentDescription = "",
                                        Modifier
                                            .padding(end = 6.dp)
                                            .size(AssistChipDefaults.IconSize)

                                    )

                                    Text(
                                        text = it.name,
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector =  if (it.isSet.value ) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                    contentDescription = "",
                                    modifier = Modifier
                                        .padding(end = 2.dp)
                                        .size(AssistChipDefaults.IconSize)
                                )
                            },
                            trailingIcon = {  },

                            )
                    }

                }

                Row (modifier = Modifier){

                    AssistChip(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        onClick = {
                            isDialog.value = false
                            tagUpdateEvent?.let {
                                it()
                            }
                        },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center) {
                                Icon(
                                    Icons.Outlined.DownloadDone,
                                    contentDescription = "",
                                    Modifier.padding(end = 10.dp)
                                )
                                Text(
                                    text = "Done"
                                )
                            }
                        },
                    )

                    AssistChip(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        onClick = {
                            tagList.clear()
                        },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center) {
                                Icon(
                                    Icons.Outlined.RemoveDone,
                                    contentDescription = "",
                                    Modifier.padding(end = 10.dp)
                                )
                                Text(
                                    text = "Remove"
                                )
                            }
                        },
                    )

                }




            }



        }
    }

}








@Preview
@Composable
fun PrevMemoSelectView() {
    val context = LocalContext.current
//    val viewModel =     WriteMemoViewModel(repository = RepositoryProvider.getRepository(context.applicationContext) )

 //   var isDialog = rememberSaveable { mutableStateOf(false) }


    val configuration = LocalConfiguration.current


    val isPortrait = when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> { true}
        else -> {  false }
    }

    var selectedItem by rememberSaveable { mutableStateOf(0) }

    GISMemoTheme {
        Surface(
            modifier = Modifier.background(color = Color.White)

        ) {

            //  MemoTagSelectView(isDialog = isDialog, tagList = tagInfoDataList)

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {

                if(isPortrait){

                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(90.dp)
                            .shadow(6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        mainScreens.forEachIndexed { index, item ->


                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                Box(modifier = Modifier) {

                                    IconButton(
                                        modifier = Modifier,
                                        onClick = {
                                            selectedItem = index
                                        },
                                        content = {
                                            item.icon?.let {
                                                Icon(
                                                    modifier = Modifier,
                                                    imageVector = it,
                                                    contentDescription = item.name
                                                )
                                            }
                                        }
                                    )

                                    if(selectedItem == index){
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .width(50.dp).height(40.dp)
                                                .border(width = 25.dp, color = Color.Blue.copy(alpha = 0.1f),
                                                    shape = ShapeDefaults.Large)
                                        )

                                    }

                                }

                                item.name?.let { Text( text = it) }

                            }


                        }

                    }

                } else {

                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .width(90.dp)
                            .shadow(6.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        mainScreens.forEachIndexed { index, item ->

                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                Box(modifier = Modifier) {
                                    IconButton(
                                        modifier = Modifier,
                                        onClick = {
                                            selectedItem = index
                                        },
                                        content = {
                                            item.icon?.let {
                                                Icon(
                                                    modifier = Modifier,
                                                    imageVector = it,
                                                    contentDescription = item.name
                                                )
                                            }
                                        }
                                    )

                                    if(selectedItem == index){
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .width(50.dp).height(40.dp)
                                                .border(width = 25.dp, color = Color.Blue.copy(alpha = 0.1f),
                                            shape = ShapeDefaults.Large)
                                        )
                                    }
                                }

                                item.name?.let { Text( text = it) }

                            }

                        }
                    }
                }

            }


/*


            Column(modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {





                BottomNavigation(
                    modifier = Modifier.height(100.dp),
                    backgroundColor = Color.Transparent,
                    contentColor = Color.Black,
                )
                {
                    mainScreens.forEachIndexed { index, item ->
                        NavigationRailItem(
                            modifier = Modifier
                                .align(Alignment.CenterVertically),
                            icon = {
                                item.icon?.let {
                                    Icon(
                                        it,
                                        contentDescription = item.name
                                    )
                                }
                            },
                            label = { androidx.compose.material.Text(item.name ?: "") },
                            selected = selectedItem == index,
                            onClick = {
                                selectedItem = index
                            }
                        )
                    }
                }




                NavigationRail(
                    modifier = Modifier.width(100.dp).background(color = Color.White).shadow(6.dp),
                    containerColor = Color.Transparent,
                    contentColor = Color.Black,
                    header = {
                       //Spacer(Modifier.height(20.dp))
                    }
                ) {
                    mainScreens.forEachIndexed { index, item ->
                        NavigationRailItem(
                            modifier = Modifier.padding(vertical = 2.dp),
                            icon = {
                                item.icon?.let {
                                    Icon(
                                        it,
                                        contentDescription = item.name
                                    )
                                }
                            },
                            label = {
                                androidx.compose.material.Text(
                                    item.name ?: ""
                                )
                            },
                            selected = selectedItem == index,
                            onClick = {
                                selectedItem = index
                            }
                        )

                    }
                }


            }
*/
        }

    }

}


 */
