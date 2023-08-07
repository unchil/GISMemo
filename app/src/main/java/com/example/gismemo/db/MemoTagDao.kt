package com.example.gismemo.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gismemo.db.entity.MEMO_TAG_TBL
import kotlinx.coroutines.flow.Flow


@Dao
interface MemoTagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(it: List<MEMO_TAG_TBL>)

    @Query("SELECT * FROM MEMO_TAG_TBL A WHERE A.id = :id AND A.'index' != 10000 ")
    fun select_Flow(id:Long): Flow<List<MEMO_TAG_TBL>>

    @Query("DELETE FROM MEMO_TAG_TBL WHERE id = :id")
    suspend fun delete(id:Long)

    @Query("DELETE FROM MEMO_TAG_TBL")
    suspend fun trancate()

}