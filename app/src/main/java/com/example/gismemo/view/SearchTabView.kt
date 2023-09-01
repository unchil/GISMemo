package com.example.gismemo.view

import android.app.Activity
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
import androidx.compose.material.icons.Icons
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
import com.example.gismemo.LocalUsableHaptic
import com.example.gismemo.R
import com.example.gismemo.shared.composables.LocalPermissionsManager
import com.example.gismemo.shared.composables.PermissionsManager
import com.example.gismemo.ui.theme.GISMemoTheme
import com.example.gismemo.viewmodel.ListViewModel
import kotlinx.coroutines.launch

typealias QueryData= Pair< SearchOption, SearchQueryDataValue>

data class TagInfoData(
    var icon : ImageVector,
    var name: Int,
    var isSet:MutableState<Boolean> = mutableStateOf(false)
)

val tagInfoDataList: List<TagInfoData> = listOf(
    TagInfoData(Icons.Outlined.ShoppingCart, R.string.search_hashTag_ShoppingCart),
    TagInfoData(Icons.Outlined.AccountBalance, R.string.search_hashTag_AccountBalance),
    TagInfoData(Icons.Outlined.Store, R.string.search_hashTag_Store),
    TagInfoData(Icons.Outlined.Theaters, R.string.search_hashTag_Theaters),
    TagInfoData(Icons.Outlined.FlightTakeoff, R.string.search_hashTag_FlightTakeoff),
    TagInfoData(Icons.Outlined.FlightLand, R.string.search_hashTag_FlightLand),
    TagInfoData(Icons.Outlined.Hotel, R.string.search_hashTag_Hotel),
    TagInfoData(Icons.Outlined.School, R.string.search_hashTag_School),
    TagInfoData(Icons.Outlined.Hiking, R.string.search_hashTag_Hiking),
    TagInfoData(Icons.Outlined.DownhillSkiing, R.string.search_hashTag_DownhillSkiing),
    TagInfoData(Icons.Outlined.Kayaking, R.string.search_hashTag_Kayaking),
    TagInfoData(Icons.Outlined.Skateboarding, R.string.search_hashTag_Skateboarding),
    TagInfoData(Icons.Outlined.Snowboarding, R.string.search_hashTag_Snowboarding),
    TagInfoData(Icons.Outlined.ScubaDiving, R.string.search_hashTag_ScubaDiving),
    TagInfoData(Icons.Outlined.RollerSkating, R.string.search_hashTag_RollerSkating),
    TagInfoData(Icons.Outlined.Photo, R.string.search_hashTag_Photo),
    TagInfoData(Icons.Outlined.Restaurant, R.string.search_hashTag_Restaurant),
    TagInfoData(Icons.Outlined.Park, R.string.search_hashTag_Park),
    TagInfoData(Icons.Outlined.LocalCafe, R.string.search_hashTag_LocalCafe),
    TagInfoData(Icons.Outlined.LocalTaxi, R.string.search_hashTag_LocalTaxi),
    TagInfoData(Icons.Outlined.Forest, R.string.search_hashTag_Forest),
    TagInfoData(Icons.Outlined.EvStation, R.string.search_hashTag_EvStation),
    TagInfoData(Icons.Outlined.FitnessCenter, R.string.search_hashTag_FitnessCenter),
    TagInfoData(Icons.Outlined.House, R.string.search_hashTag_House),
    TagInfoData(Icons.Outlined.Apartment, R.string.search_hashTag_Apartment),
    TagInfoData(Icons.Outlined.Cabin, R.string.search_hashTag_Cabin)
).sortedBy {
    it.name
}

