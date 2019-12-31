package com.goat.weatherInfo.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
/**
 * Created by Remy on 28-12-2019.
 * Time related tools
 */
public class TimeUtil {

    public static String getTime(Integer dateInt) {
        long CurrentTimeLong = new Long(dateInt).longValue() * 1000;
        DateFormat ymdhmsFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
        String CurrentTime = ymdhmsFormat.format(CurrentTimeLong);
        return CurrentTime;
    }


}
