package com.uptech.magicmodule.utils;

import android.content.Context;

import java.io.InputStream;

/**
 * <pre>
 *      author : zouweilin
 *      e-mail : zwl9517@hotmail.com
 *      time   : 2017/08/29
 *      version:
 *      desc   :
 * </pre>
 */
public class FucUtil {

    /**
     * 读取asset目录下文件。
     * @return content
     */
    public static String readFile(Context mContext, String file, String code)
    {
        int len = 0;
        byte []buf = null;
        String result = "";
        try {
            InputStream in = mContext.getAssets().open(file);
            len  = in.available();
            buf = new byte[len];
            in.read(buf, 0, len);

            result = new String(buf,code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
