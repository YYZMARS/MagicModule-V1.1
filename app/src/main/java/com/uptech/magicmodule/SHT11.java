package com.uptech.magicmodule;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.uptech.magicmodule.bean.Forecast;
import com.uptech.magicmodule.bean.Weather;
import com.uptech.magicmodule.utils.AIUIUtil;
import com.uptech.magicmodule.utils.HttpUtil;
import com.uptech.magicmodule.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static java.lang.System.loadLibrary;


/**
 * A simple {@link Fragment} subclass.
 */
public class SHT11 extends Fragment implements View.OnClickListener {

    private static final String TAG = "SHT11";
    /*加载JNI*/
    static {
        loadLibrary("SHT11-jni");
    }
    /*声明JNI函数*/
    private native int DeviceOpen(String path);
    private native int ReadValue(Float temp, Float humidity, Float dew_point);
    private native int DeviceClose();

    private TextView temp_TextView, humidity_TextView, dew_point_TextView;
    private Float temp, humidity, dew_point;  //温度值、湿度值、露点值
    private boolean ThreadRun = false;          //线程While循环条件

    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    private FloatingActionButton mFab;
    private AIUIAgent mAIUIAgent = null;
    //交互状态
    private int mAIUIState = AIUIConstant.STATE_IDLE;

    private TextView time;
    private TextView year;
    private ImageView bingPicImg;

    private TextView tv_weather;
    private Thread thread;
    private Runnable runnable;
    private Handler handler;

