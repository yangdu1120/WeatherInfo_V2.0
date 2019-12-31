package com.goat.weatherInfo.models;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Remy on 30-12-2019.
 * Hourly data Model
 */

public class HourlyData {

    private static List<Data> hourlyDatas = new ArrayList<>();

    public static List<Data> getHourlyDatas() {
        return hourlyDatas;
    }

    public static void setHourlyDatas(List<Data> hourlyDatas) {
        HourlyData.hourlyDatas = hourlyDatas;
    }

}
