package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Suggestion {
    @SerializedName("comf")
    public Comfort comfort; //舒适度

    @SerializedName("cw")
    public Carwash carwash;//洗车建议

    @SerializedName("sport")
    public Sport sport;  //运动建议

    public class Comfort{
        @SerializedName("txt")
        public String info;
    }

    public class Carwash{
        @SerializedName("txt")
        public String info;
    }

    public class Sport{
        @SerializedName("txt")
        public String info;
    }
}
