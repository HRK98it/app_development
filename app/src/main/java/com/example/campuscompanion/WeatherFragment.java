package com.example.campuscompanion;

// Experiment 4: display weather using a public REST API (OpenWeatherMap)

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.campuscompanion.net.ApiService;
import com.example.campuscompanion.net.RetrofitClient;
import com.example.campuscompanion.net.WeatherResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherFragment extends Fragment {


    private static final String API_KEY = "2d52ceb94c596f0a15f2b082e68793e9";

    private EditText etCity;
    private Button btnGetWeather;
    private TextView tvWeatherResult;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weather, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etCity = view.findViewById(R.id.etCity);
        etCity.setText("Nagpur");
        btnGetWeather = view.findViewById(R.id.btnGetWeather);
        tvWeatherResult = view.findViewById(R.id.tvWeatherResult);

        btnGetWeather.setOnClickListener(v -> fetchWeather());
    }

    private void fetchWeather() {
        String city = etCity.getText().toString().trim();
        if (TextUtils.isEmpty(city)) {
            Toast.makeText(requireContext(), "Enter a city name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (API_KEY.equals("YOUR_OPENWEATHERMAP_API_KEY")) {
            tvWeatherResult.setText("Weather API key is not configured. Add your OpenWeatherMap key in WeatherFragment.java.");
            return;
        }
        tvWeatherResult.setText("Loading weather for " + city + "...");
        ApiService apiService = RetrofitClient.getApiService();
        apiService.getWeather(city, API_KEY, "metric").enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weather = response.body();
                    String desc = weather.weather != null && !weather.weather.isEmpty()
                            ? weather.weather.get(0).description : "N/A";
                    String result = weather.cityName + ": " + weather.main.temp + "°C, " + desc
                            + "\nHumidity: " + weather.main.humidity + "%";
                    tvWeatherResult.setText(result);
                } else {
                    tvWeatherResult.setText("Could not load weather for " + city + ". Check the city name and API key.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                tvWeatherResult.setText("Error: " + t.getMessage());
            }
        });
    }
}
