package com.goat.weatherInfo.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Remy on 28-12-2019.
 * Time related tools
 */
public class TimeUtil {

    private static final String TAG = "TimeUtil";

    //format target time
    public static String getTime(Integer dateInt) {
        long targetTimeLong = new Long(dateInt).longValue() * 1000;
        DateFormat ymdhmsFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);
        String targetTime = ymdhmsFormat.format(targetTimeLong);
        return targetTime;
    }

    //compare currentTime with targetTime
    public static boolean compareTime(String targetTime) {

        // Convert 13 to 10 digits from current timeStamp
        long currentTimeStampSec = System.currentTimeMillis() / 1000;
        String currentTimestamp = String.format("%010d", currentTimeStampSec);
        LogUtil.v(TAG, "targetTime:" + targetTime);
        LogUtil.v(TAG, "currentTimestamp:" + currentTimestamp);

        if (Integer.parseInt(targetTime) < Integer.parseInt(currentTimestamp)) {
            return true;
        } else {
            return false;
        }
    }
}