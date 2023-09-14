package com.unchil.gismemo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unchil.gismemo.data.Repository
import com.unchil.gismemo.db.CURRENTWEATHER_TBL
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(val repository: Repository) : ViewModel() {


    val _currentWeatheStaterFlow: MutableStateFlow<CURRENTWEATHER_TBL?>
        = repository._currentWeather

      fun searchWeather(location: LatLng) {
         viewModelScope.launch {
             repository.getWeatherData(
                 location.latitude.toString(), location.longitude.toString()
             )
         }
    }


}