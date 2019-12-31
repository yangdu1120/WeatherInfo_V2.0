package com.goat.weatherInfo.services;

import com.goat.weatherInfo.models.Weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
/**
 * Created by Remy on 28-12-2019.
 * get weather into by location
 */
public interface WeatherService {

    @GET("{lat},{lng}")
    Call<Weather> getWeather(@Path("lat") double lat, @Path("lng") double lng);

}
