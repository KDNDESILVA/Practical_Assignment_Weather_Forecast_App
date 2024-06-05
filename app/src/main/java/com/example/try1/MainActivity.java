package com.example.try1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String WEATHER_API_KEY = "698fd58d906808539983f8199f204f1a";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private static final String TAG = "MainActivity";

    private FusedLocationProviderClient fusedLocationClient;
    private TextView latLngTextView, addressTextView, timeTextView, descriptionTextView,
            tempratureTextView, humidityTextView ;
    private ImageView weatherIcon;
    private Button citySearchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latLngTextView = findViewById(R.id.textView_lat_lng);
        addressTextView = findViewById(R.id.textView_address);
        timeTextView = findViewById(R.id.textView_time);
        descriptionTextView = findViewById(R.id.textView_description);
        tempratureTextView = findViewById(R.id.textView_temp);
        humidityTextView = findViewById(R.id.textView_humidity);
        weatherIcon = findViewById(R.id.imageView_weather_icon);
        citySearchButton = findViewById(R.id.citySearch);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fetchLocation();
        updateCurrentTime();

        citySearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CityActivity.class);
                startActivity(intent);
            }
        });

    }

    private void fetchLocation() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                latLngTextView.setText(String.format(Locale.getDefault(), "Lat: %.4f, Lng: %.4f", latitude, longitude));
                fetchAddressFromLocation(latitude, longitude);
                fetchWeatherData(latitude, longitude);
            }
        });
    }

    private void fetchAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                addressTextView.setText(address.getAddressLine(0));
            } else {
                addressTextView.setText("Unable to get address");
            }
        } catch (IOException e) {
            e.printStackTrace();
            addressTextView.setText("Unable to get address");
        }
    }

    private void updateCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        timeTextView.setText(currentTime);
    }

    private void fetchWeatherData(double latitude, double longitude) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherService weatherService = retrofit.create(WeatherService.class);
        Call<WeatherResponse> call = weatherService.getCurrentWeather(latitude, longitude, WEATHER_API_KEY, "metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weatherResponse = response.body();

                    float temperature = weatherResponse.main.temp;
                    String temperatureString = String.format(Locale.getDefault(), "%.1f", temperature);
                    tempratureTextView.setText(temperatureString + "°");

                    int humidity = weatherResponse.main.humidity;
                    String humidityString = String.valueOf(humidity);
                    humidityTextView.setText(humidityString + "%");

                    descriptionTextView.setText(weatherResponse.weather[0].description);

                    String iconCode = weatherResponse.weather[0].icon;
                    setWeatherIcon(iconCode);

                } else {
                    Log.e(TAG, "Response not successful: " + response.message());
                    descriptionTextView.setText("Unable to fetch weather data");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e(TAG, "Network call failure: " + t.getMessage());
                descriptionTextView.setText("Unable to fetch weather data");
            }
        });
    }
    private void setWeatherIcon(String iconCode) {
        String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";

        Picasso.get().load(iconUrl).into(weatherIcon);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
