package com.example.czy.weather.util;

/**
 * Created by czy on 2017/7/18.
 * 网络请求回调接口
 */

public interface NetRequestCallBack {

    void onFailure(String exception);

    void onSuccess(String response);

}
