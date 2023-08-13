package com.example.gismemo.view

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import coil.size.Size
import com.example.gismemo.LocalUsableHaptic
import com.example.gismemo.R
import com.example.gismemo.data.RepositoryProvider
import com.example.gismemo.db.LocalLuckMemoDB
import com.example.gismemo.db.LuckMemoDB
import com.example.gismemo.db.UnixTimeToString
import com.example.gismemo.db.yyyyMMddHHmm
import com.example.gismemo.shared.composables.LocalPermissionsManager
import com.example.gismemo.shared.composables.PermissionsManager
import com.example.gismemo.shared.utils.SnackBarChannelType
import com.example.gismemo.shared.utils.snackbarChannelList
import com.example.gismemo.ui.theme.GISMemoTheme
import com.example.gismemo.viewmodel.ListViewModel
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class TagInfoData(
    var icon : ImageVector,
    var name: String,
    var isSet:Boolean = false
)



val tagInfoDataList: List<TagInfoData> = listOf(
    TagInfoData(Icons.Outlined.ShoppingCart, "마트"),
    TagInfoData(Icons.Outlined.AccountBalance, "박물관"),
    TagInfoData(Icons.Outlined.Store, "가게"),
    TagInfoData(Icons.Outlined.Theaters, "극장"),
    TagInfoData(Icons.Outlined.FlightTakeoff, "이륙"),
    TagInfoData(Icons.Outlined.FlightLand, "착륙"),
    TagInfoData(Icons.Outlined.Hotel, "호텔"),
    TagInfoData(Icons.Outlined.School, "학교"),
    TagInfoData(Icons.Outlined.Hiking, "하이킹"),
    TagInfoData(Icons.Outlined.DownhillSkiing, "스키"),
    TagInfoData(Icons.Outlined.Kayaking, "카약"),
    TagInfoData(Icons.Outlined.Skateboarding, "스케이트보딩"),
    TagInfoData(Icons.Outlined.Snowboarding, "스노우보딩"),
    TagInfoData(Icons.Outlined.ScubaDiving, "스쿠버다이빙"),
    TagInfoData(Icons.Outlined.RollerSkating, "롤러스케이팅"),
    TagInfoData(Icons.Outlined.Photo, "포토스팟"),
    TagInfoData(Icons.Outlined.Restaurant, "음식점"),
    TagInfoData(Icons.Outlined.Park, "공원"),
    TagInfoData(Icons.Outlined.LocalCafe, "카페"),
    TagInfoData(Icons.Outlined.LocalTaxi, "택시"),
    TagInfoData(Icons.Outlined.Forest, "숲"),
    TagInfoData(Icons.Outlined.EvStation, "전기차 충전"),
    TagInfoData(Icons.Outlined.FitnessCenter, "피트니스"),
    TagInfoData(Icons.Outlined.House, "집"),
    TagInfoData(Icons.Outlined.Apartment, "아파트"),
    TagInfoData(Icons.Outlined.Cabin, "캐빈")
).sortedBy {
    it.name
}

fun  List<TagInfoData>.clear(){
    this.forEach {
        it.isSet = false
    }
}

data class TagInfoDataNew(
    var icon : ImageVector,
    var name: String,
    var isSet:MutableState<Boolean> = mutableStateOf(false)
)


