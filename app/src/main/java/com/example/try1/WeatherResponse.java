package com.example.try1;

import com.google.gson.annotations.SerializedName;

public class WeatherResponse {

    @SerializedName("main")
    public Main main;

    @SerializedName("weather")
    public Weather[] weather;

    public static class Main {
        @SerializedName("temp")
        public float temp;

        @SerializedName("humidity")
        public int humidity;
    }

    public class Weather {
        @SerializedName("description")
        public String description;
        public String icon;
    }
}
