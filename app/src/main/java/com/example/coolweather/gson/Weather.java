package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {
    public String status;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;
    @SerializedName("daily_forecast")       //序列化注解，告诉Gson，forecastList对应的就是daily_forecast
    public List<Forecast> forecastList;     //daily_forecast就是forecastList的标签一样
}
