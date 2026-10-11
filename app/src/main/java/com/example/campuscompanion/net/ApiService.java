package com.example.campuscompanion.net;

// Experiment 4: Retrofit interface for the OpenWeatherMap REST API

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    // Get your free key at https://openweathermap.org/api
    @GET("weather")
    Call<WeatherResponse> getWeather(
            @Query("q") String city,
            @Query("appid") String apiKey,
            @Query("units") String units // "metric" for Celsius
    );
}
