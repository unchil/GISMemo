package com.example.gismemo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.gismemo.data.Repository
import com.example.gismemo.db.entity.MEMO_FILE_TBL
import com.example.gismemo.db.entity.MEMO_TAG_TBL
import com.example.gismemo.db.entity.MEMO_TBL
import com.example.gismemo.db.entity.MEMO_TEXT_TBL
import com.example.gismemo.view.QueryData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModel(val repository:Repository, ) : ViewModel() {



    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val memoListPaging :Flow<PagingData<MEMO_TBL>>

    val searchQueryFlow:Flow<Event.Search>

    val eventHandler: (Event) -> Unit

    init {
        val eventStateFlow = MutableSharedFlow<Event>()

        //  1. fun onEvent(event: Event)
        eventHandler = {
            viewModelScope.launch {
                eventStateFlow.emit(it)
            }
        }

        //  2.   when (event) { is Event.Search ->   }
        searchQueryFlow = eventStateFlow
            .filterIsInstance<Event.Search>()
            .distinctUntilChanged()
            .onStart {
                emit( Event.Search(queryDataList = mutableListOf()) )
            }

        //  3.  viewModelScope.launch { searchMemo() }
        memoListPaging = searchQueryFlow
            .flatMapLatest {
                searchMemo(queryDataList = it.queryDataList)
            }.cachedIn(viewModelScope)

    }

    private fun searchMemo(queryDataList:MutableList<QueryData>): Flow<PagingData<MEMO_TBL>> {
        _isRefreshing.value = true
        val result = repository.getMemoListStream(queryDataList)
        _isRefreshing.value = false
        return result
    }

    fun onEvent(event: Event) {
        when (event) {
            is Event.ToRoute -> {
                toRoute(event.navController, event.route)
            }
            is Event.DeleteItem -> deleteItem(event.id)
            is Event.SetFiles -> setFiles(event.id)
            is Event.SetMemo -> setMemo(event.id)

            else -> {}
        }
    }


    private fun setMemo(id:Long){
        viewModelScope.launch {
            repository.setMemo(id = id)
        }
    }

    private fun setFiles(id:Long){
        viewModelScope.launch {
            repository.setFiles(id = id)
        }
    }

    private fun deleteItem(id:Long){
        viewModelScope.launch {
            repository.deleteMemo(id= id)

        }
    }

    private fun toRoute(navController: NavController, route:String){
        navController.navigate(route = route)
    }




    sealed class Event {
        data class ToRoute(val navController: NavController, val route:String) : Event()
        data class DeleteItem(val id:Long):Event()
        data class  SetMemo(val id: Long): Event()
        data class  SetFiles(val id: Long): Event()

//mutableListOf<QueryData>()
        data class Search(val queryDataList:MutableList<QueryData>) : Event()

    }








}

