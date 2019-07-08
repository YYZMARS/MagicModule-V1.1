package com.uptech.magicmodule;


import android.content.ComponentName;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import static java.lang.System.loadLibrary;


/**
 * A simple {@link Fragment} subclass.
 */
public class YS17 extends Fragment{

    /*加载JNI*/
    static {
        loadLibrary("YS17-jni");
    }

    private boolean ThreadRun = true;          //线程While循环条件
    private Thread thread;
    private Runnable runnable;
    private Handler handler;
    private Integer YS17_state;
    private ImageView imageView_ys;
    private Button tv_safe;

    /*声明JNI函数*/
    private native int DeviceOpen(String path);
    private native int ReadValue(Integer YS17_state);
    private native int DeviceClose();

    public YS17() {
        // Required empty public constructor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ys17, container, false);

        int ret_ys = DeviceOpen(getString(R.string.YS17Path));
        if (ret_ys < 0) {
            new AlertDialog.Builder(getActivity())
                    .setMessage("打开设备文件失败！")
                    .setPositiveButton("确定", null)
                    .show();
            return view;
        }

        imageView_ys = (ImageView) view.findViewById(R.id.imageview_ys17);
        tv_safe = (Button)view.findViewById(R.id.safe);
        tv_safe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ComponentName componetName = new ComponentName(
                        //app2的包名
                        "com.up_tech.usbcameratest",
                        //你要启动的界面
                        "com.up_tech.usbcameratest.MainActivity");
                Intent intent= new Intent();
                intent.setComponent(componetName);
                startActivity(intent);
            }
        });

        YS17_state = new Integer(0);
        /*初始化一个Runnable，先不进行start()*/
        runnable = new Runnable()
        {
            @Override
            public void run()
            {
                ThreadRun = true;
                while (ThreadRun) {
                    if( ReadValue(YS17_state) > 0)  //调用JNI函数，读取模块状态。
                    {
                        Message message = new Message();    //初始化一个消息。
                        message.what = YS17_state;
                        handler.sendMessage(message);       //向主线程发送消息。
                    }
                    try {
                        Thread.sleep(500);
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
                if (msg.what == 0){
                    imageView_ys.setImageResource(R.drawable.ys17_fire);
                    //播放系统默认闹钟铃声
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                    Ringtone r = RingtoneManager.getRingtone(getContext(), notification);
                    r.play();
                }
                else{
                    imageView_ys.setImageResource(R.drawable.ys17_security);
                }

                super.handleMessage(msg);
            }
        };

        thread = new Thread(runnable);
        thread.start();
        return view;
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        /*停止线程，关闭设备文件*/
        ThreadRun = false;
        DeviceClose();
    }


}
