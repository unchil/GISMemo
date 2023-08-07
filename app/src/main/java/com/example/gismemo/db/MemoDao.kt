package com.example.gismemo.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gismemo.db.entity.MEMO_TBL
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {
    //------------ MEMO_TBL
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(it: MEMO_TBL)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert_List(it: List<MEMO_TBL>)

    @Query("SELECT * FROM MEMO_TBL  ORDER BY id DESC")
    fun select_All_Flow(): Flow<List<MEMO_TBL>>

    @Query("SELECT * FROM MEMO_TBL  WHERE isPin = 1")
    fun select_Marker_Flow(): Flow<List<MEMO_TBL>>


    @Query("SELECT * FROM MEMO_TBL  ORDER BY id DESC")
    fun  select_All_Paging(): PagingSource<Int, MEMO_TBL>

    /*
    @Query("SELECT * FROM MEMO_TBL WHERE  id BETWEEN :fromDate AND  :toDate  ORDER BY id DESC ")
    fun  select_Search_Paging(fromDate:Long, toDate:Long): PagingSource<Int, MEMO_TBL>
*/

    @Query(
"SELECT D.* " +
        "FROM " +
        "(" +
        "    SELECT A.ID " +
        "    FROM MEMO_TBL A, MEMO_TAG_TBL B " +
        "    WHERE A.ID = B.ID " +
        "    AND B.'INDEX' IN (:tagArray) " +
        "    GROUP BY A.ID " +
        ") C , MEMO_TBL D " +
        "WHERE D.ID = C.ID " +
        "AND D.ID BETWEEN :fromDate AND :toDate " +
        "AND D.ISSECRET IN (:secretArray) " +
        "AND D.ISPIN IN (:markerArray) " +
        "AND D.TITLE LIKE :title " +
        "ORDER BY D.ID DESC"
    )
    fun  select_Search_Paging(
        tagArray:ArrayList<Int>,
        fromDate:Long,
        toDate:Long,
        secretArray:ArrayList<Int>,
        markerArray:ArrayList<Int>,
        title:String
    ): PagingSource<Int, MEMO_TBL>


    @Query("SELECT * FROM MEMO_TBL WHERE id = :id  LIMIT 1")
    fun select_Flow(id:Long): Flow<MEMO_TBL>

    @Query("DELETE FROM MEMO_TBL WHERE id = :id")
    suspend fun delete(id:Long)


    @Query("UPDATE MEMO_TBL  SET isSecret = :secret WHERE id = :id")
    suspend fun update_Secret(id:Long, secret:Boolean)

    @Query("UPDATE MEMO_TBL  SET isPin = :mark WHERE id = :id")
    suspend fun update_Marker(id:Long, mark:Boolean)


    @Query("UPDATE MEMO_TBL  SET snippets = :snippets WHERE id = :id")
    suspend fun update_Snippets(id:Long, snippets:String)

    @Query("DELETE FROM MEMO_TBL")
    suspend fun trancate()

}