val tagInfoDataListNew: List<TagInfoDataNew> = listOf(
    TagInfoDataNew(Icons.Outlined.ShoppingCart, "마트"),
    TagInfoDataNew(Icons.Outlined.AccountBalance, "박물관"),
    TagInfoDataNew(Icons.Outlined.Store, "가게"),
    TagInfoDataNew(Icons.Outlined.Theaters, "극장"),
    TagInfoDataNew(Icons.Outlined.FlightTakeoff, "이륙"),
    TagInfoDataNew(Icons.Outlined.FlightLand, "착륙"),
    TagInfoDataNew(Icons.Outlined.Hotel, "호텔"),
    TagInfoDataNew(Icons.Outlined.School, "학교"),
    TagInfoDataNew(Icons.Outlined.Hiking, "하이킹"),
    TagInfoDataNew(Icons.Outlined.DownhillSkiing, "스키"),
    TagInfoDataNew(Icons.Outlined.Kayaking, "카약"),
    TagInfoDataNew(Icons.Outlined.Skateboarding, "스케이트보딩"),
    TagInfoDataNew(Icons.Outlined.Snowboarding, "스노우보딩"),
    TagInfoDataNew(Icons.Outlined.ScubaDiving, "스쿠버다이빙"),
    TagInfoDataNew(Icons.Outlined.RollerSkating, "롤러스케이팅"),
    TagInfoDataNew(Icons.Outlined.Photo, "포토스팟"),
    TagInfoDataNew(Icons.Outlined.Restaurant, "음식점"),
    TagInfoDataNew(Icons.Outlined.Park, "공원"),
    TagInfoDataNew(Icons.Outlined.LocalCafe, "카페"),
    TagInfoDataNew(Icons.Outlined.LocalTaxi, "택시"),
    TagInfoDataNew(Icons.Outlined.Forest, "숲"),
    TagInfoDataNew(Icons.Outlined.EvStation, "전기차 충전"),
    TagInfoDataNew(Icons.Outlined.FitnessCenter, "피트니스"),
    TagInfoDataNew(Icons.Outlined.House, "집"),
    TagInfoDataNew(Icons.Outlined.Apartment, "아파트"),
    TagInfoDataNew(Icons.Outlined.Cabin, "캐빈")
).sortedBy {
    it.name
}

fun  List<TagInfoDataNew>.clearNew(){
    this.forEach {
        it.isSet.value = false
    }
}




enum class SearchOption {
    TITLE, SECRET, MARKER, TAG, DATE
}

fun SearchOption.name():String{
    return when(this){
        SearchOption.TITLE ->  "제목"
        SearchOption.SECRET -> "보안"
        SearchOption.MARKER -> "마커"
        SearchOption.TAG -> "태그"
        SearchOption.DATE -> "날짜"
    }
}


sealed class SearchQueryDataValue {
    data class radioGroupOption(val index:Int) : SearchQueryDataValue()
    data class tagOption(val indexList: ArrayList<Int>): SearchQueryDataValue()
    data class dateOption(val fromToDate:Pair<Long,Long>): SearchQueryDataValue()
    data class titleOption(val title: String): SearchQueryDataValue()
}

data class SearchQueryData (
    var icon : ImageVector ,
   var searchType: SearchOption,
    var isSet:MutableState<Boolean>
        = mutableStateOf(false) ,
    var selectedValue:MutableState<SearchQueryDataValue?>
        = mutableStateOf(null)

)

val searchQueryDataList: List<SearchQueryData> = listOf(
    SearchQueryData(Icons.Outlined.Lock, SearchOption.SECRET),
    SearchQueryData(Icons.Outlined.LocationOn, SearchOption.MARKER),
    SearchQueryData(Icons.Outlined.Class, SearchOption.TAG),
    SearchQueryData(Icons.Outlined.CalendarMonth, SearchOption.DATE)
)


