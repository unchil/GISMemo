package com.unchil.gismemo.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.unchil.gismemo.db.CURRENTWEATHER_TBL

@Entity(tableName = "MEMO_TBL")
data class MEMO_TBL(
    @PrimaryKey(autoGenerate = false)
    var id: Long,
    var latitude: Float,
    var longitude: Float,
    var altitude: Float,
    var isSecret: Boolean,
    var isPin: Boolean,
    var title: String,
    var snippets: String,
    var desc: String,
    var snapshot: String,
    var snapshotCnt: Int,
    var textCnt: Int,
    var photoCnt: Int,
    var videoCnt: Int
)

@Entity(tableName = "MEMO_FILE_TBL", primaryKeys = arrayOf("id", "type", "index", "subIndex"))
data class MEMO_FILE_TBL(
    var id: Long,
    var type: String,
    var index:Int,
    var subIndex:Int,
    var filePath: String
)

@Entity(tableName = "MEMO_TEXT_TBL",  primaryKeys = arrayOf("id", "index"))
data class MEMO_TEXT_TBL(
    var id: Long,
    var index:Int,
    var comment:String
)

@Entity(tableName = "MEMO_TAG_TBL", primaryKeys = arrayOf("id", "index"))
data class MEMO_TAG_TBL(
    var id: Long,
    var index:Int,
)


@Entity(tableName = "MEMO_WEATHER_TBL")
data class MEMO_WEATHER_TBL(
    @PrimaryKey(autoGenerate = false)
    var id: Long,
    var base: String ,
    var visibility: Int,
    var timezone: Long,
    var name: String,
    var latitude: Float,
    var longitude: Float,
    //       var altitude: Float,
    var main: String,
    var description: String,
    var icon : String,
    var temp: Float,
    var feels_like: Float,
    var pressure: Float,
    var humidity: Float,
    var temp_min: Float,
    var temp_max: Float,
    var speed : Float,
    var deg : Float,
    var all  : Int,
    var type : Int,
    var country : String,
    var sunrise : Long,
    var sunset : Long

)

fun MEMO_WEATHER_TBL.toCURRENTWEATHER_TBL():CURRENTWEATHER_TBL{
    return CURRENTWEATHER_TBL(
        dt = this.id,
        base = this.base,
        visibility = this.visibility,
        timezone = this.timezone,
        name = this.name,
        latitude = this.latitude,
        longitude = this.longitude,
        main = this.main,
        description = this.description,
        icon = this.icon,
        temp = this.temp,
        feels_like = this.feels_like,
        pressure = this.pressure,
        humidity = this.humidity,
        temp_min = this.temp_min,
        temp_max = this.temp_max,
        speed = this.speed,
        deg = this.deg,
        all = this.all,
        type = this.type,
        country = this.country,
        sunrise = this.sunrise,
        sunset = this.sunset )

}

fun CURRENTWEATHER_TBL.toMEMO_WEATHER_TBL(id:Long):MEMO_WEATHER_TBL{
    return MEMO_WEATHER_TBL(
        id = id,
        base = this.base,
        visibility = this.visibility,
        timezone = this.timezone,
        name = this.name,
        latitude = this.latitude,
        longitude = this.longitude,
        main = this.main,
        description = this.description,
        icon = this.icon,
        temp = this.temp,
        feels_like = this.feels_like,
        pressure = this.pressure,
        humidity = this.humidity,
        temp_min = this.temp_min,
        temp_max = this.temp_max,
        speed = this.speed,
        deg = this.deg,
        all = this.all,
        type = this.type,
        country = this.country,
        sunrise = this.sunrise,
        sunset = this.sunset )

}