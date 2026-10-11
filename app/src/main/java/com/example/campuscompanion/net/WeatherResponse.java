package com.example.campuscompanion.net;

// Experiment 4: model matching OpenWeatherMap's /weather JSON response

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {

    @SerializedName("name")
    public String cityName;

    @SerializedName("main")
    public Main main;

    @SerializedName("weather")
    public List<Weather> weather;

    public static class Main {
        @SerializedName("temp")
        public double temp;

        @SerializedName("humidity")
        public int humidity;
    }

    public static class Weather {
        @SerializedName("description")
        public String description;
    }
}
