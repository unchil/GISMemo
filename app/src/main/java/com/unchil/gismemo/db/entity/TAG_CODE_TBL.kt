package com.unchil.gismemo.db.entity

import androidx.room.*


@Entity(tableName= "TAG_CODE_TBL")
data class TAG_CODE_TBL(
    @PrimaryKey(autoGenerate = true)
    var seq: Long?,
    var iconName:String
)

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(it: TAG_CODE_TBL)

    @Query("DELETE FROM TAG_CODE_TBL")
    suspend fun trancate()

    @Query("DELETE FROM TAG_CODE_TBL WHERE seq = :seq")
    suspend fun delete(seq:Long)

}