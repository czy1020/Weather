package com.example.czy.weather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by czy on 2017/7/27.
 * 基本信息
 */

public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {

        @SerializedName("loc")
        public String updateTime;

    }


}