    public SHT11() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sht11, container, false);

        temp_TextView = (TextView)view.findViewById(R.id.temp_value);
        humidity_TextView = (TextView)view.findViewById(R.id.humidity_value);
        dew_point_TextView = (TextView)view.findViewById(R.id.dew_point_value);
        time = (TextView)view.findViewById(R.id.time);
        year = (TextView)view.findViewById(R.id.timeyear);
        comfortText = (TextView)view.findViewById(R.id.comfort_text);
        carWashText = (TextView)view.findViewById(R.id.car_wash_text);
        sportText = (TextView)view.findViewById(R.id.sport_text);
        //tv_weather = (TextView)view.findViewById(R.id.weather);

        bingPicImg = (ImageView)view.findViewById(R.id.bing_pic_img);
        //loadbingpic();
        temp = new Float(0);
        humidity = new Float(0);
        dew_point = new Float(0);
        mFab = (FloatingActionButton)view.findViewById(R.id.fabvoice);
        mFab.setOnClickListener(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String weatherString = prefs.getString("weather",null );
        if(weatherString != null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        }else{
            requestWeather("CN101010400");
        }


        /*初始化一个Runnable，先不进行start()*/
        runnable = new Runnable()
        {
            @Override
            public void run()
            {
                while (ThreadRun) {
                    ReadValue(temp, humidity, dew_point);   //调用JNI函数，读取X、Y、Z坐标值。
                    Message message = new Message();    //初始化一个消息。
                    handler.sendMessage(message);       //向主线程发送消息。
                    try {
                        Thread.sleep(500);              //延时
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        /*创建一个Handler，用于处理子线程发送的消息*/
        handler = new Handler()
        {
            public void handleMessage(Message msg)
            {
                /*如果此时此刻子线程已经停止运行了，就不需要对文本内容进行修改了。*/
                if(!ThreadRun)
                    return;
                /*修改界面文本显示内容。*/
                temp_TextView.setText(temp.toString()+"℃");
                humidity_TextView.setText(humidity.toString()+"%RH");
                dew_point_TextView.setText(dew_point.toString()+"Td");

                long sysTime = System.currentTimeMillis();//获取系统时间
                CharSequence Time= DateFormat.format("HH:mm", sysTime);//时间显示格式
                CharSequence Year = DateFormat.format("yyyy年MM月dd日  EEEE", sysTime);//时间显示格式

                time.setText(Time); //更新时间
                year.setText(Year);

                super.handleMessage(msg);
            }
        };



        //读取数据
        if (!StartSHT11()){
            StopSHT11();
        }

        return view;
    }

    public void requestWeather(final String weatherId){

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
        String weatherInfo = weather.now.more.info;
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //进入语音识别
            case R.id.fabvoice:
                voiceRec();

                //initAIUI("今天应该穿什么衣服");
                  //initAIUI("今天天气怎么样");
                //initAIUI("打开客厅的灯");
        }
    }

    private void loadbingpic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

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

    //语音识别
    private void voiceRec() {
        RecognizerDialog dialog = new RecognizerDialog(getContext(), null);
        dialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        dialog.setParameter(SpeechConstant.ACCENT, "mandarin");

        dialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                printResult(recognizerResult);

            }

            @Override
            public void onError(SpeechError speechError) {
            }
        });
        dialog.show();
//        Toast.makeText(this, "请开始说话", Toast.LENGTH_SHORT).show();
    }

    //回调结果：
    private void printResult(RecognizerResult results) {
        String text = parseIatResult(results.getResultString());
        Log.e(TAG, "这段文字的长度为" + text.length());
        Log.e(TAG, "这段文字的识别结果为" + text);
        // 自动填写地址
        if (text.length() > 3)
            initAIUI(text);
    }

    //解析结果
    public static String parseIatResult(String json) {
        StringBuffer ret = new StringBuffer();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");

            Log.e(TAG, "words长度为" + words.length());

            for (int i = 0; i < words.length(); i++) {
                // 转写结果词，默认使用第一个结果
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                JSONObject obj = items.getJSONObject(0);
                ret.append(obj.getString("w"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret.toString();
    }

    //初始化AIUI
    public void initAIUI(String text) {
        mAIUIAgent = AIUIAgent.createAgent(getContext(), getAIUIParams(), new AIUIUtil().aiuiListener);
        AIUIMessage startMsg = new AIUIMessage(AIUIConstant.CMD_START, 0, 0, null, null);
        mAIUIAgent.sendMessage(startMsg);

        // 先发送唤醒消息，改变AIUI内部状态，只有唤醒状态才能接收语音输入
        if (AIUIConstant.STATE_WORKING != mAIUIState) {
            AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
            mAIUIAgent.sendMessage(wakeupMsg);
        }

        String params = "data_type=text";
        byte[] textData = text.getBytes();
        AIUIMessage msg = new AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0, params, textData);
        mAIUIAgent.sendMessage(msg);
    }

    /**
     * 读取配置
     */
    private String getAIUIParams() {
        String params = "";

        AssetManager assetManager = getResources().getAssets();
        try {
            InputStream ins = assetManager.open("cfg/aiui_phone.cfg");
            byte[] buffer = new byte[ins.available()];

            ins.read(buffer);
            ins.close();

            params = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return params;
    }

    private void stop() {
        if (null != this.mAIUIAgent) {
            AIUIMessage stopMsg = new AIUIMessage(AIUIConstant.CMD_STOP, 0, 0, null, null);
            mAIUIAgent.sendMessage(stopMsg);

            mAIUIAgent.destroy();
            mAIUIAgent = null;
        }
    }


    //开始读取数据
    private boolean StartSHT11() {
        /*调用JNI函数打开设备，ChipType表示芯片类型*/
        if (DeviceOpen(getString(R.string.SHT11Path)) < 0)
        {
            new AlertDialog.Builder(getActivity())
                    .setMessage("打开设备文件失败！")
                    .setPositiveButton("确定", null)
                    .show();
            return false;
        }
        /*设置线程While循环条件为true*/
        ThreadRun = true;
        /*初始化新线程*/
        thread = new Thread(runnable);
        /*运行新线程*/
        thread.start();

        return true;
    }

    //停止读取数据
    private void StopSHT11()
    {
        /*设置线程while循环条件为false，使线程退出*/
        ThreadRun = false;
        /*调用JNI函数关闭设备文件*/
        DeviceClose();
        /*设置界面文本内容为0*/
        temp_TextView.setText("0");
        humidity_TextView.setText("0");
        dew_point_TextView.setText("0");

    }



    /*销毁界面时调用的函数*/
    @Override
    public void onDestroyView(){

        super.onDestroyView();
        stop();
        /*停止线程，关闭设备文件*/
        ThreadRun = false;
        DeviceClose();


    }




}
