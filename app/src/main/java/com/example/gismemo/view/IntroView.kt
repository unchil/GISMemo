package com.example.gismemo.view

import android.Manifest
import android.content.res.Configuration
import androidx.biometric.BiometricManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.size.Size
import com.example.gismemo.LocalUsableHaptic
import com.example.gismemo.data.RepositoryProvider
import com.example.gismemo.db.LocalLuckMemoDB
import com.example.gismemo.db.entity.MEMO_TBL
import com.example.gismemo.model.BiometricCheckType
import com.example.gismemo.model.ListItemBackgroundAction
import com.example.gismemo.model.getDesc
import com.example.gismemo.navigation.GisMemoDestinations
import com.example.gismemo.shared.composables.biometricPrompt
import com.example.gismemo.shared.launchIntent_Biometric_Enroll
import com.example.gismemo.shared.launchIntent_ShareMemo
import com.example.gismemo.shared.utils.SnackBarChannelType
import com.example.gismemo.shared.utils.snackbarChannelList
import com.example.gismemo.ui.theme.GISMemoTheme
import com.example.gismemo.viewmodel.ListViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

val SwipeBoxHeight = 70.dp

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun IntroView(
    navController: NavHostController
) {

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val db = LocalLuckMemoDB.current
    val coroutineScope = rememberCoroutineScope()

    val viewModel = remember {
        ListViewModel(repository = RepositoryProvider.getRepository().apply { database = db }  )
    }


    val memoListStream = viewModel.memoListPaging.collectAsLazyPagingItems()
    val isRefreshing = viewModel.isRefreshing.collectAsState()
    val isSearchRefreshing: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val sheetState = SheetState(
        skipPartiallyExpanded = false,
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true
    )

    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
    val snackBarHostState = remember { SnackbarHostState() }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing.value,
        onRefresh = {
            viewModel.eventHandler(
                ListViewModel.Event.Search(queryDataList = mutableListOf())
            )
            isSearchRefreshing.value = true
        }
    )

    val channel = remember { Channel<Int>(Channel.CONFLATED) }

    val lazyListState = rememberLazyListState()

    var isPortrait by remember { mutableStateOf(false) }

    var gridWidth by remember { mutableStateOf(1f) }

    val upButtonPaddingValue: Dp
    val sheetPeekHeightValue: Dp
    val drawerSheetWidthValue: Float
    val listBottomPaddingValue:Dp

    when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> {
            isPortrait = true
            gridWidth = 1f
            upButtonPaddingValue = 160.dp
            sheetPeekHeightValue = 140.dp
            drawerSheetWidthValue = 0f
            listBottomPaddingValue = 130.dp

            if (drawerState.isOpen) {
                coroutineScope.launch {
                    drawerState.close()
                }
            }

        }
        else -> {
            isPortrait = false
            gridWidth = 0.8f
            upButtonPaddingValue = 10.dp
            sheetPeekHeightValue = 0.dp
            drawerSheetWidthValue = 0.5f
            listBottomPaddingValue = 10.dp
        }
    }

    val isUsableHaptic = LocalUsableHaptic.current
    val hapticFeedback = LocalHapticFeedback.current

    fun hapticProcessing(){
        if(isUsableHaptic){
            coroutineScope.launch {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }


    LaunchedEffect(channel) {
        channel.receiveAsFlow().collect { index ->
            val channelData = snackbarChannelList.first {
                it.channel == index
            }

            //----------
            val message = when (channelData.channelType) {
                SnackBarChannelType.SEARCH_RESULT -> {

                    context.resources.getString( channelData.message) + "[${memoListStream.itemCount}]"
                }
                else -> {
                    context.resources.getString( channelData.message)
                }
            }
            //----------

            val result = snackBarHostState.showSnackbar(
                message =   message,
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

    LaunchedEffect(key1 = memoListStream.loadState.source.refresh,) {
        if (!isRefreshing.value) {
            when (memoListStream.loadState.source.refresh) {
                is LoadState.Error -> {}
                LoadState.Loading -> {}
                is LoadState.NotLoading -> {

                    if (isSearchRefreshing.value) {
                        channel.trySend(snackbarChannelList.first {
                            it.channelType == SnackBarChannelType.SEARCH_RESULT
                        }.channel)
                        isSearchRefreshing.value = false
                    }

                }
            }
        }
    }


    val searchView: (@Composable () -> Unit) = {
        val sheetControl: (() -> Unit)? =  if (isPortrait) {
            null
        } else {
            { if (drawerState.isOpen) { coroutineScope.launch {  drawerState.close() }  } }
        }
        SearchView(
            isSearchRefreshing = isSearchRefreshing,
            sheetControl = sheetControl,
            onEvent = viewModel.eventHandler
        ) {
            channel.trySend(snackbarChannelList.first {
                it.channelType == SnackBarChannelType.SEARCH_CLEAR
            }.channel)
        }
    }



    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            if (!isPortrait) {
                ModalDrawerSheet(
                    modifier = Modifier.fillMaxWidth(drawerSheetWidthValue),
                    drawerShape = ShapeDefaults.ExtraSmall
                ) {
                    searchView()
                }
            }
        },
        content = {
            BottomSheetScaffold(
                modifier = Modifier.fillMaxSize().statusBarsPadding(),
                scaffoldState = scaffoldState,
                snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
                sheetPeekHeight = sheetPeekHeightValue,
                sheetDragHandle = {
                    Box(
                        modifier = Modifier.height(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.scale(1f),
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "search",
                        )
                    }
                },
                sheetContent = {
                    if (isPortrait) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            searchView()
                        }
                    }
                }
            ) { innerPadding ->

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                        verticalAlignment = Alignment.CenterVertically

                    ) {

                        if (!isPortrait) {
                            Box(modifier = Modifier.fillMaxWidth(0.4f)) {
                                WeatherContent{
                                    if(!it){
                                        channel.trySend(snackbarChannelList.first {
                                            it.channelType == SnackBarChannelType.LOCATION_SERVICE_DISABLE
                                        }.channel)
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pullRefresh(state = pullRefreshState)
                        ) {

                            LazyColumn(
                                modifier = Modifier.padding(bottom = listBottomPaddingValue),
                                state = lazyListState,
                                userScrollEnabled = true,
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                contentPadding = PaddingValues(
                                    horizontal = 6.dp,
                                    vertical = 10.dp
                                )
                            ) {

                                if (isPortrait) {
                                    stickyHeader {
                                        WeatherContent{
                                            if(!it){
                                                channel.trySend(snackbarChannelList.first {
                                                    it.channelType == SnackBarChannelType.LOCATION_SERVICE_DISABLE
                                                }.channel)
                                            }
                                        }
                                    }
                                }

                                items(memoListStream.itemCount) {
                                    memoListStream[it]?.let { memo ->
                                        MemoSwipeView(
                                            item = memo,
                                            channel = channel,
                                            event = viewModel::onEvent,
                                            navController = navController
                                        )
                                    }
                                }

                            }

                            PullRefreshIndicator(
                                refreshing = isRefreshing.value,
                                state = pullRefreshState,
                                modifier = Modifier.align(Alignment.TopCenter)
                            )

                            SearchingProgressIndicator(isVisibility = isSearchRefreshing.value)

                            UpButton(
                                modifier = Modifier
                                    .padding(end = 10.dp, bottom = upButtonPaddingValue)
                                    .align(Alignment.BottomEnd),
                                listState = lazyListState
                            )

                        }
                    }
                }
            }
        } // content
    )


}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class,)
@Composable
fun MemoSwipeView(
    item: MEMO_TBL,
    channel:  Channel<Int>? = null,
    event: ((ListViewModel.Event)->Unit)? = null,
    navController: NavController? = null
){

    val context = LocalContext.current
    val db = LocalLuckMemoDB.current


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




    val permissions = listOf(
        Manifest.permission.USE_BIOMETRIC,
    )
    val multiplePermissionsState = rememberMultiplePermissionsState( permissions)


    var isGranted by mutableStateOf(true)
    permissions.forEach { chkPermission ->
        isGranted =  isGranted && multiplePermissionsState.permissions.find { it.permission == chkPermission }?.status?.isGranted
            ?: false
    }







    val onResult: (isSucceeded:Boolean, bioMetricCheckType: BiometricCheckType, errorMsg:String? ) -> Unit =
        { result, bioMetricCheckType, msg ->
            if(result){
                when(bioMetricCheckType){
                    BiometricCheckType.DETAILVIEW -> {
                        navController?.let {
                            if (event != null) {
                                event(
                                    ListViewModel.Event.ToRoute(
                                        navController = it,
                                        route = GisMemoDestinations.DetailMemoView.createRoute(id = item.id.toString())
                                    )
                                )
                            }
                        }
                    }
                    BiometricCheckType.SHARE -> {
                        coroutineScope.launch {
                            launchIntent_ShareMemo(context = context, memo = item, db = db )
                        }
                    }
                    BiometricCheckType.DELETE -> {
                        if (event != null) {
                            event(  ListViewModel.Event.DeleteItem(id = item.id) )
                        }
                        channel?.let {channel ->
                            channel.trySend(snackbarChannelList.first {
                                it.channelType == SnackBarChannelType.MEMO_DELETE
                            }.channel)
                        }
                    }
                }
            }else {
                channel?.let {channel ->
                    channel.trySend(
                        snackbarChannelList.first {
                            it.channelType == SnackBarChannelType.AUTHENTICATION_FAILED
                        }.channel
                    )
                }

            }
        }




    val ANCHOR_OFFSET = 80
    val isAnchor = remember { mutableStateOf(false) }
    val isToStart = remember { mutableStateOf(false) }

    val dismissState = rememberDismissState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                DismissValue.Default -> {
                    isAnchor.value = false
                    false
                }
                DismissValue.DismissedToEnd -> {
                    isToStart.value = false
                    isAnchor.value = !isAnchor.value
                    false
                }
                DismissValue.DismissedToStart -> {
                    isToStart.value = true
                    isAnchor.value = !isAnchor.value
                    false
                }
            }
        }
    )

    val dismissContentOffset = if ( isAnchor.value )  {
        if(isToStart.value) -ANCHOR_OFFSET.dp else  ANCHOR_OFFSET.dp
    } else {  0.dp  }

    val checkBiometricSupport: (() -> Unit) = {
        val biometricManager = BiometricManager.from(context)
        when (  biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
                    or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        ) {
            BiometricManager.BIOMETRIC_SUCCESS -> { }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                launchIntent_Biometric_Enroll(context)
            }
            else -> {
                channel?.trySend(snackbarChannelList.first {
                    it.channelType == SnackBarChannelType.BIOMETRIC_NO_SUCCESS
                }.channel)
            }
        }

    }

    Card(
        modifier = Modifier
            .height(260.dp)
            .padding(top = 2.dp) ,
        shape = ShapeDefaults.ExtraSmall ,
        onClick = {
            hapticProcessing()
            if(item.isSecret && isGranted ) {
                checkBiometricSupport.invoke()
                biometricPrompt(context, BiometricCheckType.DETAILVIEW, onResult)
            }else {
                navController?.let {
                    if (event != null) {
                        event(
                            ListViewModel.Event.ToRoute(
                                navController = it,
                                route = GisMemoDestinations.DetailMemoView.createRoute(id = item.id.toString())
                            )
                        )
                    }
                }
            }

        }

    ) {

        SwipeToDismiss(
            modifier = Modifier
            ,
            state = dismissState,
            background = {
                BackgroundContent(dismissState) {
                    when(it){
                        ListItemBackgroundAction.SHARE -> {

                            if(item.isSecret && isGranted ) {
                                checkBiometricSupport.invoke()
                                biometricPrompt(context, BiometricCheckType.SHARE, onResult)
                            }else {
                                coroutineScope.launch {
                                    launchIntent_ShareMemo(context = context, memo = item, db = db )
                                }
                            }

                        }
                        ListItemBackgroundAction.DELETE -> {

                            if(item.isSecret && isGranted ) {
                                checkBiometricSupport.invoke()
                                biometricPrompt(context, BiometricCheckType.DELETE, onResult)
                            }else {
                                if (event != null) {
                                    event(  ListViewModel.Event.DeleteItem(id = item.id) )
                                }
                                channel?.let {channel ->
                                    channel.trySend(snackbarChannelList.first {
                                        it.channelType == SnackBarChannelType.MEMO_DELETE
                                    }.channel)
                                }
                            }
                        }
                    }

                    isAnchor.value = false
                }
            },
            dismissContent = {

                Box(

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SwipeBoxHeight)
                        .offset(x = dismissContentOffset)
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.inverseOnSurface)
                    ,

                    contentAlignment = Alignment.Center
                ) {

                    Row(
                        modifier = Modifier
                            .clickable(false,null,null){}
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {

                        Icon(
                            modifier = Modifier.scale(1f),
                            imageVector = if (item.isSecret) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                            contentDescription = "Lock",
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.9f),
                            verticalArrangement = Arrangement.SpaceAround,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                               text =  item.title,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                minLines =  1,
                            )
                            Text(
                                text =   item.desc,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                minLines =  1,
                            )
                            Text(
                                text =   item.snippets,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                minLines =  1,
                            )
                        }

                        Icon(
                            modifier = Modifier.scale(1f),
                            imageVector = if (item.isPin) Icons.Outlined.LocationOn else Icons.Outlined.LocationOff,
                            contentDescription = "Mark",
                        )


                    }
                }

            }
        )

        ImageViewer(data = item.snapshot.toUri() , size = Size.ORIGINAL, isZoomable = false)

    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackgroundContent(
    dismissState:DismissState,
    onClick:(ListItemBackgroundAction)->Unit
){

    val color by animateColorAsState(
        when (dismissState.targetValue) {
            DismissValue.Default -> SnackbarDefaults.backgroundColor.copy(alpha = 0.0f)
            DismissValue.DismissedToEnd -> Color.Green.copy(alpha = 0.4f)
            DismissValue.DismissedToStart -> Color.Red.copy(alpha = 0.5f)
        }
    )
    val scale by animateFloatAsState(
        when (dismissState.targetValue == DismissValue.Default) {
            true -> 1f
            else -> 1.5f
        }
    )



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



    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(SwipeBoxHeight)
            .background(color)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Box(
            modifier = Modifier,
            contentAlignment = Alignment.CenterStart
        ) {

            Row {

                IconButton(
                    onClick = {
                        hapticProcessing()
                        onClick(ListItemBackgroundAction.SHARE)
                    }
                ) {
                    Icon(
                        modifier = Modifier.scale(scale),
                        imageVector = ListItemBackgroundAction.SHARE.getDesc().second,
                        contentDescription = ListItemBackgroundAction.SHARE.getDesc().first
                    )
                }
            }
        }


        Box(
            modifier = Modifier,
            contentAlignment = Alignment.CenterEnd
        ) {

            Row {
                IconButton(
                    onClick = {
                        hapticProcessing()
                        //  isAnchor.value = false
                        onClick(ListItemBackgroundAction.DELETE)
                    }
                ) {
                    Icon(
                        modifier = Modifier.scale(scale),
                        imageVector = ListItemBackgroundAction.DELETE.getDesc().second,
                        contentDescription = ListItemBackgroundAction.DELETE.getDesc().first
                    )
                }
            }
        }

    }

}


@Composable
fun SearchingProgressIndicator(
    isVisibility:Boolean
){
    if(isVisibility) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(
           //     color = Color.Gray,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}


@Composable
fun UpButton(
    modifier:Modifier,
    listState: LazyListState
){


    val showButton by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
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

    if( showButton) {
        FloatingActionButton(
            modifier = Modifier.then(modifier),
      //      interactionSource =  interactionSource,
            onClick = {
                coroutineScope.launch {
                    listState.animateScrollToItem(0)
                    hapticProcessing()
                }
            }
        ) {
            Icon(
                modifier = Modifier.scale(1f),
                imageVector = Icons.Outlined.Publish,
                contentDescription = "Up",

            )

        }
    }
}



@Preview
@Composable
fun PrevIntroView(){

    val navController = rememberNavController()

    GISMemoTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.onPrimary,
            contentColor = MaterialTheme.colors.primary
        ) {
            IntroView(navController)
        }
    }
}