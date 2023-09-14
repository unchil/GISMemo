package com.unchil.gismemo.api


import com.unchil.gismemo.model.CurrentWeather
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherInterface {
    @GET("weather")
    suspend fun getWeatherData(@Query("lat")latitude: String,
                               @Query("lon")longitude: String,
                               @Query("units")units: String,
                               @Query("appid")apiKey: String) : CurrentWeather
}

