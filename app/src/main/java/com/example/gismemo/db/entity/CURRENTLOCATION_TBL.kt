package com.example.gismemo.db.entity

import android.location.Location
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng


@Entity(tableName = "CURRENTLOCATION_TBL")
data class CURRENTLOCATION_TBL(
    @PrimaryKey(autoGenerate = false)
    var dt: Long,
    var latitude: Float,
    var longitude: Float,
    var altitude: Float
)

fun Location.toCURRENTLOCATION_TBL(): CURRENTLOCATION_TBL {
    return CURRENTLOCATION_TBL(
        dt = this.time,
        latitude = this.latitude.toFloat(),
        longitude = this.longitude.toFloat(),
        altitude = this.altitude.toFloat()
    )
}

fun CURRENTLOCATION_TBL.toLatLng(): LatLng {

    return LatLng(
        this.latitude.toDouble(),
        this.longitude.toDouble()
    )
}
