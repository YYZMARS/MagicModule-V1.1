package com.uptech.magicmodule;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;


public class App extends Application {

    private static Context context;
    private static App mInstance = null;

    @Override
    public void onCreate() {
        //初始化SDK
        super.onCreate();
        mInstance = this;
        context = getApplicationContext();
        //Bmob.initialize(getApplicationContext(), "17eb824a2296dabc13fabbc3674f31b6");
        initVoice();
    }

    public static App getInstance() {
        return mInstance;
    }

    //获取应用上下文环境
    public static Context getContext() {
        return context;
    }

    //初始化讯飞语音
    private void initVoice() {
        // 将“12345678”替换成您申请的APPID，申请地址：http://www.xfyun.cn
        // 请勿在“=”与appid之间添加任何空字符或者转义符
        SpeechUtility.createUtility(context, SpeechConstant.APPID + "=5b191a6b");
        Setting.setShowLog(true); //设置日志开关（默认为true），设置成false时关闭语音云SDK日志打印
    }
}