fun  List<TagInfoData>.clear(){
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

@Composable
fun RadioButtonGroupView(
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




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchView(
    isSearchRefreshing:MutableState<Boolean> = mutableStateOf( false),
    sheetControl: (() -> Unit)? = null,
    onEvent: ((ListViewModel.Event) -> Unit)? = null,
    onMessage:(() -> Unit)? = null
){

    val context = LocalContext.current
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

    val secretOption =  listOf(
        context.resources.getString(R.string.search_radioBt_select),
        context.resources.getString(R.string.search_radioBt_none),
        context.resources.getString(R.string.search_radioBt_all)
    )
    val markerOption =  listOf(
        context.resources.getString(R.string.search_radioBt_select),
        context.resources.getString(R.string.search_radioBt_none),
        context.resources.getString(R.string.search_radioBt_all)
    )


    val secretRadioGroupState = rememberSaveable {
        mutableStateOf(secretOption.lastIndex )
    }

    val markerRadioGroupState = rememberSaveable{
        mutableStateOf(markerOption.lastIndex )
    }



    var isTagBox by rememberSaveable{  mutableStateOf(false)}
    val isDateBox = rememberSaveable{  mutableStateOf(false)}

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

    /*
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val isPressed by interactionSource.collectIsPressedAsState()

     */

// context.resources.getString(R.string.search_searchBar_placeholder)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(color = MaterialTheme.colorScheme.background),
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
                placeholder = { Text(context.resources.getString(R.string.search_searchBar_placeholder)) },
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
                                contentDescription = "Search"
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
                                    imageVector = Icons.Outlined.Replay,
                                    contentDescription = "Clear"
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
                        contentDescription = "Secret"
                    )
                    Text(
                        modifier = Modifier,
                        text = context.resources.getString(R.string.search_radioBtGroup_secret)
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
                        text =  context.resources.getString(R.string.search_radioBtGroup_marker)
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
                        Text(context.resources.getString(R.string.search_hashTag))
                        Icon(
                            modifier = Modifier.scale(1.5f),
                            imageVector = if (isTagBox) Icons.Outlined.ArrowDropDown else Icons.Outlined.ArrowRight,
                            contentDescription = "tag "
                        )
                    }
                })



            AssistChipGroupView(
                isVisible = isTagBox,
                setState = selectedTagArray,
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
                        Text(context.resources.getString(R.string.search_period))
                        Icon(
                            modifier = Modifier.scale(1.5f),
                            imageVector = if (isDateBox.value)  Icons.Outlined.ArrowDropDown else Icons.Outlined.ArrowRight,
                            contentDescription = "date "
                        )
                    }
                })

            AnimatedVisibility(visible = isDateBox.value) {

                DateRangePicker(
                    state = dateRangePickerState,
                    modifier = Modifier
                        .height(420.dp),
                    title = { Text("") },
                    headline = {
                        Text(
                            modifier = Modifier.padding(start = 10.dp),
                            text = context.resources.getString(R.string.search_dateRangePicker_headline),
                            style = TextStyle(
                                fontWeight = FontWeight.Light,
                                fontSize = 16.sp
                            )
                        )
                    }
                )

            }

        }

}





@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssistChipGroupView(
    modifier: Modifier = Modifier,
    isVisible:Boolean =true,
    setState:MutableState<ArrayList<Int>> = mutableStateOf( arrayListOf()),
    content: @Composable (( ) -> Unit)? = null
){

    val context = LocalContext.current

    tagInfoDataList.clear()
    setState.value.forEach {
        tagInfoDataList[it].isSet.value = true
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
            modifier = Modifier.then(modifier)
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
                            it.isSet.value = !it.isSet.value
                            if (it.isSet.value)  setState.value.add(index) else   setState.value.remove(index)
                        },
                        label = {
                            Row {
                                Icon(
                                    imageVector = it.icon,
                                    contentDescription = "",
                                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                                )
                                Text(text = context.resources.getString(it.name))
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector =   if (it.isSet.value) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                contentDescription = "",
                                modifier = Modifier.size(AssistChipDefaults.IconSize),

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




@Preview
@Composable
private fun PrevSearchView(
    modifier: Modifier = Modifier,
){

    val permissionsManager = PermissionsManager()
    CompositionLocalProvider(LocalPermissionsManager provides permissionsManager) {


        GISMemoTheme {
            Surface(
                modifier = Modifier.background(color = Color.White)
            ) {

                AssistChipGroupView(  )

            }
        }


    }

}

