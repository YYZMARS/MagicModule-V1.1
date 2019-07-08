package com.uptech.magicmodule.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.Toast;

import com.flyco.animation.BounceEnter.BounceBottomEnter;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.flyco.dialog.widget.MaterialDialog;


public class DialogUtils {

    public static void showDonateDialog(Activity activity) {
        final MaterialDialog dialog = new MaterialDialog(activity);
        dialog.content("请和骑手确认付款金额").btnText("关闭", "去付款").show();
        //left btn click listener
        //right btn click listener
    }

    //跳转到支付宝付款界面
    private static void donate(Context context) {
        String intentFullUrl = "intent://platformapi/startapp?saId=10000007&" +
                "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2Ftsx04691tkjfibd19cbcre4%3F_s" +
                "%3Dweb-other&_t=1472443966571#Intent;" +
                "scheme=alipayqr;package=com.eg.android.AlipayGphone;end";
        try {
            Intent intent = Intent.parseUri(intentFullUrl, Intent.URI_INTENT_SCHEME);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
