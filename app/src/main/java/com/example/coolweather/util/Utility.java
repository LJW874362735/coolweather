package com.example.coolweather.util;

import android.text.TextUtils;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//处理Json数据
public class Utility {
    /* 解析和处理服务器返回的省级数据*/
    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvinces=new JSONArray(response); //Json数组
                for (int i=0;i<allProvinces.length();i++){ //取出Json数组的每一个元素JSONObject
                    JSONObject provinceObject=allProvinces.getJSONObject(i);
                    Province province=new Province();
                    //解析Json字段，并放到实体类存入到数据库中
                    province.setProvinceName(provinceObject.getString("name")); //获取省的名字
                    province.setProvinceCode(provinceObject.getInt("id")); //获取省的代号id
                    province.save(); //将数据存储到数据库中

                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /* 解析和处理服务器返回的市级数据*/
    public static boolean handleCityResponse(String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCity=new JSONArray(response); //Json数组
                for (int i=0;i<allCity.length();i++){ //取出Json数组的每一个元素JSONObject
                    JSONObject cityObject=allCity.getJSONObject(i);
                    City city=new City();
                    //解析Json字段，并放到实体类存入到数据库中
                    city.setCityName(cityObject.getString("name")); //获取省的名字
                    city.setCityCode(cityObject.getInt("id")); //获取省的代号id
                    city.setProvinceId(provinceId);
                    city.save(); //将数据存储到数据库中

                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /* 解析和处理服务器返回的县级数据*/
    public static boolean handleCountyResponse(String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCounty=new JSONArray(response); //Json数组
                for (int i=0;i<allCounty.length();i++){ //取出Json数组的每一个元素JSONObject
                    JSONObject countyJSONObject=allCounty.getJSONObject(i);
                    County county=new County();
                    //解析Json字段，并放到实体类存入到数据库中
                    county.setCountyName(countyJSONObject.getString("name")); //获取省的名字
                    county.setWeatherId(countyJSONObject.getString("weather_id")); //获取省的代号id
                    county.setCityId(cityId);
                    county.save(); //将数据存储到数据库中

                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }


    /*将返回的Json数据，用Gson解析成weather实体类*/
    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject=new JSONObject(response); //将response放到JsonObject对象
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather"); //获取JsonObject中的JsonArray数组HeWeather
            String weatherContent=jsonArray.getJSONObject(0).toString();//获取sonArray数组HeWeather的第一个元素并转为Json字符串
            return new Gson().fromJson(weatherContent,Weather.class);//将Json字符串转为Weather实体
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }
}
