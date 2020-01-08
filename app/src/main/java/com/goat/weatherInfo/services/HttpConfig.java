package com.goat.weatherInfo.services;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Remy on 28-12-2019.
 * Http configration (using dark sky API)
 */
public class HttpConfig {

    //My own API key
    private static final String APICALL = "https://api.darksky.net/forecast/66203959032c727f3cf9fbebd266fab2/";
    private static Retrofit retrofit = null;

    public static Retrofit retrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(APICALL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
