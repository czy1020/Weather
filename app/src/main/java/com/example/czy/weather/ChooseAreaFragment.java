package com.example.czy.weather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.czy.weather.db.City;
import com.example.czy.weather.db.County;
import com.example.czy.weather.db.Province;
import com.example.czy.weather.util.Net;
import com.example.czy.weather.util.NetRequestCallBack;
import com.example.czy.weather.util.NetRequestInstance;
import com.example.czy.weather.util.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.litepal.crud.DataSupport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by czy on 2017/7/18.
 */

public class ChooseAreaFragment extends Fragment {

    private static int LEVEL_PROVINCE = 0;
    private static int LEVEL_CITY = 1;
    private static int LEVEL_COUNTY = 2;
    private TextView titleTv;
    private Button backBtn;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private List<String> data = new ArrayList<>();
    private List<Province> provinces;
    private List<City> cities;
    private List<County> counties;
    private Province selectedProvince;
    private City selectedCity;
    //当前选中级别
    private int currentLevel;
    private ProgressDialog progressDialog;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.choose_area, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleTv = view.findViewById(R.id.title_tv);
        backBtn = view.findViewById(R.id.back_btn);
        listView = view.findViewById(R.id.list_view);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, data);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinces.get(i);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cities.get(i);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String weatherId = counties.get(i).getWeatherId();
                    Intent intent = new Intent(getContext(), WeatherActivity.class);
                    intent.putExtra("weather_id", weatherId);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    /**
     * 查询全国的所有的省，优先从数据库查询，如果没有则去服务器查询
     */
    private void queryProvinces() {
        titleTv.setText("中国");
        backBtn.setVisibility(View.GONE);
        provinces = DataSupport.findAll(Province.class);
        if (provinces.size() > 0) {
            data.clear();
            for (Province province : provinces) {
                data.add(province.getProvinceName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = Net.CITY_BASE_URL;
            queryFromServer(address, "province");
        }
    }

    /**
     * 查询选中的省内所有市
     */
    private void queryCities() {
        titleTv.setText(selectedProvince.getProvinceName());
        backBtn.setVisibility(View.VISIBLE);
        cities = DataSupport.where("provinceid=?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cities.size() > 0) {
            data.clear();
            for (City city : cities) {
                data.add(city.getCityName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = Net.CITY_BASE_URL + provinceCode;
            Log.d("cityAdd", address);
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询选中市内所有的县
     */
    private void queryCounties() {
        titleTv.setText(selectedCity.getCityName());
        backBtn.setVisibility(View.VISIBLE);
        counties = DataSupport.where("cityid=?", String.valueOf(selectedCity.getId())).find(County.class);
        if (counties.size() > 0) {
            data.clear();
            for (County county : counties) {
                data.add(county.getCountyName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = Net.CITY_BASE_URL + provinceCode + "/" + cityCode;
            Log.d("countyAdd", address);
            queryFromServer(address, "county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县的数据
     */
    private void queryFromServer(String address, final String type) {

        showProgressDialog();

        //网络请求
        NetRequestInstance.getInstance().netRequest(getContext(), address, new NetRequestCallBack() {
            @Override
            public void onFailure(String exception) {

                closeProgressDialog();

                Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();

                Log.d("ChooseAreaFragment", "网络连接异常" + exception);

            }

            @Override
            public void onSuccess(String response) {

//                Log.d("ChooseAreaFragment", response);
//
//                Gson gson = new Gson();
//
//                Type provinceList = new TypeToken<LinkedList<Province>>() {}.getType();
//                LinkedList<Province> province = gson.fromJson(response, provinceList);
//                Log.d("provinceList", province.toString());
//
//                Type cityList = new TypeToken<LinkedList<Province>>() {}.getType();
//                LinkedList<City> city = gson.fromJson(response, cityList);
//                Log.d("cityList", city.toString());
//
//                Type countyList = new TypeToken<LinkedList<Province>>() {}.getType();
//                LinkedList<County> county = gson.fromJson(response, countyList);
//                Log.d("countyList", county.toString());

//                province.save();
//                City city = gson.fromJson(response, City.class);
//                city.save();
//                County county = gson.fromJson(response, County.class);
//                county.save();

                Log.d("服务器返回数据", response);

                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(response);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(response, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountytReponse(response, selectedCity.getId());
                }

                if (result) {
                    closeProgressDialog();
                    if ("province".equals(type)) {
                        queryProvinces();
                    } else if ("city".equals(type)) {
                        queryCities();
                    } else if ("county".equals(type)) {
                        queryCounties();
                    }
                }

            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 隐藏进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
