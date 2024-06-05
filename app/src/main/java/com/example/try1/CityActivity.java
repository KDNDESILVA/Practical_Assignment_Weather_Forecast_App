package com.example.try1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CityActivity  extends AppCompatActivity {

    private static final String WEATHER_API_KEY = "698fd58d906808539983f8199f204f1a";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private static final String PREFS_NAME = "WeatherPrefs";
    private static final String PREF_CITY_NAME = "LastCityName";
    private TextView descriptionTextView, tempratureTextView, humidityTextView, cityTextView ;
    private ImageView weatherIcon, tempIcon, humidIcon, statusIcon;
    private EditText enterCity;
    private ImageButton searchButton, backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);

        enterCity = findViewById(R.id.enterCity);
        searchButton = findViewById(R.id.btnSearch);
        descriptionTextView = findViewById(R.id.textView_description);
        tempratureTextView = findViewById(R.id.textView_temp);
        humidityTextView = findViewById(R.id.textView_humidity);
        weatherIcon = findViewById(R.id.weatherIcon);
        tempIcon = findViewById(R.id.tempIcon);
        humidIcon = findViewById(R.id.humidIcon);
        statusIcon = findViewById(R.id.statusIcon);
        cityTextView = findViewById(R.id.cityTextView);
        backButton = findViewById(R.id.backButton);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityName = enterCity.getText().toString().trim();
                if (!cityName.isEmpty()) {
                    fetchWeatherByCity(cityName);
                    saveCityName(cityName);
                    cityTextView.setText("'" + cityName + "'" );
                } else {
                    Toast.makeText(CityActivity.this, "Please enter a city name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(CityActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        String lastCityName = getLastSearchedCity();
        if (lastCityName != null) {
            cityTextView.setText(lastCityName);
            fetchWeatherByCity(lastCityName);
        }

    }
    private void fetchWeatherByCity(String cityName) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherService weatherService = retrofit.create(WeatherService.class);
        Call<WeatherResponse> call = weatherService.getCurrentWeatherByCity(cityName, WEATHER_API_KEY, "metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weatherResponse = response.body();

                    float temperature = weatherResponse.main.temp;
                    tempratureTextView.setText(String.format(Locale.getDefault(), "%.1f °C", temperature));

                    int humidity = weatherResponse.main.humidity;
                    humidityTextView.setText(String.format(Locale.getDefault(), "%d %%", humidity));

                    descriptionTextView.setText(weatherResponse.weather[0].description.trim());

                    String iconCode = weatherResponse.weather[0].icon;
                    setWeatherIcon(iconCode);

                } else {
                    Log.e("TAG", "Response not successful: " + response.message());
                    descriptionTextView.setText("Unable to fetch weather data");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e("TAG", "Network call failure: " + t.getMessage());
                descriptionTextView.setText("Unable to fetch weather data");
            }
        });
    }
    private void setWeatherIcon(String iconCode) {
        String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";

        Picasso.get().load(iconUrl).into(weatherIcon);
    }

    private void saveCityName(String cityName) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_CITY_NAME, cityName);
        editor.apply();
    }

    private String getLastSearchedCity() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(PREF_CITY_NAME, null);
    }


}
