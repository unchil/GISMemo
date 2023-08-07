package com.example.gismemo.view

import android.annotation.SuppressLint
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gismemo.db.UnixTimeToString
import com.example.gismemo.db.yyyyMMddHHmm
import com.example.gismemo.shared.utils.SnackBarChannelType
import com.example.gismemo.shared.utils.snackbarChannelList
import com.example.gismemo.ui.theme.GISMemoTheme
import com.example.gismemo.viewmodel.ListViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class TagInfoData(
    var icon : ImageVector ,
    var name: String,
    var isSet:MutableState<Boolean>  = mutableStateOf(false)
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


/*
@Composable
fun SearchTabView(
    isVisible: MutableState<Boolean>,
    state:SearchQueryDataState
){

    Row(
        modifier = Modifier
            .height(80.dp)
            .fillMaxWidth()
            .padding(10.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color = Color.White),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Button(
            onClick = {
                state.second.value = SearchOption.TITLE
                isVisible.value = true
            },
            modifier = Modifier.padding(10.dp)
        ) {

            Row {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = "검색",
                    Modifier.size(AssistChipDefaults.IconSize)
                )
                Text("검색")
            }

        }


        searchQueryDataList.forEach {  searchQueryData ->

            val name = searchQueryData.searchType.name
            val imageVector = searchQueryData.icon

            IconButton(
                onClick = {
                    searchQueryData.isSet.value = true
                    state.second.value = searchQueryData.searchType
                    isVisible.value = true
                }
            ) {
                Icon(
                    imageVector,
                    contentDescription = name,
                    Modifier.size(AssistChipDefaults.IconSize)
                )
            }

        }

    }

}



@Composable
fun RadioButtonGroupDialog(
    isVisible:MutableState<Boolean>,
    state:RadioGroupState,
    onClose:((Int)->Unit)? = null,
){

    val (selectedOption, onOptionSelected)
        = rememberSaveable { mutableStateOf(state.second[state.first.value]) }

    Dialog(
        onDismissRequest = {
            isVisible.value = false
            onClose?.let {
                it(state.first.value)
            }
        }
    ) {



        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = ShapeDefaults.Small,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {

            Row(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth()
                    .selectableGroup(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                state.second.forEachIndexed { index, it ->
                    Row(
                        modifier = Modifier
                            .selectable(
                                selected = (it == selectedOption),
                                onClick = {
                                    onOptionSelected( it )
                                    state.first.value= index
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
    }

}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleSearchDialog(
    isVisible:MutableState<Boolean>,
    state:SearchQueryDataState,
    secretOption: List<String>,
    markerOption: List<String>,
    onEvent: ((ListViewModel.Event) -> Unit)? = null,
    onReset:(()->Unit)? = null
){

    val scrollState = rememberScrollState()



    Dialog(  onDismissRequest = {  isVisible.value = false }, ) {

        val query_title = rememberSaveable { mutableStateOf("") }
        val recognizerIntent = remember { recognizerIntent }

        val startLauncherRecognizerIntent = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {

            if (it.resultCode == Activity.RESULT_OK) {
                val result =  it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                query_title.value = query_title.value + result?.get(0).toString() + " "
            }
        }




        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = ShapeDefaults.Small,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {

            SearchBar(
                query = query_title.value ,
                onQueryChange = {
                    query_title.value = it
                },
                onSearch = { searchTitle ->

                    val queryDataList =  mutableListOf<QueryData>()

                    searchTitle.trim().let { queryString ->
                        if (queryString.isNotEmpty()) {
                            queryDataList.add(
                                QueryData(SearchOption.TITLE, SearchQueryDataValue.titleOption(title = queryString ))
                            )
                        }
                    }

                    state.first.forEach {
                        if(it.isSet.value) {
                            when( it.selectedValue.value){

                                is SearchQueryDataValue.dateOption -> {
                                    queryDataList.add(
                                        QueryData(SearchOption.DATE, SearchQueryDataValue.dateOption(
                                            fromToDate = Pair((it.selectedValue.value as SearchQueryDataValue.dateOption).fromToDate.first,
                                                (it.selectedValue.value as SearchQueryDataValue.dateOption).fromToDate.second)))
                                    )

                                }

                                is SearchQueryDataValue.radioGroupOption -> {

                                    when(it.searchType){
                                        SearchOption.SECRET -> {
                                            queryDataList.add(
                                                QueryData(SearchOption.SECRET, SearchQueryDataValue.radioGroupOption(
                                                    index = (it.selectedValue.value as SearchQueryDataValue.radioGroupOption).index ))
                                            )
                                        }
                                        SearchOption.MARKER -> {
                                            queryDataList.add(
                                                QueryData(SearchOption.MARKER, SearchQueryDataValue.radioGroupOption(
                                                    index = (it.selectedValue.value as SearchQueryDataValue.radioGroupOption).index ))
                                            )
                                        }
                                        else -> { }
                                    }
                                }

                                is SearchQueryDataValue.tagOption -> {
                                    queryDataList.add(
                                        QueryData(SearchOption.TAG, SearchQueryDataValue.tagOption(
                                            indexList =  (it.selectedValue.value as SearchQueryDataValue.tagOption).indexList
                                        ))
                                    )

                                }
                                else -> {}
                            }
                            it.selectedValue.value = null
                        }

                    }

                    onEvent?.let {
                        it(ListViewModel.Event.Search(queryDataList))
                    }

                    onReset?.let { it() }
                },

                active = isVisible.value,
                onActiveChange = {
                    isVisible.value = it
                },
                placeholder = { Text("검색 할 제목을 입력 하세요")},
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth(),
                leadingIcon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = "search")},
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            modifier = Modifier,
                            onClick = {
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
                        if (query_title.value.isNotEmpty()) {
                            Icon(
                                modifier = Modifier.clickable {
                                    query_title.value = ""
                                },
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close icon"
                            )
                        }
                    }
                }
            ) {

                var dateText = ""
                var secretText = ""
                var markerText = ""
                var tagText = ""



                state.first.forEach {

                    if(it.isSet.value) {

                        when( it.selectedValue.value){

                            is SearchQueryDataValue.dateOption -> {
                                dateText =
                                    "\t\t\t시작일 : ${UnixTimeToString((it.selectedValue.value as SearchQueryDataValue.dateOption).fromToDate.first, yyyyMMddHHmm)} \n" +
                                            "\t\t\t종료일 : ${UnixTimeToString((it.selectedValue.value as SearchQueryDataValue.dateOption).fromToDate.second, yyyyMMddHHmm)} \n"
                            }

                            is SearchQueryDataValue.radioGroupOption -> {

                                when(it.searchType){
                                    SearchOption.SECRET -> {
                                        secretText = secretOption[(it.selectedValue.value as SearchQueryDataValue.radioGroupOption).index]
                                    }
                                    SearchOption.MARKER -> {
                                        markerText = markerOption[(it.selectedValue.value as SearchQueryDataValue.radioGroupOption).index]
                                    }
                                    else -> { }
                                }
                            }

                            is SearchQueryDataValue.tagOption -> {

                                (it.selectedValue.value as SearchQueryDataValue.tagOption).indexList.forEach {
                                    tagText = tagText + tagInfoDataList[it].name + " "
                                }

                            }
                            else -> {}
                        }

                    }

                }

                Column(modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
                    .padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween) {

                    if (dateText.isNotEmpty()) {
                        Row {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = "date"
                            )
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = "검색 기간"
                            )
                        }

                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = dateText
                        )
                    }

                    if (secretText.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = "secret"
                            )
                            Text(
                                modifier = Modifier,
                                text = " 보안조건 : "
                            )
                        }
                            Text(
                                modifier = Modifier,
                                text = "\t\t\t${secretText}\n"
                            )

                    }

                    if (markerText.isNotEmpty()) {
                        Row (modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = "marker"
                            )
                            Text(
                                modifier = Modifier,
                                text = "마커조건 : "
                            )
                        }
                            Text(
                                modifier = Modifier,
                                text = "\t\t\t${markerText}\n"
                            )


                    }

                    if (tagText.isNotEmpty()) {
                        Row (modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                imageVector = Icons.Outlined.Class,
                                contentDescription = "tag"
                            )
                            Text(
                                modifier = Modifier,
                                text = "태그조건 : "
                            )
                        }

                            Text(
                                modifier = Modifier,
                                text = "\t${tagText}\n"
                            )


                    }

                }




            }//SearchBar

        }//Surface




    }
}





@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssistChipGroupDialog(
    isVisible:MutableState<Boolean>,
    setState:MutableState<ArrayList<Int>> = mutableStateOf( arrayListOf()),
    getState:((ArrayList<Int>)->Unit)? = null,
    content: @Composable (( ArrayList<Int> ) -> Unit)? = null
){

    if(isVisible.value) {

        Dialog(
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
            onDismissRequest = {
                isVisible.value = false

                if (getState != null) {
                    val selectedTagArray = arrayListOf<Int>()
                    tagInfoDataList.forEachIndexed { index, tagInfoData ->
                        if (tagInfoData.isSet.value) {
                            selectedTagArray.add(index)
                        }
                    }
                    tagInfoDataList.clear()
                    getState(selectedTagArray)
                }
            }

        ) {

            tagInfoDataList.clear()
            if (setState.value.isNotEmpty()) {
                setState.value.forEach {
                    tagInfoDataList[it].isSet.value = true
                }
            }

            val lazyStaggeredGridState = rememberLazyStaggeredGridState()
            val tagList = rememberSaveable { tagInfoDataList }
            val itemModifier = Modifier.wrapContentSize()


            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                shape = ShapeDefaults.Small,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {

                Column {

                    if (content != null) {

                        val selectedTagArray = arrayListOf<Int>()
                        tagInfoDataList.forEachIndexed { index, tagInfoData ->
                            if (tagInfoData.isSet.value) {
                                selectedTagArray.add(index)
                            }
                        }
                        content(selectedTagArray)
                    }

                    Divider()

                    LazyHorizontalStaggeredGrid(
                        rows = StaggeredGridCells.Fixed(5),
                        modifier = Modifier.fillMaxWidth(),
                        state = lazyStaggeredGridState,
                        contentPadding = PaddingValues(10.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalItemSpacing = 6.dp,
                        userScrollEnabled = true,
                    ) {


                        itemsIndexed(tagList) { index, it ->

                            AssistChip(
                                modifier = itemModifier,
                                onClick = { it.isSet.value = !it.isSet.value },
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
                                        if (it.isSet.value) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank
                                    Icon(
                                        imageVector = trailingIcon,
                                        contentDescription = "",
                                        modifier = Modifier.size(AssistChipDefaults.IconSize)
                                    )
                                },
                            )

                        } // itemsIndexed

                    }

                }

            }


        }

    }

}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerFullDialog(
    isVisible:MutableState<Boolean>,
    state:MutableState<Pair<Long,Long>>,
    onClear: ((Pair<Long,Long>) -> Unit)?,
    onSave: ((Pair<Long,Long>) -> Unit)?

){

    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = if(state.value.first == 0L) null else state.value.first,
        initialSelectedEndDateMillis = if(state.value.second == 0L) null else state.value.second
    )

    Column(modifier = Modifier
        .fillMaxSize()
        .background(color = Color.White), verticalArrangement = Arrangement.Top) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {


            TextButton(
                onClick = {
                    isVisible.value = !isVisible.value

                    state.value = Pair(0, 0)
                    onClear?.let {
                        it(state.value)
                    }
                },

            ) {
                Text(text = "Clear")
            }


            TextButton(
                onClick = {
                    isVisible.value = !isVisible.value
                    state.value = Pair(dateRangePickerState.selectedStartDateMillis?: 0, dateRangePickerState.selectedEndDateMillis?:0)
                    onSave?.let {
                        it(state.value)
                    }

                },
                enabled = dateRangePickerState.selectedEndDateMillis != null  // SearchTabType 에 따라 다른 체크
            ) {
                Text(text = "Save")
            }

        }

        DateRangePicker(state = dateRangePickerState, modifier = Modifier)
    }
}



@Composable
fun DrawerIconButtons(
    modifier: Modifier,
    isDrawerable:MutableState<Boolean> = mutableStateOf(false),
    content: @Composable ((RowScope ) -> Unit)? = null
){

    IconButton(
        modifier = Modifier.then(modifier),
        onClick = { isDrawerable.value = !isDrawerable.value}
    ) {
        Icon(
            modifier = Modifier.scale(1f),
            imageVector = Icons.Outlined.Code,
            contentDescription = "RowDrawer",
        )
    }


}



 */



@Composable
fun RadioButtonGroupView(
    isVisible:MutableState<Boolean> = mutableStateOf(true),
    state:MutableState<RadioGroupState>,
    content: @Composable (( ) -> Unit)? = null
){
    val (selectedOption, onOptionSelected) = mutableStateOf(state.value.second[state.value.first.value])

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


        state.value.second.forEachIndexed { index, it ->
            Row(
                modifier = Modifier
                    .selectable(
                        selected = (it == selectedOption),
                        onClick = {
                            onOptionSelected( it )
                            state.value.first.value= index
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
            tagInfoDataList[it].isSet.value = true
        }
    }

    val  lazyStaggeredGridState = rememberLazyStaggeredGridState()
    val lazyGridState = rememberLazyGridState()
    val tagList = rememberSaveable {   tagInfoDataList }
    val itemModifier = Modifier.wrapContentSize()


    AnimatedVisibility(visible = isVisible) {


        Column (
            modifier = Modifier
                .then(modifier)
            //    .clip(RoundedCornerShape(6.dp))


        ){



/*
            LazyHorizontalGrid(
                rows = GridCells.Fixed(4),
                        modifier  = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                ,
                state = lazyGridState,
                contentPadding =  PaddingValues(10.dp),
                verticalArrangement = Arrangement.Center,
                userScrollEnabled = true,
            ){

                */
            LazyHorizontalStaggeredGrid(
                rows =  StaggeredGridCells.Fixed(4),
                modifier  = Modifier.padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .height(200.dp),
                state = lazyStaggeredGridState,
                contentPadding =  PaddingValues(10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalItemSpacing = 6.dp,
                userScrollEnabled = true,
            ){



                itemsIndexed(tagList) { index, it ->


                        AssistChip(
                            modifier = itemModifier,
                            onClick = {
                                it.isSet.value = !it.isSet.value

                                if (getState != null) {
                                    val selectedTagArray = arrayListOf<Int>()
                                    tagInfoDataList.forEachIndexed { index, tagInfoData ->
                                        if (tagInfoData.isSet.value) {
                                            selectedTagArray.add(index)
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
                                    if (it.isSet.value) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank
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


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchView(
    isSearchRefreshing:MutableState<Boolean> = mutableStateOf( false),
    sheetControl: (() -> Unit)? = null,
    onEvent: ((ListViewModel.Event) -> Unit)? = null,
    onMessage:(() -> Unit)? = null
){

    val isVisible:MutableState<Boolean> = mutableStateOf(true)

    val dateRangePickerState = rememberDateRangePickerState()

    val secretOption =  listOf("보안", "비보안", "모두")
    val secretRadioGroupState = rememberSaveable{
        mutableStateOf( RadioGroupState(mutableStateOf( secretOption.lastIndex) , secretOption))
    }

    val markerOption =  listOf("마커", "노마커", "모두")
    val markerRadioGroupState = rememberSaveable{
         mutableStateOf( RadioGroupState(mutableStateOf( markerOption.lastIndex) , markerOption))
    }

    var isTagBox by rememberSaveable{  mutableStateOf(true)}
    val isDateBox = rememberSaveable{  mutableStateOf(true)}

    val selectedTagArray:MutableState<ArrayList<Int>> = rememberSaveable{ mutableStateOf(arrayListOf())  }

    val query_title = rememberSaveable { mutableStateOf("") }

    val recognizerIntent = remember { recognizerIntent }

    val startLauncherRecognizerIntent = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {

        if (it.resultCode == Activity.RESULT_OK) {
            val result =  it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            query_title.value = query_title.value + result?.get(0).toString() + " "
        }
    }

    val aa =  mutableStateOf(secretOption.lastIndex)

    val initStateValue = {
        query_title.value = ""
        dateRangePickerState.setSelection(null, null)

        aa.value =secretOption.lastIndex
        secretRadioGroupState.value = RadioGroupState(mutableStateOf( secretOption.lastIndex) , secretOption)
        markerRadioGroupState.value = RadioGroupState(mutableStateOf( markerOption.lastIndex) , markerOption)


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

        if(secretRadioGroupState.value.first.value < secretRadioGroupState.value.second.lastIndex){
            queryDataList.add(
                QueryData(SearchOption.SECRET,
                    SearchQueryDataValue.radioGroupOption(
                        index = secretRadioGroupState.value.first.value
                    )
                )
            )
        }

        if(markerRadioGroupState.value.first.value < markerRadioGroupState.value.second.lastIndex){
            queryDataList.add(
                QueryData(SearchOption.MARKER,
                    SearchQueryDataValue.radioGroupOption(
                        index = markerRadioGroupState.value.first.value
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
            .fillMaxSize().verticalScroll(scrollState)
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
                placeholder = { Text("검색 할 제목을 입력 하세요") },
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
                state = secretRadioGroupState
            ) {
                Row(modifier = Modifier) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "secret"
                    )
                    Text(
                        modifier = Modifier,
                        text = " 보안조건 : "
                    )
                }
            }

            Divider(
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            )

            RadioButtonGroupView(
                state = markerRadioGroupState
            ) {
                Row(modifier = Modifier) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "marker"
                    )
                    Text(
                        modifier = Modifier,
                        text = "마커조건 : "
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
                onClick = { isTagBox = !isTagBox },
                content = {
                    Row(
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Class,
                            contentDescription = "tag"
                        )
                        Text("태그조건")
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
                onClick = { isDateBox.value = !isDateBox.value },
                content = {
                    Row(
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "date"
                        )
                        Text("검색기간")
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
                            text = "시작일-종료일",
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





typealias SearchQueryDataState =  Pair< List<SearchQueryData>, MutableState<SearchOption>>
typealias RadioGroupState = Pair<MutableState<Int>, List<String>>
typealias DateRangePickerDialogState = Pair<Long, Long>

typealias QueryData= Pair< SearchOption, SearchQueryDataValue>

