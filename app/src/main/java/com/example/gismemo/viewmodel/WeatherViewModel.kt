package com.example.gismemo.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import com.example.gismemo.data.Repository
import com.example.gismemo.db.CURRENTWEATHER_TBL
import com.example.gismemo.db.entity.CURRENTLOCATION_TBL
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WeatherViewModel(val repository: Repository) : ViewModel() {



    val _currentWeatheStaterFlow: MutableStateFlow<CURRENTWEATHER_TBL?>
        = repository._currentWeather

    val currentLocationStateFlow:StateFlow<CURRENTLOCATION_TBL?>
        = repository._currentLocation




     suspend fun searchWeather(location: Location) {


        repository.setCurrentLocation(
            CURRENTLOCATION_TBL(
                dt = location.time,
                latitude = location.latitude.toFloat(),
                longitude = location.longitude.toFloat(),
                altitude = location.altitude.toFloat()))

         repository.getWeatherData(
             location.latitude.toString(), location.longitude.toString())

    }


}