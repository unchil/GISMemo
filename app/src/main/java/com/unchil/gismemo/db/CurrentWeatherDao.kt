package com.unchil.gismemo.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrentWeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(it: CURRENTWEATHER_TBL)

    @Query("DELETE FROM CURRENTWEATHER_TBL")
    suspend fun delete()


    @Query("SELECT * FROM CURRENTWEATHER_TBL ORDER BY dt DESC LIMIT 1 ")
    fun select_Paging(): PagingSource<Int, CURRENTWEATHER_TBL>

    @Query("SELECT * FROM CURRENTWEATHER_TBL ORDER BY dt DESC LIMIT 1 ")
    fun select_Flow(): Flow<CURRENTWEATHER_TBL>
}


