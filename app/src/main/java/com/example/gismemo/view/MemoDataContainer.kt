package com.example.gismemo.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.size.Size
import com.example.gismemo.LocalUsableHaptic
import com.example.gismemo.data.RepositoryProvider
import com.example.gismemo.db.LocalLuckMemoDB
import com.example.gismemo.model.*
import com.example.gismemo.shared.utils.SnackBarChannelType
import com.example.gismemo.shared.utils.snackbarChannelList
import com.example.gismemo.viewmodel.MemoContainerViewModel
import com.example.gismemo.viewmodel.WriteMemoViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch


@Composable
fun MemoDataContainer(
    onEvent:((WriteMemoViewModel.Event)->Unit)? = null,
    deleteHandle:((index:Int)->Unit)? = null,
    channel: Channel<Int>? = null){

    val context = LocalContext.current
    val db = LocalLuckMemoDB.current
    val viewModel = remember {
        MemoContainerViewModel(
            repository = RepositoryProvider.getRepository().apply { database = db } ,
            user = if (onEvent == null ) MemoDataContainerUser.DetailMemoView else MemoDataContainerUser.WriteMemoView
        )
    }

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

    val currentTabView = remember {
        mutableStateOf(WriteMemoDataType.SNAPSHOT)
    }

    val currentTabIndex =  remember {
        mutableStateOf(0)
    }

    BottomNavigation(
        backgroundColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        contentColor =  androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
    ) {

        WriteMemoDataTypeList.forEachIndexed { index, it ->
            BottomNavigationItem(
                icon = {
                    Icon(
                        imageVector = it.getDesc().second,
                        contentDescription = context.resources.getString(   it.getDesc().first ) ,
                //        tint = if( currentTabView.value ==  it) Color.Red else Color.Black
                    )
                },
                label = { Text( context.resources.getString(   it.getDesc().first) ) },
                selected = currentTabView.value ==  it,
               // selectedContentColor =  if( currentTabView.value == it) Color.Red else Color.Black,
                onClick = {
                    hapticProcessing()
                    currentTabView.value = it
                    currentTabIndex.value = index
                },
            )
        }
    }

    Row(
        modifier = Modifier.padding(horizontal = 10.dp)
         //   .background(color = MaterialTheme.colorScheme.background)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        repeat(WriteMemoDataTypeList.count()) { iteration ->
            Box(
                modifier = Modifier
                    .height(2.dp)
                    .fillMaxWidth(1f / (WriteMemoDataTypeList.count() - iteration))
                    .clip(CircleShape)
                    .background(if (currentTabIndex.value == iteration) Color.Red else Color.Transparent))
        }
    }

    val memoData: MutableState<MemoData?> = mutableStateOf(
        when (currentTabView.value){
            WriteMemoDataType.PHOTO ->  MemoData.Photo(dataList = viewModel.phothoList.collectAsState().value.toMutableList())
            WriteMemoDataType.AUDIOTEXT -> MemoData.AudioText(dataList = viewModel.audioTextList.collectAsState().value.toMutableList())
            WriteMemoDataType.VIDEO ->  MemoData.Video(dataList = viewModel.videoList.collectAsState().value.toMutableList())
            WriteMemoDataType.SNAPSHOT ->  MemoData.SnapShot(dataList = viewModel.snapShotList.collectAsState().value.toMutableList())
        }
    )

    val onDelete:((page:Int) -> Unit)  =   { page ->
        if (currentTabView.value ==  WriteMemoDataType.SNAPSHOT ) {
            deleteHandle?.let {
                it (page)
            }
        }
        onEvent?.let {
            it(WriteMemoViewModel.Event.DeleteMemoItem(currentTabView.value, page))
        }
    }

    Column(modifier = Modifier
    ) {
        memoData.value?.let {
            when (it) {
                is MemoData.AudioText -> PagerAudioTextView(item = it, onDelete = if(deleteHandle != null) onDelete else null, channel = channel)
                is MemoData.Photo -> PagerPhotoView(item = it, onDelete = if(deleteHandle != null) onDelete else null, channel = channel)
                is MemoData.SnapShot ->  PagerSnapShotView(item = it, onDelete = if(deleteHandle != null) onDelete else null, channel = channel)
                is MemoData.Video -> PagerVideoView(item = it, onDelete = if(deleteHandle != null) onDelete else null, channel = channel)
            }
        }
    }

}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerSnapShotView(item: MemoData.SnapShot, onDelete:((page:Int) -> Unit)? = null, channel:Channel<Int>? = null){

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

    val pagerState  =   rememberPagerState(initialPage = 0)
    val defaultData:Pair<String, Int> =  Pair(
         context.resources.getString(WriteMemoDataType.SNAPSHOT.getDesc().first )
        , item.dataList.size)
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(2.dp)
    ) {

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {

            androidx.compose.material3.Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 10.dp),
                textAlign = TextAlign.Center,
                text = defaultData.first,
                style = MaterialTheme.typography.titleMedium
            )

            onDelete?.let {
                if (defaultData.second > 0) {
                    IconButton(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        onClick = {
                            hapticProcessing()
                            it(pagerState.currentPage)
                            channel?.let { channel ->
                                channel.trySend(snackbarChannelList.first { snackBarChannelData ->
                                    snackBarChannelData.channelType == SnackBarChannelType.ITEM_DELETE
                                }.channel)
                            }
                        },
                        content = {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete"
                            )
                        }
                    )
                }
            }

        }

        Row(
            modifier = Modifier.padding(vertical = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(defaultData.second) { iteration ->
                val color =if (pagerState.currentPage == iteration) Color.Red else MaterialTheme.colorScheme.onSurface
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(10.dp)
                )
            }
        }

        if (defaultData.second > 0) {
            HorizontalPager(
                modifier = Modifier .padding(10.dp)
                    .verticalScroll(state = scrollState)
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background),
                pageCount = defaultData.second,
                state = pagerState,
            ) { page ->
                 ImageViewer(
                    data = (item.dataList[page]),
                    size = Size.ORIGINAL,
                    isZoomable = false
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .background(color = Color.DarkGray)
            )
        }

    } // Column



}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerAudioTextView(item: MemoData.AudioText, onDelete:((page:Int) -> Unit)? = null, channel:Channel<Int>? = null){
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

    val pagerState  =   rememberPagerState(initialPage = 0)
    val defaultData:Pair<String, Int> = Pair(
        context.resources.getString(WriteMemoDataType.AUDIOTEXT.getDesc().first )
        , item.dataList.size)
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(2.dp)
    ) {

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {

            androidx.compose.material3.Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 10.dp),
                textAlign = TextAlign.Center,
                text = defaultData.first,
                style = MaterialTheme.typography.titleMedium
            )

            onDelete?.let {
                if (defaultData.second > 0) {
                    IconButton(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        onClick = {
                            hapticProcessing()
                            it(pagerState.currentPage)
                            channel?.let { channel ->
                                channel.trySend(snackbarChannelList.first { snackBarChannelData ->
                                    snackBarChannelData.channelType == SnackBarChannelType.ITEM_DELETE
                                }.channel)
                            }
                        },
                        content = {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete"
                            )
                        }
                    )
                }
            }

        }

        Row(
            modifier = Modifier.padding(vertical = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(defaultData.second) { iteration ->
                val color =   if (pagerState.currentPage == iteration) Color.Red else  MaterialTheme.colorScheme.onSurface
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(10.dp)
                )
            }
        }

        if (defaultData.second > 0) {
            HorizontalPager(
                modifier = Modifier.padding(10.dp)
                    .verticalScroll(state = scrollState)
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background),
                pageCount = defaultData.second,
                state = pagerState,
            ) { page ->

                 AudioTextView(data = item.dataList[page])

            }

            Box(
                modifier = Modifier
                    .height(220.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center

            ) {
                ExoplayerCompose(
                    uriList = item.dataList[pagerState.targetPage].second
                )
            }

        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .background(color = Color.DarkGray)
            )
        }

    } // Column



}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerPhotoView(item: MemoData.Photo, onDelete:((page:Int) -> Unit)? = null, channel:Channel<Int>? = null){
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

    val pagerState  =   rememberPagerState(initialPage = 0)

    val defaultData:Pair<String, Int> = Pair(
        context.resources.getString(WriteMemoDataType.PHOTO.getDesc().first )
        , item.dataList.size)

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(2.dp)
    ) {

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {

            androidx.compose.material3.Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 10.dp),
                textAlign = TextAlign.Center,
                text = defaultData.first,
                style = MaterialTheme.typography.titleMedium
            )

            onDelete?.let {
                if (defaultData.second > 0) {
                    IconButton(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        onClick = {
                            hapticProcessing()
                            it(pagerState.currentPage)
                            channel?.let { channel ->
                                channel.trySend(snackbarChannelList.first { snackBarChannelData ->
                                    snackBarChannelData.channelType == SnackBarChannelType.ITEM_DELETE
                                }.channel)
                            }
                        },
                        content = {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete"
                            )
                        }
                    )
                }
            }

        }

        Row(
            modifier = Modifier
                .padding(vertical = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(defaultData.second) { iteration ->
                val color =   if (pagerState.currentPage == iteration) Color.Red else  MaterialTheme.colorScheme.onSurface
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(10.dp)
                )
            }
        }


                if (defaultData.second > 0) {
                    HorizontalPager(
                        modifier = Modifier
                            .padding(10.dp)
                            .verticalScroll(state = scrollState)
                            .fillMaxSize()
                            .background(color = Color.White),
                        pageCount = defaultData.second,
                        state = pagerState,
                    ) { page ->
                        ImageViewer(
                            data = (item.dataList[page]),
                            size = Size.ORIGINAL,
                            isZoomable = false
                        )
                    }

                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                            .background(color = Color.DarkGray)
                    )
                }


    } // Column



}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerVideoView(item: MemoData.Video, onDelete:((page:Int) -> Unit)? = null, channel:Channel<Int>? = null){
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

    var videoTrackIndex by remember { mutableStateOf(0) }
    val defaultData:Pair<String, Int> = Pair(
        context.resources.getString(WriteMemoDataType.VIDEO.getDesc().first )
        , item.dataList.size)

    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(2.dp)
    ) {

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {

            androidx.compose.material3.Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(color = Color.Transparent)
                    .padding(top = 10.dp),
                textAlign = TextAlign.Center,
                text = defaultData.first,
                style = MaterialTheme.typography.titleMedium
            )

            onDelete?.let {
                if (defaultData.second > 0) {
                    IconButton(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        onClick = {
                            hapticProcessing()
                            it(videoTrackIndex)
                            channel?.let { channel ->
                                channel.trySend(snackbarChannelList.first { snackBarChannelData ->
                                    snackBarChannelData.channelType == SnackBarChannelType.ITEM_DELETE
                                }.channel)
                            }
                        },
                        content = {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete"
                            )
                        }
                    )
                }
            }

        }

        Row(
            modifier = Modifier
                .padding(vertical = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(defaultData.second) { iteration ->

                val color =   if (videoTrackIndex == iteration) Color.Red else  MaterialTheme.colorScheme.onSurface
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(10.dp)
                )
            }
        }


        // .verticalScroll(state = scrollState) 사용시 ExoplayerCompose minimum size 가 된다.

        val draggableState =  rememberDraggableState{}

        if (defaultData.second > 0) {

            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .draggable(
                        state = draggableState,
                        orientation = Orientation.Horizontal,
                        onDragStopped = {
                            videoTrackIndex = if (it >= 0F) {
                                if (videoTrackIndex - 1 > 0) --videoTrackIndex else 0
                            } else {
                                if (videoTrackIndex < defaultData.second - 1) ++videoTrackIndex else defaultData.second - 1
                            }
                        }
                    )
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                ExoplayerCompose(
                    uriList = item.dataList.toList(),
                    setTrackIndex = {exoPlayer ->
                        exoPlayer.seekTo(videoTrackIndex, 0)
                    }
                ) {
                    videoTrackIndex = it
                }
            }



        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .background(color = Color.DarkGray)
            )
        }


    } // Column



}

