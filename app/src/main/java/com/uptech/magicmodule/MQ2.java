package com.uptech.magicmodule;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import static java.lang.System.loadLibrary;


/**
 * A simple {@link Fragment} subclass.
 */
public class MQ2 extends Fragment {

    /*加载JNI*/
    static {
        loadLibrary("MQ2-jni");
    }
    /*声明JNI函数*/
    private native int DeviceOpen(String path);
    private native int ReadValue(Integer mq2_state);
    private native int DeviceClose();

    private boolean ThreadRun = true;          //线程While循环条件
    private Thread thread;
    private Runnable runnable;
    private Handler handler;
    private Integer mq2_state;
    private ImageView imageView;

    public MQ2() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mq2, container, false);
        int ret = DeviceOpen(getString(R.string.MQ2Path));
        if (ret < 0) {
            new AlertDialog.Builder(getActivity())
                    .setMessage("打开设备文件失败！")
                    .setPositiveButton("确定", null)
                    .show();
            return view;
        }
        imageView = (ImageView) view.findViewById(R.id.imageview_mq2);
        mq2_state = new Integer(0);
        /*初始化一个Runnable，先不进行start()*/
        runnable = new Runnable()
        {
            @Override
            public void run()
            {
                ThreadRun = true;
                while (ThreadRun) {
                    if (ReadValue(mq2_state) > 0 )   //调用JNI函数，读取模块状态。
                    {
                        Message message = new Message();    //初始化一个消息。
                        message.what = mq2_state;
                        handler.sendMessage(message);       //向主线程发送消息。
                    }
                    try {
                        Thread.sleep(100);              //延时
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
                if (msg.what == 1)
                    imageView.setImageResource(R.drawable.mq2_abnormality);
                else
                    imageView.setImageResource(R.drawable.mq2_normal);
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