@Composable
fun RadioButtonGroupView(
    isVisible:MutableState<Boolean> = mutableStateOf(true),
    state:MutableState<Int>,
    items:List<String>,
    content: @Composable (( ) -> Unit)? = null
){



    val isUsableHaptic = LocalUsableHaptic.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    fun hapticProcessing(){
        if(isUsableHaptic){
            coroutineScope.launch {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }



    val (selectedOption, onOptionSelected) = mutableStateOf(items[state.value])

    Row(
        modifier = Modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        content?.let {
            it()
        }


        items.forEachIndexed { index, it ->
            Row(
                modifier = Modifier
                    .selectable(
                        selected = (it == selectedOption),
                        onClick = {
                            hapticProcessing()
                            onOptionSelected( it )
                           state.value = index
                        },
                        role = Role.RadioButton
                    ),
                horizontalArrangement = Arrangement.Center
            ) {
                RadioButton(
                    selected = (it == selectedOption),
                    onClick = null
                )
                Text(
                    text = it,
                    modifier = Modifier
                )
            }
        }
    }

}




@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchView(
    isSearchRefreshing:MutableState<Boolean> = mutableStateOf( false),
    sheetControl: (() -> Unit)? = null,
    onEvent: ((ListViewModel.Event) -> Unit)? = null,
    onMessage:(() -> Unit)? = null
){


    val isUsableHaptic = LocalUsableHaptic.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    fun hapticProcessing(){
        if(isUsableHaptic){
            coroutineScope.launch {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }



    val isVisible:MutableState<Boolean> = mutableStateOf(true)

    val dateRangePickerState = rememberDateRangePickerState()

    val secretOption =  listOf("Secret", "None", "All")
    val markerOption =  listOf("Marker", "None", "All")


    val secretRadioGroupState = rememberSaveable {
        mutableStateOf(secretOption.lastIndex )
    }

    val markerRadioGroupState = rememberSaveable{
        mutableStateOf(markerOption.lastIndex )
    }



    var isTagBox by rememberSaveable{  mutableStateOf(true)}
    val isDateBox = rememberSaveable{  mutableStateOf(true)}

    val selectedTagArray:MutableState<ArrayList<Int>> = rememberSaveable{ mutableStateOf(arrayListOf())  }
    val query_title = rememberSaveable { mutableStateOf("") }


    val startLauncherRecognizerIntent = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {

        if (it.resultCode == Activity.RESULT_OK) {
            val result =  it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            query_title.value = query_title.value + result?.get(0).toString() + " "
        }
    }

    val initStateValue = {
        query_title.value = ""
        dateRangePickerState.setSelection(null, null)
        secretRadioGroupState.value =  secretOption.lastIndex
        markerRadioGroupState.value = markerOption.lastIndex
        selectedTagArray.value = arrayListOf()
    }

    val onSearch: (String) -> Unit = { searchTitle ->

        val queryDataList =  mutableListOf<QueryData>()

        searchTitle.trim().let { queryString ->
            if (queryString.isNotEmpty()) {
                queryDataList.add(
                    QueryData(SearchOption.TITLE,
                        SearchQueryDataValue.titleOption(title = queryString )
                    )
                )
            }
        }


        dateRangePickerState.selectedStartDateMillis?.let {
            if(it != 0L){
                QueryData(SearchOption.DATE,
                    SearchQueryDataValue.dateOption(
                        fromToDate = Pair( dateRangePickerState.selectedStartDateMillis ?: 0, dateRangePickerState.selectedEndDateMillis ?: 0)
                    )
                )
            }
        }

        if( secretRadioGroupState.value  <  secretOption.lastIndex){
            queryDataList.add(
                QueryData(SearchOption.SECRET,
                    SearchQueryDataValue.radioGroupOption(
                        index = secretRadioGroupState.value
                    )
                )
            )
        }

        if(markerRadioGroupState.value < markerOption.lastIndex){
            queryDataList.add(
                QueryData(SearchOption.MARKER,
                    SearchQueryDataValue.radioGroupOption(
                        index = markerRadioGroupState.value
                    )
                )
            )
        }

        if( selectedTagArray.value.isNotEmpty()){
            queryDataList.add(
                QueryData(SearchOption.TAG,
                    SearchQueryDataValue.tagOption(
                        indexList =  selectedTagArray.value
                    ))
            )
        }



        onEvent?.let {
            it(ListViewModel.Event.Search(queryDataList))
            if(queryDataList.isNotEmpty()){
                isSearchRefreshing.value= true
            }

        }

        initStateValue()

        sheetControl?.let {
            it()
        }

    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(color = Color.White),
        verticalArrangement = Arrangement.Top
    ) {

            SearchBar(
                query = query_title.value,
                onQueryChange = {
                    query_title.value = it
                },
                onSearch = onSearch,
                active = isVisible.value,
                onActiveChange = {
                    isVisible.value = it
                },
                placeholder = { Text("Enter a title to search for") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 10.dp)
                    .padding(top = 10.dp)
                    .clip(ShapeDefaults.Medium),
                leadingIcon = {


                    IconButton(
                        modifier = Modifier,
                        onClick = {
                            hapticProcessing()
                            onSearch(query_title.value)
                        },
                        content = {
                            Icon(
                                modifier = Modifier,
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "search"
                            )
                        }
                    )


                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            modifier = Modifier,
                            onClick = {
                                hapticProcessing()
                                startLauncherRecognizerIntent.launch(recognizerIntent())
                            },
                            content = {
                                Icon(
                                    modifier = Modifier,
                                    imageVector = Icons.Outlined.Mic,
                                    contentDescription = "SpeechToText"
                                )
                            }
                        )


                        IconButton(
                            modifier = Modifier,
                            onClick = {
                                hapticProcessing()
                                initStateValue()
                                onMessage?.let {
                                    it()
                                }
                            },
                            content = {
                                Icon(
                                    modifier = Modifier,
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "clear"
                                )
                            }
                        )


                    }
                }
            ) { }


            Divider(
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            )



            RadioButtonGroupView(
                state = secretRadioGroupState,
                items = secretOption
            ) {
                Row(modifier = Modifier) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "secret"
                    )
                    Text(
                        modifier = Modifier,
                        text = " IsSecret : "
                    )
                }
            }

            Divider(
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            )

            RadioButtonGroupView(
                state = markerRadioGroupState,
                items = markerOption
            ) {
                Row(modifier = Modifier) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "marker"
                    )
                    Text(
                        modifier = Modifier,
                        text = "IsMarker : "
                    )
                }
            }

            Divider(
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            )


            androidx.compose.material.IconButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = {
                    hapticProcessing()
                    isTagBox = !isTagBox
                          },
                content = {
                    Row(
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Class,
                            contentDescription = "tag"
                        )
                        Text("hashTag")
                        Icon(
                            modifier = Modifier,
                            imageVector = if (isTagBox) Icons.Outlined.UnfoldLess else Icons.Outlined.UnfoldMore,
                            contentDescription = "tag "
                        )
                    }
                })



            AssistChipGroupView(
                isVisible = isTagBox,
                setState = selectedTagArray,
                getState = {
                    selectedTagArray.value = it
                }
            )



        Divider(
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            )




            androidx.compose.material.IconButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = {
                    hapticProcessing()
                    isDateBox.value = !isDateBox.value
                          },
                content = {
                    Row(
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "date"
                        )
                        Text("Search Period")
                        Icon(
                            modifier = Modifier,
                            imageVector = if (isDateBox.value) Icons.Outlined.UnfoldLess else Icons.Outlined.UnfoldMore,
                            contentDescription = "date "
                        )
                    }
                })

            AnimatedVisibility(visible = isDateBox.value) {

                DateRangePicker(
                    state = dateRangePickerState,
                    modifier = Modifier.height(420.dp),
                    title = { Text("") },
                    headline = {
                        Text(
                            modifier = Modifier.padding(start = 10.dp),
                            text = "start date - end date",
                            style = TextStyle(
                                color = Color.Black,
                                fontWeight = FontWeight.Light,
                                fontSize = 16.sp
                            )
                        )
                    }
                )

            }

        }

}