/*
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerMemoDataView(item: MemoData, onDelete:((page:Int) -> Unit)? = null, channel:Channel<Int>? = null){

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

    val pagerState  =   rememberPagerState(initialPage = 0)

    var videoTrackIndex by remember { mutableStateOf(0) }
    //   val audioUriList:MutableState<List<Uri>> = remember { mutableStateOf( emptyList()) }

    val defaultData:Pair<String, Int> = when(item){
        is MemoData.Photo ->  Pair(WriteMemoDataType.PHOTO.name, item.dataList.size)
        is MemoData.AudioText -> Pair(WriteMemoDataType.AUDIOTEXT.name, item.dataList.size)
        is MemoData.SnapShot -> Pair(WriteMemoDataType.SNAPSHOT.name, item.dataList.size)
        is MemoData.Video -> Pair(WriteMemoDataType.VIDEO.name, item.dataList.size)
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)
    ) {

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {

            androidx.compose.material3.Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(color = Color.Transparent)
                    .padding(top = 10.dp),
                textAlign = TextAlign.Center,
                text = defaultData.first,
                style = MaterialTheme.typography.h6
            )

            onDelete?.let {
                if (defaultData.second > 0) {
                    IconButton(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        onClick = {
                            hapticProcessing()
                            it(pagerState.currentPage)
                            channel?.let { channel ->
                                channel.trySend(snackbarChannelList.first { snackBarChannelData ->
                                    snackBarChannelData.channelType == SnackBarChannelType.ITEM_DELETE
                                }.channel)
                            }
                        },
                        content = {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete"
                            )
                        }
                    )
                }
            }

        }

        Row(
            modifier = Modifier
                .background(color = Color.White)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(defaultData.second) { iteration ->
                val color = when (item) {
                    is MemoData.Video -> {
                        if (videoTrackIndex == iteration) Color.DarkGray else Color.LightGray
                    }
                    else -> {
                        if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                        //       if (pageIndex == iteration) Color.DarkGray else Color.LightGray
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(10.dp)
                )
            }
        }


        // .verticalScroll(state = scrollState) 사용시 ExoplayerCompose minimum size 가 된다.

        val draggableState =  rememberDraggableState{}

        when (item) {
            is MemoData.Video -> {

                if (defaultData.second > 0) {

                    Box(
                        modifier = Modifier
                            .draggable(
                                state = draggableState,
                                orientation = Orientation.Horizontal,
                                onDragStopped = {
                                    videoTrackIndex = if (it >= 0F) {
                                        if (videoTrackIndex - 1 > 0) --videoTrackIndex else 0
                                    } else {
                                        if (videoTrackIndex < defaultData.second - 1) ++videoTrackIndex else defaultData.second - 1
                                    }
                                }
                            )
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        ExoplayerCompose(
                            uriList = item.dataList.toList(),
                            setTrackIndex = {exoPlayer ->
                                exoPlayer.seekTo(videoTrackIndex, 0)
                            }
                        ) {
                            videoTrackIndex = it
                        }
                    }



                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                            .background(color = Color.LightGray)
                    )
                }

            }
            else -> {
                if (defaultData.second > 0) {
                    HorizontalPager(
                        modifier = Modifier
                            .verticalScroll(state = scrollState)
                            .fillMaxSize()
                            .background(color = Color.White),
                        pageCount = defaultData.second,
                        state = pagerState,
                    ) { page ->
                        when (item) {
                            is MemoData.AudioText -> {
                                AudioTextView(data = item.dataList[page])
                            }
                            is MemoData.Photo -> ImageViewer(
                                data = (item.dataList[page]),
                                size = Size.ORIGINAL,
                                isZoomable = false
                            )
                            is MemoData.SnapShot -> ImageViewer(
                                data = (item.dataList[page]),
                                size = Size.ORIGINAL,
                                isZoomable = false
                            )
                            else -> {}
                        }
                    }

                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                            .background(color = Color.LightGray)
                    )
                }
            }
        }

/*
        when (item) {
            is MemoData.AudioText -> {
                if (defaultData.second > 0) {
                    Box(
                        modifier = Modifier
                            .height(280.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center

                    ) {
                        ExoplayerCompose(
                            uriList = item.dataList[pagerState.targetPage].second
                        )
                    }
                }
            }
            else -> {}
        }

 */




    } // Column



}

 */