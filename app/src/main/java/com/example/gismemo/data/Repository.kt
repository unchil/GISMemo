package com.example.gismemo.data

import android.net.Uri
import android.provider.ContactsContract.SearchSnippets
import androidx.core.net.toUri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.example.gismemo.BuildConfig
import com.example.gismemo.api.OpenWeatherInterface
import com.example.gismemo.api.RetrofitAdapter
import com.example.gismemo.db.CURRENTWEATHER_TBL
import com.example.gismemo.db.LuckMemoDB
import com.example.gismemo.db.entity.*
import com.example.gismemo.model.WriteMemoDataType
import com.example.gismemo.model.WriteMemoDataTypeList
import com.example.gismemo.model.toCURRENTWEATHER_TBL
import com.example.gismemo.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class Repository{


    lateinit var database:LuckMemoDB


    val currentSelectedTab:MutableStateFlow<WriteMemoDataType?>
        = MutableStateFlow(null)

    val selectedMemo:MutableStateFlow<MEMO_TBL?> = MutableStateFlow(null)

    val selectedWeather:MutableStateFlow<MEMO_WEATHER_TBL?> = MutableStateFlow(null)

    val isChangeLocale: MutableStateFlow<Int>
            = MutableStateFlow(0)

    val onChangeLocale: MutableStateFlow<Boolean>
            = MutableStateFlow(false)

    val isFirstSetup:MutableStateFlow<Boolean>
            = MutableStateFlow(true)

    val isUsableHaptic: MutableStateFlow<Boolean>
            = MutableStateFlow(true)

    val isUsableDarkMode: MutableStateFlow<Boolean>
            = MutableStateFlow(false)

    val currentIsDrawing: MutableStateFlow<Boolean>
            = MutableStateFlow(false)

    val currentIsEraser: MutableStateFlow<Boolean>
            = MutableStateFlow(false)

    var selectedTagList: MutableStateFlow<ArrayList<Int>>
        = MutableStateFlow(arrayListOf())

    val currentIsLock: MutableStateFlow<Boolean>
            = MutableStateFlow(false)

    val currentIsMarker: MutableStateFlow<Boolean>
        = MutableStateFlow(false)


    val currentPolylineList:MutableStateFlow<List<DrawingPolyline>>
        = MutableStateFlow(listOf())

    fun clearCurrentValue(){
        selectedTagList.value = arrayListOf()

        currentIsLock.value = false
        currentIsMarker.value = false
        currentIsDrawing.value = false
        currentIsEraser.value = false
        currentPolylineList.value = listOf()
        currentSnapShot.value = listOf()
    }

    fun updateIsUsableHaptic(value:Boolean){
        isUsableHaptic.value = value
    }

    fun updateIsUsableDarkMode(value:Boolean){
        isUsableDarkMode.value = value
    }

    fun updateOnChangeLocale(value:Boolean){
        onChangeLocale.value = value
    }

    fun updateIsChangeLocale(value:Int){
        isChangeLocale.value = value
    }


    fun updateCurrentIsDrawing(isDrawing:Boolean){
        currentIsDrawing.value = isDrawing
    }


    fun updateCurrentIsEraser(isEraser:Boolean){
        currentIsEraser.value = isEraser
    }


    fun updateCurrentTags(tagArrayList:ArrayList<Int>){
        selectedTagList.value = tagArrayList
    }

    fun updateCurrentIsLock(isLock:Boolean){
        currentIsLock.value = isLock
    }

    fun updateCurrentIsMarker(isMarker:Boolean){
        currentIsMarker.value = isMarker
    }



    fun updateCurrentPolylineList(polylineList:List<DrawingPolyline>){
        currentPolylineList.value = polylineList
    }

//------
    val currentAudioText: MutableStateFlow<List<Pair<String, List<Uri>>>>
            = MutableStateFlow( listOf())

    val currentPhoto:  MutableStateFlow<List<Uri>>
    = MutableStateFlow( listOf())

    val currentVideo: MutableStateFlow<List<Uri>>
            = MutableStateFlow( listOf())

    val currentSnapShot: MutableStateFlow<List<Uri>>
            = MutableStateFlow( listOf())

    val detailAudioText: MutableStateFlow<List<Pair<String, List<Uri>>>>
            = MutableStateFlow( listOf())

    val detailPhoto:  MutableStateFlow<List<Uri>>
            = MutableStateFlow( listOf())

    val detailVideo: MutableStateFlow<List<Uri>>
            = MutableStateFlow( listOf())

    val detailSnapShot: MutableStateFlow<List<Uri>>
            = MutableStateFlow( listOf())



//------

    val OPENWEATHER_URL = "https://api.openweathermap.org/data/2.5/"

    val _currentWeather:MutableStateFlow<CURRENTWEATHER_TBL?>  = MutableStateFlow(null)

    suspend fun deleteMemoItem( type:WriteMemoDataType,  index:Int) {
        when(type){
            WriteMemoDataType.PHOTO -> {
                val newMemoItem = currentPhoto.value.toMutableList()
                newMemoItem.removeAt(index)
                currentPhoto.emit(newMemoItem)
            }
            WriteMemoDataType.AUDIOTEXT -> {
                val newMemoItem = currentAudioText.value.toMutableList()
                newMemoItem.removeAt(index)
                currentAudioText.emit(newMemoItem)
            }
            WriteMemoDataType.VIDEO -> {
                val newMemoItem = currentVideo.value.toMutableList()
                newMemoItem.removeAt(index)
                currentVideo.emit(newMemoItem)
            }
            WriteMemoDataType.SNAPSHOT -> {
                val newMemoItem = currentSnapShot.value.toMutableList()
                newMemoItem.removeAt(index)
                currentSnapShot.emit(newMemoItem)
            }
        }
    }



    suspend fun initMemoItem(){

        WriteMemoDataTypeList.forEach {

            when(it){
                WriteMemoDataType.PHOTO -> {
                    val newMemoItem = currentPhoto.value.toMutableList()
                    newMemoItem.clear()
                    currentPhoto.emit(newMemoItem)
                }

                WriteMemoDataType.AUDIOTEXT -> {
                    val newMemoItem = currentAudioText.value.toMutableList()
                    newMemoItem.clear()
                    currentAudioText.emit(newMemoItem)
                }
                WriteMemoDataType.VIDEO -> {
                    val newMemoItem = currentVideo.value.toMutableList()
                    newMemoItem.clear()
                    currentVideo.emit(newMemoItem)
                }
                WriteMemoDataType.SNAPSHOT -> {
                    val newMemoItem = currentSnapShot.value.toMutableList()
                    newMemoItem.clear()
                    currentSnapShot.emit(newMemoItem)
                }

            }
        }

    }


    suspend fun insertMemo (
        id:Long,
        isLock:Boolean,
        isMark:Boolean,
        selectTagArrayList:ArrayList<Int>,
        title:String,
        desc:String,
        snippets: String,
        location: CURRENTLOCATION_TBL
    )  {


        val memoFileTblList = mutableListOf<MEMO_FILE_TBL>()
        val memoTextTblList = mutableListOf<MEMO_TEXT_TBL>()
/*
        val desc =
            "screenshot:${currentSnapShot.value.size} audioText:${currentAudioText.value.size} photo:${currentPhoto.value.size} video:${currentVideo.value.size}"

 */

        val snapshot = currentSnapShot.value.first().encodedPath ?: ""
        val memoTbl = MEMO_TBL(
            id = id,
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude,
            isSecret = isLock,
            isPin = isMark,
            title = title,
            snippets = snippets,
            snapshotCnt = currentSnapShot.value.size,
            textCnt = currentAudioText.value.size,
            photoCnt = currentPhoto.value.size,
            videoCnt = currentVideo.value.size,
            desc = desc,
            snapshot = snapshot
        )


        database.memoDao.insert(memoTbl)

        currentSnapShot.value.forEachIndexed { index, uri ->
            memoFileTblList.add(
                MEMO_FILE_TBL(
                    id = id,
                    type = WriteMemoDataType.SNAPSHOT.name,
                    index = index,
                    subIndex = 0,
                    filePath = uri.encodedPath ?: ""
                )
            )
        }

        currentPhoto.value.forEachIndexed { index, uri ->
            memoFileTblList.add(
                MEMO_FILE_TBL(
                    id = id,
                    type = WriteMemoDataType.PHOTO.name,
                    index = index,
                    subIndex = 0,
                    filePath = uri.encodedPath ?: ""
                )
            )
        }

        currentVideo.value.forEachIndexed { index, uri ->
            memoFileTblList.add(
                MEMO_FILE_TBL(
                    id = id,
                    type = WriteMemoDataType.VIDEO.name,
                    index = index,
                    subIndex = 0,
                    filePath = uri.encodedPath ?: ""
                )
            )
        }


        currentAudioText.value.forEachIndexed { index, pairData ->
            memoTextTblList.add(
                MEMO_TEXT_TBL(
                    id = id,
                    index = index,
                    comment = pairData.first
                )
            )

            pairData.second.forEachIndexed { subIndex, uri ->
                memoFileTblList.add(
                    MEMO_FILE_TBL(
                        id = id,
                        type = WriteMemoDataType.AUDIOTEXT.name,
                        index = index,
                        subIndex = subIndex,
                        filePath = uri.encodedPath ?: ""
                    )
                )
            }
        }


        val memoTagTblList = mutableListOf(MEMO_TAG_TBL(id = id, index = 10000))
        selectTagArrayList.forEach {
            memoTagTblList.add(MEMO_TAG_TBL(id = id, index = it))
        }

         database.memoTagDao.insert(memoTagTblList)
         database.memoFileDao.insert(memoFileTblList)
         database.memoTextDao.insert(memoTextTblList)
        _currentWeather.value?.toMEMO_WEATHER_TBL(id)?.let {
            database.memoWeatherDao.insert(it)
        }



        initMemoItem()
    }


    suspend fun deleteMemo(id:Long){
        database.withTransaction {
            database.memoDao.delete(id)
            database.memoFileDao.delete(id)
            database.memoTextDao.delete(id)
            database.memoTagDao.delete(id)
            database.memoWeatherDao.delete(id)
        }
    }

    suspend fun deleteAllMemo(){
        database.memoDao.trancate()
    }


    val _markerMemoList:MutableStateFlow<List<MEMO_TBL>>  = MutableStateFlow(listOf())

    suspend fun setMarkerMemoList() {

        database.memoDao.select_Marker_Flow().collectLatest {
            _markerMemoList.emit(it)
        }

    }


      fun getShareMemoData(id:Long, completeHandle:(attachments:ArrayList<Uri>, comments:ArrayList<String>)->Unit )
        = CoroutineScope(Dispatchers.IO).launch {
            val attachments = arrayListOf<Uri>()
            val comments = arrayListOf<String>()

          database.memoFileDao.select(id).forEach {
              attachments.add(it.filePath.toUri())
          }
          database.memoTextDao.select(id).forEach {
              comments.add(it.comment)
          }
            completeHandle(attachments, comments)
        }



     fun setMemo(id:Long){
        CoroutineScope(Dispatchers.IO).launch {
            selectedMemo.value = database.memoDao.select(id)
        }
    }


     fun setTags(id:Long){
        CoroutineScope(Dispatchers.IO).launch {
            val tagArrayList = arrayListOf<Int>()
            database.memoTagDao.select(id).forEach {
                tagArrayList.add(it.index)
            }
            selectedTagList.value = tagArrayList
        }
    }



    suspend fun setWeather(id:Long){
        database.memoWeatherDao.select_Flow(id).collectLatest {
           selectedWeather.value = it
        }
    }

    suspend fun setFiles(id:Long){


        database.memoFileDao.select_Flow(id).collectLatest {
            val currentSnapShotList = it.filter { it.type ==  WriteMemoDataType.SNAPSHOT.name}.map { it.filePath.toUri() }.sorted()
            detailSnapShot.emit( currentSnapShotList )

            val currentPhotoList = it.filter { it.type ==  WriteMemoDataType.PHOTO.name}.map { it.filePath.toUri() }.sorted()
            detailPhoto.emit(  currentPhotoList )

            val currentVideoList = it.filter { it.type ==  WriteMemoDataType.VIDEO.name}.map { it.filePath.toUri() }.sorted()
            detailVideo.emit(  currentVideoList  )

            database.memoTextDao.select_Flow(id).collectLatest {memoTextTblList ->

                val audiTextList = mutableListOf<Pair<String,List<Uri>>>()
                val audioTextFileList = it.filter { it.type ==  WriteMemoDataType.AUDIOTEXT.name}

                memoTextTblList.forEach {commentList ->
                    audiTextList.add(
                        Pair(
                            commentList.comment,
                            audioTextFileList.filter {
                                it.index == commentList.index
                            }.map {
                                it.filePath.toUri()
                            }.sorted()
                        )
                    )
                }
                detailAudioText.value = audiTextList
            }

        }


    }



    suspend fun updateTagList(id:Long, selectTagList: ArrayList< Int>){

        var snippets = ""

        selectTagList.forEach {
            snippets = "${snippets} #${tagInfoDataList[it].name}"
        }



       val memoTagTblList = mutableListOf(MEMO_TAG_TBL(id = id, index = 10000))
       selectTagList.forEach {
           memoTagTblList.add(MEMO_TAG_TBL(id = id, index = it))
       }



        database.withTransaction {

            database.memoDao.update_Snippets(id, snippets)
            database.memoTagDao.delete(id)
            database.memoTagDao.insert(memoTagTblList)

        }
    }


    suspend fun updateMark(id:Long, isMark:Boolean){
        database.memoDao.update_Marker(id, isMark)
    }

    suspend fun updateSecret(id:Long, isSecret:Boolean){
        database.memoDao.update_Secret(id, isSecret)
    }



    suspend fun getWeatherData(latitude: String, longitude: String){

        val OPENWEATHER_KEY = BuildConfig.OPENWEATHER_KEY
        val OPENWEATHER_UNITS = "metric"

        val service = RetrofitAdapter.create( service = OpenWeatherInterface::class.java, url = OPENWEATHER_URL)

        val apiResponse = service.getWeatherData(
            latitude = latitude,
            longitude = longitude,
            units = OPENWEATHER_UNITS,
            apiKey = OPENWEATHER_KEY
        )

        database.withTransaction {
            database.currentWeatherDao.delete()
            database.currentWeatherDao.insert(apiResponse.toCURRENTWEATHER_TBL())
        }

        _currentWeather.emit(apiResponse.toCURRENTWEATHER_TBL())
    }



     fun getMemoListPaging(): Flow<PagingData<MEMO_TBL>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = false  ),
            pagingSourceFactory = {   database.memoDao.select_All_Paging() }
        ).flow
    }


    fun getMemoListStream(queryDataList:MutableList<QueryData>): Flow<PagingData<MEMO_TBL>> {

       return  if( queryDataList.isEmpty()) {
            getMemoListPaging()
        } else {
            var tagArray = arrayListOf(10000)
           var secretArray  = arrayListOf(0,1)
           var markerArray = arrayListOf(0,1)
           var title = "% %"
           var fromDate = 0L
           var toDate = System.currentTimeMillis()

           queryDataList.forEachIndexed { index, pair ->
               when(pair.first){
                   SearchOption.TITLE -> {
                       title = if((pair.second as SearchQueryDataValue.titleOption).title.isNotEmpty()) {
                            "%" +  (pair.second as SearchQueryDataValue.titleOption).title.replace(
                               ' ','%' )  + "%"
                       }else {
                           "% %"
                       }
                   }
                   SearchOption.SECRET -> {

                       if ((pair.second as SearchQueryDataValue.radioGroupOption).index < secretArray.size) {
                           secretArray = if ((pair.second as SearchQueryDataValue.radioGroupOption).index == 0) {
                               arrayListOf(1)
                           }else{
                               arrayListOf(0)
                           }
                       }
                   }
                   SearchOption.MARKER -> {
                       if ((pair.second as SearchQueryDataValue.radioGroupOption).index < markerArray.size) {
                           markerArray = if ((pair.second as SearchQueryDataValue.radioGroupOption).index == 0) {
                               arrayListOf(1)
                           }else{
                               arrayListOf(0)
                           }
                       }
                   }
                   SearchOption.TAG -> {
                       tagArray = (pair.second as SearchQueryDataValue.tagOption).indexList
                   }
                   SearchOption.DATE -> {
                       fromDate = (pair.second as SearchQueryDataValue.dateOption).fromToDate.first
                       toDate =  (pair.second as SearchQueryDataValue.dateOption).fromToDate.second
                   }
               }
           }

            Pager(
               config = PagingConfig(
                   pageSize = 30,
                   enablePlaceholders = false  ),
               pagingSourceFactory = {
                   database.memoDao.select_Search_Paging(
                       tagArray,
                       fromDate,
                       toDate,
                       secretArray,
                       markerArray,
                       title
                   )
               }
           ).flow
        }

    }




}




