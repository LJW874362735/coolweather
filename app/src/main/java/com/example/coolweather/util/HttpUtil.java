package com.example.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {
    //请求网络
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient okHttpClient=new OkHttpClient();
        Request request=new Request.Builder().url(address).build();
        okHttpClient.newCall(request).enqueue(callback);
    }
}
