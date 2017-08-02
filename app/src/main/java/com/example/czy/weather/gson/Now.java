package com.example.czy.weather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by czy on 2017/7/27.
 * 实况天气
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {
        @SerializedName("txt")
        public String info;
    }

}
