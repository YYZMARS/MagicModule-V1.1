package com.uptech.magicmodule;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.uptech.magicmodule.bean.Forecast;
import com.uptech.magicmodule.bean.Weather;
import com.uptech.magicmodule.utils.HttpUtil;
import com.uptech.magicmodule.utils.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static java.lang.System.loadLibrary;


/**
 * A simple {@link Fragment} subclass.
 */
public class Rain extends Fragment {
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private ImageView tv_clo1;
    private ImageView tv_clo2;
    private ImageView tv_clo3;
    private ImageView tv_squ1;
    private ImageView tv_squ2;
    private ImageView tv_squ3;
    private ImageView tv_shoe1;
    private ImageView tv_shoe2;
    private ImageView tv_shoe3;

    private TextView aqiText;
    private TextView pm25Text;
    private ImageView bingPicImg;
//    private TextView comfortText;
//    private TextView carWashText;
//    private TextView sportText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container,false );
        weatherLayout = (ScrollView)view.findViewById(R.id.weather_layout);
        titleCity = (TextView)view.findViewById(R.id.title_city);
        titleUpdateTime = (TextView)view.findViewById(R.id.title_update_time);
        degreeText = (TextView)view.findViewById(R.id.degree_text);
        weatherInfoText = (TextView)view.findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) view.findViewById(R.id.forecast_layout);
        aqiText = (TextView)view.findViewById(R.id.aqi_text);
        pm25Text = (TextView)view.findViewById(R.id.pm25_text);
        tv_clo1 = (ImageView)view.findViewById(R.id.clo1);
        tv_clo2 = (ImageView)view.findViewById(R.id.clo2);
        tv_clo3 = (ImageView)view.findViewById(R.id.clo3);
        tv_squ1 = (ImageView)view.findViewById(R.id.squ1);
        tv_squ2 = (ImageView)view.findViewById(R.id.squ2);
        tv_squ3 = (ImageView)view.findViewById(R.id.squ3);
        tv_shoe1 = (ImageView)view.findViewById(R.id.shoe1);
        tv_shoe2 = (ImageView)view.findViewById(R.id.shoe2);
        tv_shoe3 = (ImageView)view.findViewById(R.id.shoe3);
        bingPicImg = (ImageView)view.findViewById(R.id.bing_pic_img);
        //loadbingpic();

//        comfortText = (TextView)view.findViewById(R.id.comfort_text);
//        carWashText = (TextView)view.findViewById(R.id.car_wash_text);
//        sportText = (TextView)view.findViewById(R.id.sport_text);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String weatherString = prefs.getString("weather",null );
        if(weatherString != null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        }else{
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather("CN101010400");
        }
        return view;
    }

    public void requestWeather(String weatherId){

        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(),"获取天气信息失败" ,Toast.LENGTH_SHORT ).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                            editor.putString("weather",responseText );
                            editor.apply();
                            showWeatherInfo(weather);

                        }else{
                            Toast.makeText(getContext(),"获取天气失败" ,Toast.LENGTH_SHORT ).show();
                        }
                    }
                });

            }
        });
    }

    private void showWeatherInfo(Weather weather){
        String cityName = weather.basic.cityname;
        String updateTime = weather.basic.update.updateTime.split("")[1];
        String degree = weather.now.temperature + "℃";
        wear(weather.now.temperature);
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for(Forecast forecast : weather.forecastList){
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.forecast_item,forecastLayout ,false );
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView)view.findViewById(R.id.info_text);
            TextView maxText = (TextView)view.findViewById(R.id.max_text);
            TextView minText = (TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.aqi);
        }
//        String comfort = "舒适度：" + weather.suggestion.comfort.info;
//        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
//        String sport = "运动建议：" + weather.suggestion.sport.info;
//        comfortText.setText(comfort);
//        carWashText.setText(carWash);
//        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent=new Intent(getContext(),AutoUpdateService.class);
        getActivity().startService(intent);
    }

    private void wear(String temp){
        if(Integer.valueOf(temp) >0){
            Glide.with(getActivity()).load("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1561550023&di=f94445563b1134008afa39a5cf1e287d&imgtype=jpg&er=1&src=http%3A%2F%2Fimgcache.mysodao.com%2Fimg3%2FM0B%2F8F%2FD1%2FCgAPD1yZ65exuYpHAALj076r9Ps803_400x400x2.JPG").into(tv_clo1);
            Glide.with(getActivity()).load("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1560955101962&di=288002b8f376a1105bde0d1152258876&imgtype=0&src=http%3A%2F%2Fimg.alicdn.com%2Fbao%2Fuploaded%2Fi3%2F372421874%2FTB2ElRLxOpnpuFjSZFkXXc4ZpXa_%2521%2521372421874.jpg_400x400.jpg").into(tv_squ1);
            Glide.with(getActivity()).load("https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=2625915265,8560309&fm=26&gp=0.jpg").into(tv_shoe1);
            Glide.with(getActivity()).load("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1560955402433&di=19a52bfd1dcd325de453d3b973871b77&imgtype=0&src=http%3A%2F%2Fimgcache.mysodao.com%2Fimg3%2FM03%2F94%2FD6%2FCgAPEFx_rJfms7JfAACvuxnNbXA853_400x400x2.JPG").into(tv_clo2);
            Glide.with(getActivity()).load("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1560955138683&di=1e9f7b119625e615a18f9c4d683e54e6&imgtype=0&src=http%3A%2F%2Fcbu01.alicdn.com%2Fimg%2Fibank%2F2017%2F433%2F853%2F4131358334_171931925.400x400.jpg").into(tv_squ2);
            Glide.with(getActivity()).load("https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=3934640592,577798847&fm=15&gp=0.jpg").into(tv_shoe2);
            Glide.with(getActivity()).load("https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=2132055625,2897653861&fm=26&gp=0.jpg").into(tv_clo3);
            Glide.with(getActivity()).load("https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=4002398930,1187872734&fm=26&gp=0.jpg").into(tv_squ3);
            Glide.with(getActivity()).load("https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=2875880096,542594779&fm=15&gp=0.jpg").into(tv_shoe3);
        }
    }

    private void loadbingpic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Glide.with(getContext()).load(getResources().getDrawable(R.drawable.background )).into(bingPicImg);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingpic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                editor.putString("bing_pic",bingpic );
                editor.apply();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(getContext()).load(bingpic).into(bingPicImg);
                    }
                });

            }
        });
    }

}
