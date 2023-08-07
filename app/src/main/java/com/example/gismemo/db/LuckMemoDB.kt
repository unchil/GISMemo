package com.example.gismemo.db

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gismemo.db.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


val LocalLuckMemoDB = compositionLocalOf<LuckMemoDB> { error("Not Found Handler LuckMemoDB") }

@Database(
    entities = [
        CURRENTWEATHER_TBL::class,
        CURRENTLOCATION_TBL::class,
        MEMO_TBL::class,
        MEMO_FILE_TBL::class,
        MEMO_TEXT_TBL::class,
        MEMO_TAG_TBL::class,
        MEMO_WEATHER_TBL::class
    ],
    /*
    autoMigrations = [
        AutoMigration (
            from = 1,
            to = 2
        ),
        AutoMigration(
            from = 2,
            to = 3,
            spec = LuckMemoDB.LuckAutoMigration::class
        ),
        AutoMigration(
            from = 3,
            to = 4,
            spec = LuckMemoDB.LuckAutoMigration::class
        ),
        AutoMigration(
            from = 4,
            to = 5,
            spec = LuckMemoDB.LuckAutoMigration::class
        ),        AutoMigration(
            from = 5,
            to = 6,
            spec = LuckMemoDB.LuckAutoMigration::class
        )
    ],

     */
    version = LuckMemoDB.LATEST_VERSION,
    exportSchema = false
)

abstract class LuckMemoDB: RoomDatabase() {

    class LuckAutoMigration : AutoMigrationSpec {
        @Override
        override fun onPostMigrate(db: SupportSQLiteDatabase) {
        }
    }

    abstract val currentWeatherDao: CurrentWeatherDao
    abstract val currentLocationDao: CurrentLocationDao
    abstract val memoDao: MemoDao
    abstract val memoFileDao: MemoFileDao
    abstract val memoTagDao: MemoTagDao
    abstract val memoTextDao: MemoTextDao
    abstract val memoWeatherDao: MemoWeatherDao



    companion object {
        const val LATEST_VERSION = 1

        @Volatile
        private var INSTANCE: LuckMemoDB? = null

        fun getInstance(context: Context): LuckMemoDB =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext,
                LuckMemoDB::class.java, "LuckMemoDB")
                .addCallback(object : Callback(){
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // Default Data Insert
                     //   fillInTestData(context.applicationContext)
                    }
                })
                .fallbackToDestructiveMigration()
                .build()



        private fun fillInTestData(context:Context) =   CoroutineScope(Dispatchers.IO).launch {

                val memoList:MutableList<MEMO_TBL> = mutableListOf()
                 val memoTagList:MutableList<MEMO_TAG_TBL> = mutableListOf()

                (1..10000).forEach{


                    val isSecret = if(it%3 == 0) true else false
                    val isPin = if(it%5 == 0) true else false
                    val id = System.currentTimeMillis() + it

                    val data = MEMO_TBL(
                        id = id ,
                        latitude = 0f,
                        longitude = 0f,
                        altitude = 0f,
                        isSecret = isSecret,
                        isPin = isPin,
                        title = "테스트 메모",
                        snippets = "snippets",
                        desc = "description",
                        snapshot = "",
                        snapshotCnt = 1,
                        textCnt = 0,
                        photoCnt = 0,
                        videoCnt = 0
                    )

                    memoList.add(data)

                    val tagData = MEMO_TAG_TBL(
                        id = id,
                        10000
                    )
                    memoTagList.add(tagData)
                }


                getInstance(context).memoDao.insert_List(memoList)
                getInstance(context).memoTagDao.insert(memoTagList)

        }



    }


}
