package com.example.gismemo.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gismemo.data.Repository
import com.example.gismemo.db.CURRENTWEATHER_TBL
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(val repository: Repository) : ViewModel() {


    val _currentWeatheStaterFlow: MutableStateFlow<CURRENTWEATHER_TBL?>
        = repository._currentWeather

      fun searchWeather(location: Location) {
         viewModelScope.launch {
             repository.getWeatherData(
                 location.latitude.toString(), location.longitude.toString()
             )
         }
    }


}