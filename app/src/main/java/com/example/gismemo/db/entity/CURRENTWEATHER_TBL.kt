package com.example.gismemo.db

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.icu.text.SimpleDateFormat
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.gismemo.R


@Entity(tableName = "CURRENTWEATHER_TBL")
data class CURRENTWEATHER_TBL(

    @PrimaryKey(autoGenerate = false)
//        var writeTime: Long,
    var dt: Long,
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


const val MILLISEC_CHECK = 9999999999
const val MILLISEC_DIGIT = 1L
const val MILLISEC_CONV_DIGIT = 1000L
const val yyyyMMddHHmm = "yyyy/MM/dd HH:mm"
const val HHmmss = "HH:mm:ss"

const val WEATHER_TEXT_SUN = "sunrise:%s sunset:%s"
const val WEATHER_TEXT_TEMP = "temp:%,.0f°C  min:%,.0f°C  max:%,.0f°C"
const val WEATHER_TEXT_WEATHER = "pressure:%,.0fhPa humidity:%,.0f"
const val WEATHER_TEXT_WIND = "wind:%,.0fm/s deg:%,.0f° visibility:%dkm"

const  val TAG_M_KM = 1000

@SuppressLint("SimpleDateFormat")
fun UnixTimeToString(time: Long, format: String): String{
    val UNIXTIMETAG_SECTOMILI
            = if( time > MILLISEC_CHECK) MILLISEC_DIGIT else MILLISEC_CONV_DIGIT

    return SimpleDateFormat(format)
        .format(time * UNIXTIMETAG_SECTOMILI )
        .toString()
}

fun CURRENTWEATHER_TBL.toTextHeadLine(): String {
    return UnixTimeToString(this.dt, yyyyMMddHHmm) + "  ${this.name}/${this.country}"
}


fun CURRENTWEATHER_TBL.toTextWeatherDesc(): String {
    return  "${this.main} : ${this.description}"
}


fun CURRENTWEATHER_TBL.toTextSun(getString: (Int)->String ): String {
    return String.format( getString(R.string.weather_desc_sun),
        UnixTimeToString(this.sunrise, HHmmss),
        UnixTimeToString(this.sunset, HHmmss)
    )
}

fun CURRENTWEATHER_TBL.toTextTemp(getString: (Int)->String): String {
    return String.format ( getString(R.string.weather_desc_temp),
        this.temp,
        this.temp_min,
        this.temp_max
    )
}


fun CURRENTWEATHER_TBL.toTextWeather(getString: (Int)->String): String {
    return String.format( getString(R.string.weather_desc_weather),
        this.pressure,
        this.humidity
    ) + "%"
}


fun CURRENTWEATHER_TBL.toTextWind(getString: (Int)->String): String {
    return   String.format(
        getString(R.string.weather_desc_wind),
        this.speed,
        this.deg,
        this.visibility/ TAG_M_KM )
}

/*
val INIT_CURRENT_WEATHER = CURRENTWEATHER_TBL(
    dt = 0L,
    base = "",
    visibility = 0,
    timezone = 0L,
    name = "",
    latitude = 0F,
    longitude = 0F,
    main = "",
    description = "",
    icon = "",
    temp = 0F,
    feels_like = 0F,
    pressure = 0F,
    humidity = 0F,
    temp_min = 0F,
    temp_max = 0F,
    speed = 0F,
    deg = 0F,
    all = 0,
    type = 0,
    country = "",
    sunrise = 0L,
    sunset = 0L )

*/
