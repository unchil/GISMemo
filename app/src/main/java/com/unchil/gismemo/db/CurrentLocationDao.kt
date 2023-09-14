package com.unchil.gismemo.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.unchil.gismemo.db.entity.CURRENTLOCATION_TBL
import kotlinx.coroutines.flow.Flow


@Dao
interface CurrentLocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(it: CURRENTLOCATION_TBL)

    @Query("DELETE FROM CURRENTLOCATION_TBL")
    suspend fun trancate()

    @Query("SELECT * FROM CURRENTLOCATION_TBL ORDER BY dt DESC LIMIT 1 ")
    fun select_Flow(): Flow<CURRENTLOCATION_TBL>

    @Query("SELECT * FROM CURRENTLOCATION_TBL ORDER BY dt DESC LIMIT 1 ")
    fun select(): CURRENTLOCATION_TBL

}

