package com.goat.weatherInfo.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Remy on 28-12-2019.
 * Toast related tools
 */
public class ToastUtil {

    private static Toast toast = null;
    //Show a short time duration Toast.
    public static void showToastShort(Context context, String str) {
        if (toast == null) {
            toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
        } else {
            toast.setText(str);
        }
        toast.show();
    }
    //Show a long time duration Toast.
    public static void showToastLong(Context context, String str) {
        if (toast == null) {
            toast = Toast.makeText(context, str, Toast.LENGTH_LONG);
        } else {
            toast.setText(str);
        }
        toast.show();
    }

}
