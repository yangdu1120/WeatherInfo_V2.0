package com.goat.weatherInfo.utils;
/**
 * Created by Remy on 28-12-2019.
 * Math related tools
 */
public class MathUtil {

    //convert temp F to C
    public static int tempConverterF2C(Double temp) {
        return (int) Math.round((temp - 32) * (5.0 / 9.0));
    }

    //round temp
    public static int tempConverter2F(Double temp) {
        return (int) Math.round((temp));
    }

}
