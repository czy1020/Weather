package com.example.czy.weather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.czy.weather.gson.Forecast;
import com.example.czy.weather.gson.Weather;
import com.example.czy.weather.util.Net;
import com.example.czy.weather.util.NetRequestCallBack;
import com.example.czy.weather.util.NetRequestInstance;
import com.example.czy.weather.util.Utility;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView scrollView;
    private TextView titleCityTv, titleUpdateTimeTv, degreeTv, weatherInfoTv,
            aqiTv, pm25Tv, comfortTv, carWashTv, sportTv;
    private LinearLayout forecastLayout;
    private ImageView bgIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        //使状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        //初始化组件
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        titleCityTv = (TextView) findViewById(R.id.title_city_tv);
        titleUpdateTimeTv = (TextView) findViewById(R.id.title_update_time_tv);
        degreeTv = (TextView) findViewById(R.id.degree_tv);
        weatherInfoTv = (TextView) findViewById(R.id.weather_info_tv);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiTv = (TextView) findViewById(R.id.aqi_tv);
        pm25Tv = (TextView) findViewById(R.id.pm25_tv);
        comfortTv = (TextView) findViewById(R.id.comfort_tv);
        carWashTv = (TextView) findViewById(R.id.car_wash_tv);
        sportTv = (TextView) findViewById(R.id.sport_tv);
        bgIv = (ImageView) findViewById(R.id.bg_iv);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = preferences.getString("weather", null);
        if (weatherString != null) {
            //有缓存时直接解析
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询
            String weatherId = getIntent().getStringExtra("weather_id");
            scrollView.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        String bgPic = preferences.getString("bgPic", null);
        if (bgPic != null) {
            Glide.with(this).load(bgPic).into(bgIv);
        } else {
            loadPic();
        }
    }

    private void loadPic() {

        final String bgPicUrl = Net.BACKGROUND_PIC;
        NetRequestInstance.getInstance().netRequest(this, bgPicUrl, new NetRequestCallBack() {
            @Override
            public void onFailure(String exception) {
                Log.d("WeatherActivity", exception);
            }

            @Override
            public void onSuccess(String response) {

                Log.d("WeatherActivity", response);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bgPic", response);
                editor.apply();

                Glide.with(WeatherActivity.this).load(bgPicUrl).into(bgIv);

            }
        });

    }

    /**
     * 根据天气Id请求城市天气信息
     *
     * @param weatherId
     */
    private void requestWeather(String weatherId) {

        String weatherUrl = Net.WEATHER_URL + weatherId + Net.WEATHER_API_KEY;
        Log.d("WeatherActivity", weatherId);
        NetRequestInstance.getInstance().netRequest(this, weatherUrl, new NetRequestCallBack() {
            @Override
            public void onFailure(String exception) {
                Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(String response) {

                Weather weather = Utility.handleWeatherResponse(response);
                Log.d("WeatherActivity", response);
                if (weather != null && "ok".equals(weather.status)) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                    editor.putString("weather", response);
                    editor.apply();
                    showWeatherInfo(weather);
                } else {
                    Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                }

            }
        });

        loadPic();

    }

    /**
     * 处理并展示Weather实体类中的数据
     *
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {

        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCityTv.setText(cityName);
        titleUpdateTimeTv.setText(updateTime);
        degreeTv.setText(degree);
        weatherInfoTv.setText(weatherInfo);
        forecastLayout.removeAllViews();

        for (Forecast forecast : weather.forecasts) {

            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateTv = view.findViewById(R.id.date_tv);
            TextView infoTv = view.findViewById(R.id.info_tv);
            TextView maxTv = view.findViewById(R.id.max_tv);
            TextView minTv = view.findViewById(R.id.min_tv);
            dateTv.setText(forecast.date);
            infoTv.setText(forecast.more.info);
            maxTv.setText(forecast.temperature.max);
            minTv.setText(forecast.temperature.min);
            forecastLayout.addView(view);

        }

        if (weather.aqi != null) {
            aqiTv.setText(weather.aqi.city.aqi);
            pm25Tv.setText(weather.aqi.city.pm25);
        }

        String comfort = "舒适度" + weather.suggestion.comfort.info;
        String carWash = "洗车指数" + weather.suggestion.carWash.info;
        String sport = "运动建议" + weather.suggestion.sport.info;

        comfortTv.setText(comfort);
        carWashTv.setText(carWash);
        sportTv.setText(sport);
        scrollView.setVisibility(View.VISIBLE);

    }
}
