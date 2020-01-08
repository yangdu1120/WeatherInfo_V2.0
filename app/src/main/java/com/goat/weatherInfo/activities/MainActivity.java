package com.goat.weatherInfo.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.goat.weatherInfo.BuildConfig;
import com.goat.weatherInfo.R;
import com.goat.weatherInfo.events.ErrorEvent;
import com.goat.weatherInfo.events.WeatherEvent;
import com.goat.weatherInfo.models.Currently;
import com.goat.weatherInfo.models.Data;
import com.goat.weatherInfo.models.HourlyData;
import com.goat.weatherInfo.services.WeatherServiceProvider;
import com.goat.weatherInfo.utils.LogUtil;
import com.goat.weatherInfo.utils.MathUtil;
import com.goat.weatherInfo.utils.TimeUtil;
import com.goat.weatherInfo.utils.ToastUtil;
import com.goat.weatherInfo.utils.WeathericonUtil;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by Remy on 28-12-2019.
 * landing page, to show the current weather
 */


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String CENTIGRADE_UINT = "\u00b0C";
    private static final String FAHRENHEIT_UINT = "\u00b0F";
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final int REQUEST_CHECK_SETTINGS = 100;

    private Animation refreshAnim;
    private List<Data> dailyDatas = new ArrayList<>();
    private List<Data> hourlyDatas = new ArrayList<>();

    //Defaute location (LA)
    private double latitude = 34.0686;
    private double longitude = -118.3228;

    // bunch of location related apis
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    @BindView(R.id.ll_landing_page)
    LinearLayout ll_landing_page;
    @BindView(R.id.iv_refresh)
    ImageView iv_refresh;
    @BindView(R.id.tv_city)
    TextView tv_city;
    @BindView(R.id.tv_date)
    TextView tv_date;
    @BindView(R.id.tv_temp)
    TextView tv_temp;
    @BindView(R.id.iv_weather)
    ImageView iv_weather;
    @BindView(R.id.text_summary)
    TextView tv_summary;
    @BindView(R.id.tv_temp_low)
    TextView tv_temp_low;
    @BindView(R.id.tv_temp_tag)
    TextView tv_temp_tag;
    @BindView(R.id.tv_temp_high)
    TextView tv_temp_high;
    @BindView(R.id.btn_detail_info)
    Button btn_detail_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
    }

    private void init() {

        requestCurrentWeather(latitude, longitude);
        btn_detail_info.setVisibility(View.INVISIBLE);
        refreshAnim = AnimationUtils.loadAnimation(this, R.anim.refresh_anim);
        iv_refresh.startAnimation(refreshAnim);

        //init for Location related
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                updateLocation();
            }
        };

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }


    /**
     * Starting location updates
     * Check whether location settings are satisfied and then
     * location updates will be requested
     */
    private void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        ToastUtil.showToastShort(MainActivity.this, getString(R.string.start_update));

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateLocation();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                ToastUtil.showToastLong(MainActivity.this, getString(R.string.errorMessage));
                        }
                        updateLocation();
                    }
                });
    }


    @OnClick(R.id.ll_landing_page)
    public void startLocationButtonClick() {
        // Requesting ACCESS_FINE_LOCATION using Dexter library
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        startLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            // open device settings when the permission is
                            // denied permanently
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    @OnClick(R.id.btn_detail_info)
    public void getWeatherDetailInfo() {
        Intent intent = new Intent(MainActivity.this, WeatherDetailActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.iv_refresh)
    public void updateButton() {
        iv_refresh.startAnimation(refreshAnim);
        updateLocation();
    }

    /**
     * Update the location data
     */
    private void updateLocation() {
        if (mCurrentLocation != null) {
            latitude = mCurrentLocation.getLatitude();
            longitude = mCurrentLocation.getLongitude();
            LogUtil.v(TAG, "updatedLat: " + latitude + ", " + "updatedLng: " + longitude);
            requestCurrentWeather(latitude, longitude);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.e(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.e(TAG, "User chose not to make required location settings changes.");
                        break;
                }
                break;
        }
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private int convertTemp2C(Double data){
        return MathUtil.tempConverterF2C(data);
    }

    private int convertTemp2H(Double data){
        return MathUtil.tempConverter2F(data);
    }

    //get weather data from model
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWeatherEvent(WeatherEvent weatherEvent) {

        Currently currently = weatherEvent.getWeather().getCurrently();
        dailyDatas = weatherEvent.getWeather().getDaily().getData();
        Data dailyData = dailyDatas.get(0);
        hourlyDatas = weatherEvent.getWeather().getHourly().getData();
        HourlyData.setHourlyDatas(hourlyDatas);

        int tempHighC = convertTemp2C(dailyData.getTemperatureHigh());
        int tempLowC = convertTemp2C(dailyData.getTemperatureLow());
        int tempC = convertTemp2C(currently.getTemperature());

        int tempHighH = convertTemp2H(dailyData.getTemperatureHigh());
        int tempLowH =convertTemp2H(dailyData.getTemperatureLow());
        int tempH = convertTemp2H(currently.getTemperature());

        String city = weatherEvent.getWeather().getTimezone();
        tv_city.setText(city);
        //set different degree unit(FAHRENHEIT_UINT/CENTIGRADE_UINT)
        if (city.contains("America")) {
            tv_temp_low.setText(String.valueOf(tempLowH) + FAHRENHEIT_UINT);
            tv_temp_high.setText(String.valueOf(tempHighH) + FAHRENHEIT_UINT);
            tv_temp.setText(String.valueOf(tempH) + FAHRENHEIT_UINT);
        } else {
            tv_temp_low.setText(String.valueOf(tempLowC) + CENTIGRADE_UINT);
            tv_temp_high.setText(String.valueOf(tempHighC) + CENTIGRADE_UINT);
            tv_temp.setText(String.valueOf(tempC) + CENTIGRADE_UINT);
        }

        tv_date.setText(TimeUtil.getTime(currently.getTime()));
        iv_weather.setImageResource(WeathericonUtil.ICONS.get(currently.getIcon()));
        tv_summary.setText(currently.getSummary());
        btn_detail_info.setVisibility(View.VISIBLE);
        iv_refresh.clearAnimation();
        iv_refresh.setVisibility(View.INVISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onErrorEvent(ErrorEvent errorEvent) {
        ToastUtil.showToastLong(MainActivity.this, errorEvent.getErrorMessage());
    }

    private void requestCurrentWeather(double lat, double lng) {
        WeatherServiceProvider weatherServiceProvider = new WeatherServiceProvider();
        weatherServiceProvider.getWeather(lat, lng);
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resuming location updates depending on allowed permissions
        if (checkPermissions()) {
            startLocationUpdates();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}