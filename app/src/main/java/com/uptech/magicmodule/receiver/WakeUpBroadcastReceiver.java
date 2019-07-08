package com.uptech.magicmodule.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.uptech.magicmodule.ConstantAIUI;


/**
 * <pre>
 *      author : zouweilin
 *      e-mail : zwl9517@hotmail.com
 *      time   : 2017/08/30
 *      version:
 *      desc   :
 * </pre>
 */
public abstract class WakeUpBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConstantAIUI.WAKE_UP_ACTION.equals(intent.getAction())) {
            onReceiveWakeUpBroadcast(context, intent);
        }
    }

    protected abstract void onReceiveWakeUpBroadcast(Context context, Intent intent);
}
