package com.example.czy.weather.util;

import android.app.Activity;
import android.content.Context;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by czy on 2017/7/18.
 * 网络请求单例类
 */

public class NetRequestInstance {

    private OkHttpClient okHttpClient;

    private NetRequestInstance() {
        okHttpClient = new OkHttpClient.Builder().build();
    }

    private static final NetRequestInstance INSTANCE = new NetRequestInstance();

    public static NetRequestInstance getInstance() {
        return INSTANCE;
    }

    /**
     * 网络请求
     *
     * @param context
     * @param address      请求地址
     * @param callBack 回调接口
     */
    public void netRequest(Context context, String address, final NetRequestCallBack callBack) {

        final Activity activity = (Activity) context;

        Request request = new Request.Builder().url(address).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //失败
                        callBack.onFailure("网络连接异常，请重试" + e);
                    }
                });

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                //将请求下来的数据转换成字符串
                final String data = response.body().string();

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //成功
                        callBack.onSuccess(data);
                    }
                });

            }
        });
    }

}