typealias QueryData= Pair< SearchOption, SearchQueryDataValue>


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssistChipGroupView(
    modifier: Modifier = Modifier,
    isVisible:Boolean =true,
    setState:MutableState<ArrayList<Int>> = mutableStateOf( arrayListOf()),
    getState:((ArrayList<Int>)->Unit)? = null,
    content: @Composable (( ) -> Unit)? = null
){

    tagInfoDataList.clear()
    if(setState.value.isNotEmpty()){
        setState.value.forEach {
            //tagInfoDataList[it].isSet.value = true
            tagInfoDataList[it].isSet = true
        }
    }

    val  lazyStaggeredGridState = rememberLazyStaggeredGridState()



    val itemModifier = Modifier.wrapContentSize()


    val isUsableHaptic = LocalUsableHaptic.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    fun hapticProcessing(){
        if(isUsableHaptic){
            coroutineScope.launch {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }


    AnimatedVisibility(visible = isVisible) {


        Column (
            modifier = Modifier
                .then(modifier)
        ){


            LazyHorizontalStaggeredGrid(
                rows =  StaggeredGridCells.Fixed(4),
                modifier  = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .height(200.dp),
                state = lazyStaggeredGridState,
                contentPadding =  PaddingValues(10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalItemSpacing = 6.dp,
                userScrollEnabled = true,
            ){



                itemsIndexed(tagInfoDataList) { index, it ->


                    AssistChip(
                        modifier = itemModifier,
                        onClick = {
                            hapticProcessing()
                            //      it.isSet.value = !it.isSet.value
                            it.isSet = !it.isSet

                            if (getState != null) {
                                val selectedTagArray = arrayListOf<Int>()
                                tagInfoDataList.forEachIndexed { index, tagInfoData ->
                                    if (tagInfoData.isSet) {
                                        if (tagInfoData.isSet) {
                                            selectedTagArray.add(index)
                                        }
                                    }
                                }
                                tagInfoDataList.clear()
                                getState(selectedTagArray)
                            }
                        },
                        label = {
                            Row {
                                Icon(
                                    imageVector = it.icon,
                                    contentDescription = "",
                                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                                )
                                Text(text = it.name)
                            }
                        },
                        leadingIcon = {
                            val trailingIcon =
                                if (it.isSet) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank
                            Icon(
                                imageVector = trailingIcon,
                                contentDescription = "",
                                modifier = Modifier.size(AssistChipDefaults.IconSize)
                            )
                        },
                    )



                } // itemsIndexed

            }

            content?.let {
                it()
            }

        }

    }

}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssistChipGroupViewNew(
    modifier: Modifier = Modifier,
    setState:ArrayList<Int> = arrayListOf(),
    isVisible:Boolean =true,
    content: @Composable (( ) -> Unit)? = null
){



        tagInfoDataListNew.clearNew()
        setState.forEach {
            tagInfoDataListNew[it].isSet.value = true
        }



    val  lazyStaggeredGridState = rememberLazyStaggeredGridState()

    val itemModifier = Modifier.wrapContentSize()

    val isUsableHaptic = LocalUsableHaptic.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    fun hapticProcessing(){
        if(isUsableHaptic){
            coroutineScope.launch {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }

    AnimatedVisibility(visible = isVisible) {

        Column (
            modifier = Modifier
                .then(modifier)
        ){

            LazyHorizontalStaggeredGrid(
                rows =  StaggeredGridCells.Fixed(4),
                modifier  = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .height(200.dp),
                state = lazyStaggeredGridState,
                contentPadding =  PaddingValues(10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalItemSpacing = 6.dp,
                userScrollEnabled = true,
            ){

                itemsIndexed(tagInfoDataListNew) { index, it ->

                    AssistChip(
                        modifier = itemModifier,
                        onClick = {
                            hapticProcessing()
                            it.isSet.value = !it.isSet.value

                        },
                        label = {
                            Row {
                                Icon(
                                    imageVector = it.icon,
                                    contentDescription = "",
                                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                                )
                                Text(text = it.name)
                            }
                        },
                        leadingIcon = {
                            val icon =
                                if (it.isSet.value) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank
                            Icon(
                                imageVector = icon,
                                contentDescription = "",
                                modifier = Modifier.size(AssistChipDefaults.IconSize)
                            )
                        },
                    )
                } // itemsIndexed

            }

            content?.let {
                it()
            }

        }

    }

}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PrevSearchView(
    modifier: Modifier = Modifier,
){

    val permissionsManager = PermissionsManager()
    CompositionLocalProvider(LocalPermissionsManager provides permissionsManager) {

    var isSet by rememberSaveable {
        mutableStateOf(false)
    }

        GISMemoTheme {
            Surface(
                modifier = Modifier.background(color = Color.White)
            ) {

                AssistChipGroupViewNew(setState = arrayListOf(0,1,2,3)  )

            }
        }


    }

}

