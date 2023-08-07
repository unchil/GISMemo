package com.example.gismemo.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gismemo.db.entity.MEMO_TEXT_TBL
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoTextDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(it: List<MEMO_TEXT_TBL>)

    @Query("SELECT * FROM MEMO_TEXT_TBL WHERE id = :id ")
    fun select_Flow(id:Long): Flow<List<MEMO_TEXT_TBL>>

    @Query("SELECT * FROM MEMO_TEXT_TBL WHERE id = :id ")
    fun select(id:Long): List<MEMO_TEXT_TBL>


    @Query("DELETE FROM MEMO_TEXT_TBL  WHERE id = :id")
    suspend fun delete(id:Long)

    @Query("DELETE FROM MEMO_TEXT_TBL")
    suspend fun trancate()

}